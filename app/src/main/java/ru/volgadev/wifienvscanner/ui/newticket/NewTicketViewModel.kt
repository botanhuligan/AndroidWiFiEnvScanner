package ru.volgadev.wifienvscanner.ui.newticket

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import ru.volgadev.wifienvscanner.Common
import ru.volgadev.wifienvscanner.Common.SPEED_TEST_HOST_URLS
import ru.volgadev.wifienvscanner.data.tickets.Location
import ru.volgadev.wifienvscanner.data.tickets.Point
import ru.volgadev.wifienvscanner.data.tickets.SpeedTest
import ru.volgadev.wifienvscanner.data.tickets.SpeedTestResult
import ru.volgadev.wifienvscanner.data.tickets.Ticket
import ru.volgadev.wifienvscanner.data.tickets.TicketsAPI
import ru.volgadev.wifienvscanner.data.wifi.WiFiPointScannerApi
import ru.volgadev.wifienvscanner.util.SpeedTester
import ru.volgadev.wifilib.api.WiFiPoint
import ru.volgadev.wifilib.api.WifiStateListener
import java.io.IOException

class NewTicketViewModel : ViewModel() {

    private val TAG: String = Common.APP_TAG.plus(".ScanVM")

    val connectionError: MutableLiveData<Boolean> = MutableLiveData()

    var speedTestLiveData: MutableLiveData<SpeedTest> = MutableLiveData()

    var newTicket: MutableLiveData<Ticket> = MutableLiveData()

    /* ссылка на список контактов */
    var pointsList: MutableLiveData<List<WiFiPoint>> = MutableLiveData()

    /* старт создания нового тикета. далее - заполнение полей */
     fun createNewTicket(){
         newTicket.postValue(Ticket())
     }

    fun getCities(): Array<out String>{
        return TicketsAPI.getCities()
    }

    fun getCorpusByCity(city: String): Array<out String>{
        return TicketsAPI.getCorpusByCity(city)
    }

    fun setDescription(comment: String){
        newTicket.value?.description = comment
    }

    fun setCity(c: String){
        newTicket.value?.point = Point()
        newTicket.value?.point?.location = Location()
        newTicket.value?.point?.location?.city = c
    }

    fun setCorpus(c: String){
        newTicket.value?.point?.location?.address = c
    }

    fun setFloor(c: Int){
        newTicket.value?.point?.location?.floor = c
    }

    fun makeSpeedTest(){

        Log.d(TAG, "Start speed test")
        val speedTest: SpeedTest = SpeedTest(HashMap())
        var testCount = 0
        for (url in SPEED_TEST_HOST_URLS){
            SpeedTester.speedTest(url, object: SpeedTester.SpeedTestCallback {
                override fun onConnectionError() {
                    Log.e(TAG, "Speed test error")
                    connectionError.postValue(true)
                }

                override fun onSpeedTestResults(url: String, count: Int, milliseconds: Long, downloadRate: Float) {
                    testCount++
                    speedTest.resultMap[url] = SpeedTestResult(milliseconds, downloadRate)
                    if (testCount == SPEED_TEST_HOST_URLS.size) {
                        Log.d(TAG, "Speed success")
                        speedTestLiveData.postValue(speedTest)
                        newTicket.value!!.speedTest = speedTest
                    }
                }
            }, 5)
        }
    }

    fun sendTicket(){
        if (newTicket.value!=null){
            Log.d(TAG, "Send ticket ".plus(newTicket.value.toString()))
            TicketsAPI.sendTicket(newTicket.value!!, object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Bad request result ".plus(e.message))
                }

                override fun onResponse(call: Call, response: Response) {
                   Log.d(TAG, "Success send ticket")

                    newTicket.postValue(null)
                }
            })
        }
    }

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

                newTicket.value?.wifi_points = HashMap()
                newTicket.value?.wifi_points!!.put("points", resultPoints)
            }
        })
    }
}