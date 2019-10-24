package ru.volgadev.wifienvscanner.data.tickets

import android.util.Log
import java.util.Locale
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.OkHttpClient
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Credentials
import okhttp3.MediaType
import okhttp3.Response
import ru.volgadev.wifienvscanner.Common
import ru.volgadev.wifienvscanner.Common.ADMIN_HOST_URL
import java.io.IOException
import java.lang.Exception
import okhttp3.RequestBody



/**
 * Апи для работы с тикетами из базы данных
 */
object TicketsAPI {

    private val TAG: String = Common.APP_TAG.plus(".TicketsAPI")
    private val TICKET_URL_PATH: String ="ticket/"

    private var testTickets: ArrayList<Ticket> = ArrayList<Ticket>().
        apply{
            add(Ticket("Заявка 1", "Заявка 1"))
            add(Ticket("Заявка 2", "Заявка 2"))
            add(Ticket("Заявка 3", "Заявка 3"))
        }

    private var cities: Array<String> = arrayOf("", "Москва", "Самара")


    private var cityCorpusMap: Map<String, Array<String>> = HashMap<String, Array<String>>().apply {
        this.put("", arrayOf())
        this.put("москва", arrayOf("ДФ3.К1", "Кутузовская 32. корп.Г"))
        this.put("самара", arrayOf("Московское шоссе 41"))
    }

    fun getTickets(): List<Ticket>? {

        val okHttpClient = OkHttpClient()
        var builder = getValidRequesBuilder()
        builder = builder.get()
        val request = builder.build()
        val call = okHttpClient.newCall(request)

        var result: Response? = null
        Log.d(TAG, "Send GET request synch")
        try {
            result = call.execute()
        } catch (e: Exception){
            Log.e(TAG, "Exception when sent GET request "+e.message)
            return null
        }
        if (result.isSuccessful) {

            Log.d(TAG, "Success GET request" )
            if (result.body()!=null){
                val tickets = Ticket.fromArrayJson(result.body()!!.string())
                Log.d(TAG, "Success GET request: $tickets")
                return tickets
            } else {
                Log.w(TAG, "Server return empty body")
                return null
            }

        } else {
            if (result.body()!=null) {
                Log.w(TAG, "Bad GET request: " + result.body()!!.string())
            } else {
                Log.w(TAG, "Bad GET request, response body null")
            }
            return null
        }
    }

    val JSON = MediaType.parse("application/json; charset=utf-8")
    fun sendTicket(ticket: Ticket, callback: Callback? = null){

        val okHttpClient = OkHttpClient()

        val body = RequestBody.create(JSON, ticket.toJson())

        var builder = getValidRequesBuilder()
        builder = builder.post(body)
        val request = builder.build()

        val call = okHttpClient.newCall(request)

        if (callback!= null) {
            Log.i(TAG, "Send POST request asynch: " + ticket.toJson())
            call.enqueue(callback)
        } else {
            Log.i(TAG, "Send request synch")
            var result: Response? = null
            try {
                result = call.execute()
            } catch (e: Exception){
                Log.e(TAG, "Exception when sent GET request "+e.message)
            }
            if (result!!.isSuccessful) {
                Log.i(TAG, "Success request")
            } else {
                if (result.body()!=null) {
                    Log.w(TAG, "Bad POST request: "+ result.body()!!.string())
                } else {
                    Log.w(TAG, "Bad POST request")
                }
            }
        }
    }

    private fun getValidRequesBuilder(): Request.Builder {
        var builder = Request.Builder()
        builder = builder.url(ADMIN_HOST_URL+TICKET_URL_PATH)
        val credential = Credentials.basic("test_user", "SberDevices2019")
        Log.d(TAG, "Auth header $credential")
        builder.addHeader("Authorization", credential)
        return builder
    }

    fun getCities(): Array<out String>{
        return cities
    }

    fun getCorpusByCity(city: String): Array<out String>{
        if (!cityCorpusMap.containsKey(city.toLowerCase(Locale.ROOT))) return arrayOf()
        return cityCorpusMap.get(city.toLowerCase(Locale.ROOT))!!
    }

}