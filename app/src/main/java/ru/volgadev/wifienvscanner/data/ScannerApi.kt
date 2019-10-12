package ru.volgadev.wifienvscanner.data

import android.content.Context
import ru.volgadev.wifilib.api.WiFiPoint
import ru.volgadev.wifilib.api.WifiStateListener
import ru.volgadev.wifilib.impl.AndroidWiFiScanner

/**
 * Обертка над AndroidWiFiScanner
 * @author mmarashan
 */
object ScannerApi {

    fun startScan(context: Context) {
        AndroidWiFiScanner.startScan(context)
    }

    fun stopScan(context: Context){
        AndroidWiFiScanner.stop(context)
    }

    fun setListener(listener: WifiStateListener){
        AndroidWiFiScanner.addListener(listener)
    }
}