package ru.volgadev.wifienvscanner.util

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import ru.volgadev.wifienvscanner.Common
import java.io.IOException

object SpeedTester {

    interface SpeedTestCallback {
        fun onSpeedTestResults(url:String, count: Int, milliseconds: Long, downloadRate: Float)
        fun onConnectionError()
    }

    val TAG: String = Common.APP_TAG.plus(".SpeedTest")

    fun speedTest(url:String, callback: SpeedTestCallback, count: Int = 1) {

        var counter = 0

        val okHttpClient = OkHttpClient()

        var builder = Request.Builder()
        builder = builder.url(url)
        builder = builder.get()
        val request = builder.build()

        val start: Long = System.nanoTime()

        val localCallback = object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Error on speed test: "+e.message)
                callback.onConnectionError()
            }
            override fun onResponse(call: Call, response: Response) {
                counter++
                if (counter==count) {
                    Log.e(TAG, "Success on speed test")
                    val stop: Long = System.nanoTime()
                    if (response.body()!=null) {
                        Log.d(TAG, "Empty response body")
                        callback.onSpeedTestResults(url, count, (stop - start)/1000, 0.001F * 8 * (response.body()!!.string().toByteArray().size)/(stop*1f - start*1f))
                    }
                    else callback.onSpeedTestResults(url, count, stop - start, 0f)
                } else {
                    val call: Call = okHttpClient.newCall(request)
                    call.enqueue(this)
                }
            }
        }

        val call: Call = okHttpClient.newCall(request)
        call.enqueue(localCallback)
    }

}