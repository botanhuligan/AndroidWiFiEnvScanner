package ru.volgadev.wifienvscanner.ui.newticket

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.volgadev.wifienvscanner.Common
import ru.volgadev.wifienvscanner.data.wifi.WiFiPointScannerApi
import ru.volgadev.wifilib.api.WiFiPoint
import ru.volgadev.wifilib.api.WifiStateListener

class ScannerViewModel : ViewModel() {

    private val TAG: String = Common.APP_TAG.plus(".ScanVM")

    /* ссылка на список контактов */
    var pointsList: MutableLiveData<List<WiFiPoint>> = MutableLiveData()

    fun startScan(context: Context){

        WiFiPointScannerApi.startScan(context)
        // TODO: спрятать этот слой в Data
        WiFiPointScannerApi.setListener(object : WifiStateListener(){
            /* обработчки присоединения к точке [point] */
            override fun onConnect(point: WiFiPoint) {

            }

            /* обработчик отсоединия от точки [point] */
            override fun onDisconnect(point: WiFiPoint) {

            }

            /* обработчик включения wi-fi */
            override fun onOn() {

            }

            /* обработчик выключения */
            override fun onOff() {

            }

            /* обработчик нового результата сканирования */
            override fun onNewScanResult(resultPoints: List<WiFiPoint>) {
                Log.d(TAG, "New scan result: ".plus(resultPoints.toString()))
                pointsList.postValue(resultPoints)
            }
        })
    }
}