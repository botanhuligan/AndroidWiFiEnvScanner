package ru.volgadev.wifilib.api

/* обработчик изменения состояния в сети wi-fi*/
abstract class WifiStateListener {
    /* обработчки присоединения к точке [point] */
    open fun onConnect(point: WiFiPoint) {}

    /* обработчик отсоединия от точки [point] */
    open fun onDisconnect(point: WiFiPoint) {}

    /* обработчик включения wi-fi */
    open fun onOn() {}

    /* обработчик выключения */
    open fun onOff() {}

    /* обработчик нового результата сканирования */
    open fun onNewScanResult(resultPoints: List<WiFiPoint>) {}
}

/**
 * API класса, обеспечивающего получение данных о wi-fi точках
 * internal - не позволяет app-ам напрямую работать с этими классами
 *
 * @author mmarashan
 */
internal interface WiFiScannerApi {

    /* возвращает список доступных точек */
    fun getWiFiPoints(): List<WiFiPoint>

    /* возвращает точку, к которой подключены или null */
    fun getCurrentConnection(): WiFiPoint?

    /* добавляет слушателя [WifiStateListener] */
    fun addListener(listener: WifiStateListener)
}