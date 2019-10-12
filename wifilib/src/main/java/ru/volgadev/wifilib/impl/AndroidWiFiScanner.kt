package ru.volgadev.wifilib.impl

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.wifi.ScanResult
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.*
import android.util.Log
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.core.content.ContextCompat
import ru.volgadev.wifilib.api.AuthScheme
import ru.volgadev.wifilib.api.Capability
import ru.volgadev.wifilib.api.CipherMethod
import ru.volgadev.wifilib.api.KeyManagementAlgorithm
import ru.volgadev.wifilib.api.TopologyMode
import ru.volgadev.wifilib.api.WiFiPoint
import ru.volgadev.wifilib.api.WiFiScannerApi
import ru.volgadev.wifilib.api.WifiStateListener

private const val TAG = "DroidWiFiScan"

/**
 * Реализация WiFiScannerApi с использованием android api
 * @author mmarashan
 */
object AndroidWiFiScanner : WiFiScannerApi {

    /* Android классы */
    /* обсеспечивает доступ к состоянию текущего подключения */
    private var connectivityManager: ConnectivityManager? = null
    /* обсеспечивает доступ к информации об окружающих wi-fi точках */
    private var wifiManager: WifiManager? = null

    /* API классы */
    private val nearWiFiPointList = mutableListOf<WiFiPoint>()
    @Volatile
    private var currentWiFiPoint: WiFiPoint? = null
    private val listenerList = mutableListOf<WifiStateListener>()

    /**
     * запуск работы сканера
     * подписывается на системные сообщения о состоянии сети и достает состояния с помощью android api
     */
    @MainThread
    fun startScan(context: Context): Boolean {

        wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        connectivityManager =
            context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        /* подписываем WifiScanReceiver на системные сообщения о состоянии wi-fi*/
        if (wiFiScanReceiver.register(context) == null) return false

        /** startScan() method was deprecated in API level 28.
         * The ability for apps to trigger scan requests will be removed in a future release. */
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.P) {
            @Suppress("DEPRECATION") val success = wifiManager!!.startScan()
            if (!success) {
                return false
            }
        }

        // инициализируем currentWiFiPoint и nearWiFiPointList
        updateCurrentPoint()
        handleScanResults()
        return true
    }

    /* получатель BroadcastReceiver широковещательных сообщений о состоянии wi-fi */
    private val wiFiScanReceiver = object : BroadcastReceiver() {

        var isRegistered: Boolean = false

        /**
         * register receiver
         * @param context - Context
         * @return see Context.registerReceiver(BroadcastReceiver,IntentFilter)
         */
        fun register(context: Context): Intent? {
            var intent: Intent? = null
            try {
                if (!isRegistered) {
                    val intentFilter = IntentFilter()
                    intentFilter.addAction(SCAN_RESULTS_AVAILABLE_ACTION)
                    intentFilter.addAction(NETWORK_STATE_CHANGED_ACTION)
                    intentFilter.addAction(NETWORK_IDS_CHANGED_ACTION)
                    intent = context.registerReceiver(this, intentFilter)
                }
            } finally {
                isRegistered = true
                return intent
            }
        }

        /**
         * unregister received
         * @param context - context
         */
        fun unregister(context: Context) {
            context.unregisterReceiver(this)
            isRegistered = false
        }

        override fun onReceive(c: Context, intent: Intent) {
            // обработка результата скана сетей
            if (intent.action.equals(SCAN_RESULTS_AVAILABLE_ACTION)) {
                val resultsUpdated = intent.getBooleanExtra(EXTRA_RESULTS_UPDATED, false)
                if (resultsUpdated) {
                    Log.d(TAG, "On new scan results")
                    handleScanResults()
                }
            }
            // обработка изменения wi-fi (вкл/выкл)
            if (intent.action.equals(NETWORK_STATE_CHANGED_ACTION)) {
                val wifiState: Int = intent.getIntExtra(EXTRA_WIFI_STATE, 0)
                Log.d(TAG, "Change wi-fi state")
                when (wifiState) {
                    WIFI_STATE_DISABLED -> {
                        Log.d(TAG, "Wi-Fi state disabled")
                        listenerList.forEach { it.onOff() }
                    }
                    WIFI_STATE_ENABLED -> {
                        Log.d(TAG, "Wi-Fi state enabled")
                        listenerList.forEach { it.onOn() }
                    }
                    WIFI_STATE_UNKNOWN -> {
                        Log.w(TAG, "Wi-Fi state unknow")
                    }
                }
            }
            // обработка изменения сети
            if (intent.action == NETWORK_IDS_CHANGED_ACTION) {
                updateCurrentPoint()
            }
        }
    }

    /* остановка работы. отписка от системных сообщений */
    @MainThread
    fun stop(context: Context) {
        Log.d(TAG, "Stop listen system wi-fi messages")
        wiFiScanReceiver.unregister(context)
    }

    override fun getWiFiPoints(): List<WiFiPoint> {
        return nearWiFiPointList.toList()
    }

    override fun getCurrentConnection(): WiFiPoint? {
        return currentWiFiPoint
    }

    @AnyThread
    @Synchronized
    override fun addListener(listener: WifiStateListener) {
        Log.d(TAG, "Add new listener ".plus(listener.toString()))
        listenerList += listener
    }

    /* обновляет информацию о текущем соединении из WifiInfo */
    private fun updateCurrentPoint() {

        val wifiInfo: WifiInfo = wifiManager!!.connectionInfo
        if (wifiInfo.supplicantState != SupplicantState.DISCONNECTED) {
            val point = WiFiPoint(
                SSID = wifiInfo.ssid,
                BSSID = wifiInfo.bssid,
                RSSI = wifiInfo.rssi,
                connected = true
            )
            /* если новая точка, сообщаем слушателям, что отключились от старого и подключились к новому */
            if (point != currentWiFiPoint) {
                if (currentWiFiPoint != null) listenerList.forEach { it.onConnect(currentWiFiPoint!!) }
                listenerList.forEach { it.onConnect(point) }
            }
            /* обновляем данные */
            if (currentWiFiPoint != null && nearWiFiPointList.contains(currentWiFiPoint!!)) {
                nearWiFiPointList.add(nearWiFiPointList.indexOf(currentWiFiPoint!!), point)
            } else {
                nearWiFiPointList += point
            }
            currentWiFiPoint = point
        } else {
            if (currentWiFiPoint != null) {
                listenerList.forEach { it.onDisconnect(currentWiFiPoint!!) }
                Log.d(TAG, "Disconnect from point ".plus(currentWiFiPoint))
                nearWiFiPointList -= currentWiFiPoint!!
                currentWiFiPoint = null
            }
        }
        Log.d(TAG, "Update current point ")
        Log.d(TAG, currentWiFiPoint.toString())
    }

    /* обработчик получения новых результатов сканирования wi-fi */
    private fun handleScanResults() {
        val results = wifiManager!!.scanResults
        Log.d(TAG, "Scan results: ".plus(results.size).plus(" points"))

        /* очищаем сет и заполняем заново */
        nearWiFiPointList.clear()

        results.forEach {
            val point: WiFiPoint = convert(it)
            nearWiFiPointList += point
        }

        updateCurrentPoint()

        if (nearWiFiPointList.size > 0) listenerList.forEach { it.onNewScanResult(nearWiFiPointList.toList()) }
    }

    /* конвертирует объект ScanResult в WiFiPoint */
    private fun convert(scanResult: ScanResult): WiFiPoint {
        return WiFiPoint(
            scanResult.SSID,
            scanResult.BSSID,
            scanResult.level,
            false,
            capabilities = parseCapabilities(scanResult),
            topologyMode = parseTopologyMode(scanResult),
            availableWps = parseWPSAvailable(scanResult),
            frequency = scanResult.frequency
        )
    }

    /* вытаскивает поддерживаемые протоколы и алгоритмы шифрования */
    private fun parseCapabilities(scanResult: ScanResult): List<Capability> {
        /* описание разбора строки scanResult.capabilities */
        /* first items [Authentication Scheme - Key Management Algorithm - Pairwise Cipher] */
        // https://stackoverflow.com/questions/21607815/how-do-i-connect-to-a-wifi-network-with-an-unknown-encryption-algorithm-in-andro
        /* second item [Topology mode]  */
        // http://1234g.ru/wifi/topologii-setej-wifi
        /* third item [allow WPS]  */
        // https://ru.wikipedia.org/wiki/Wi-Fi_Protected_Setup
        val capabilities: ArrayList<Capability> = ArrayList<Capability>()
        var firstQuoteIndex = 0
        var secondQuoteIndex = scanResult.capabilities.indexOf("]")

        Log.d(TAG, "Capability of ".plus(scanResult.SSID).plus(" ").plus(scanResult.capabilities))

        // цикл по всем []
        while (firstQuoteIndex <= scanResult.capabilities.length - 1) {
            val elem = scanResult.capabilities.substring(firstQuoteIndex + 1, secondQuoteIndex)

            // проверяем, что элемент не относится к топологии и флагу WPS
            if ((enumValues<TopologyMode>().any { it.name == elem }) || ("WPS" == elem)) {
                firstQuoteIndex = secondQuoteIndex + 1
                secondQuoteIndex = scanResult.capabilities.indexOf("]", firstQuoteIndex)
                continue
            }

            val cpblt = Capability()

            if (elem.contains("WPA3")) {
                cpblt.authScheme = AuthScheme.WPA3
            } else if (elem.contains("WPA2")) {
                cpblt.authScheme = AuthScheme.WPA2
            } else if (elem.contains("WPA")) {
                cpblt.authScheme = AuthScheme.WPA
            } else if (elem.contains("WEP")) {
                // точка поддерживает WEP-based протокол (подробности удаленно выяснить нельзя)
                cpblt.authScheme = AuthScheme.OTHER
                cpblt.cipherMethod = CipherMethod.WEP
                cpblt.keyManagementAlgorithm = KeyManagementAlgorithm.WEP
            }

            if (elem.contains("OWE")) {
                cpblt.keyManagementAlgorithm =
                    KeyManagementAlgorithm.OWE
            } else if (elem.contains("SAE")) {
                cpblt.keyManagementAlgorithm =
                    KeyManagementAlgorithm.SAE
            } else if (elem.contains("IEEE802.1X")) {
                cpblt.keyManagementAlgorithm =
                    KeyManagementAlgorithm.IEEE8021X
            } else if (elem.contains("EAP")) {
                cpblt.keyManagementAlgorithm = KeyManagementAlgorithm.EAP
            } else if (elem.contains("PSK")) {
                cpblt.keyManagementAlgorithm = KeyManagementAlgorithm.PSK
            }

            if (elem.contains("TKIP") && elem.contains("CCMP")) {
                cpblt.cipherMethod = CipherMethod.TKIP
                val capability2 = cpblt.copy()
                capability2.cipherMethod = CipherMethod.CCMP
                capabilities.add(cpblt)
                capabilities.add(capability2)
            } else if (elem.contains("TKIP")) {
                cpblt.cipherMethod = CipherMethod.TKIP
            } else if (elem.contains("CCMP")) {
                cpblt.cipherMethod = CipherMethod.CCMP
            }

            // если хоть один признак определен, ставим значения по умолчанию и добавляем
            if ((cpblt.authScheme != null) || (cpblt.cipherMethod != null) || (cpblt.keyManagementAlgorithm != null)) {
                // если не обнаружен алгоритм аутентификации, считаем сеть открытой
                if (cpblt.authScheme == null) cpblt.authScheme = AuthScheme.OPEN
                // если не обнаружен алгоритм шифрования, считаем трафик открытым
                if (cpblt.cipherMethod == null) cpblt.cipherMethod = CipherMethod.NONE
                // если не обнаружен алгоритма работы с ключами шифрования, считаем что его нет
                if (cpblt.keyManagementAlgorithm == null) cpblt.keyManagementAlgorithm = KeyManagementAlgorithm.NONE
                capabilities.add(cpblt)
            }

            firstQuoteIndex = secondQuoteIndex + 1
            secondQuoteIndex = scanResult.capabilities.indexOf("]", firstQuoteIndex)
        }

        // если не нашли ни одну capability, считаем что есть возможность подключиться в открытую
        if (capabilities.size == 0) capabilities.add(
            Capability(
                authScheme = AuthScheme.OPEN,
                cipherMethod = CipherMethod.NONE,
                keyManagementAlgorithm = KeyManagementAlgorithm.NONE
            )
        )

        return capabilities
    }

    /* вытаскивает топологию wi-fi сети, к которой относится точка */
    private fun parseTopologyMode(scanResult: ScanResult): TopologyMode? {
        var firstQuoteIndex = 0
        var secondQuoteIndex = scanResult.capabilities.indexOf("]")

        // цикл по всем []
        while (firstQuoteIndex <= scanResult.capabilities.length - 1) {
            val elem = scanResult.capabilities.substring(firstQuoteIndex + 1, secondQuoteIndex)

            if (elem.contains("ESS")) return TopologyMode.ESS
            if (elem.contains("BSS")) return TopologyMode.BSS
            if (elem.contains("IBSS")) return TopologyMode.IBSS

            firstQuoteIndex = secondQuoteIndex + 1
            secondQuoteIndex = scanResult.capabilities.indexOf("]", firstQuoteIndex)
        }
        return null
    }

    /* смотрит по capabilities, открыт ли доступ по WPS */
    private fun parseWPSAvailable(scanResult: ScanResult): Boolean {
        var firstQuoteIndex = 0
        var secondQuoteIndex = scanResult.capabilities.indexOf("]")

        // цикл по всем []
        while (firstQuoteIndex <= scanResult.capabilities.length - 1) {
            val elem = scanResult.capabilities.substring(firstQuoteIndex + 1, secondQuoteIndex)
            if (elem.contains("WPS")) return true
            firstQuoteIndex = secondQuoteIndex + 1
            secondQuoteIndex = scanResult.capabilities.indexOf("]", firstQuoteIndex)
        }
        return false
    }


    private fun checkPermission(context: Context, permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    /* преверяет, выданы ли разрешения на работу с wi-fi и включены ли сервися жпс и сети */
    @MainThread
    fun checkWiFiServiceAvailable(context: Context): Boolean {
        Log.w(TAG, "Check wi-fi service available")
        val lm: LocationManager =
            context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnabled: Boolean = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled: Boolean = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        // если не включены сервисы определения местоположения и сеть, работать с wi-fi не получится
        if (!gpsEnabled && !networkEnabled) {
            Log.w(TAG, "Wi-fi not available. User have to switch on on gps and network")
            return false
        } else {
            Log.d(TAG, "Gps and network provider OK")
        }
        // иначе, смотрим на то, выданы ли необходимые permissions
        val result = if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.O) {
            checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                && checkPermission(context, Manifest.permission.CHANGE_WIFI_STATE)
        } else {
            checkPermission(context, Manifest.permission.CHANGE_WIFI_STATE)
        }
        Log.d(TAG, "End check permission: $result")
        if (!result) Log.w(TAG, "Wi-fi not available. User have to allow permission")
        return result
    }
}
