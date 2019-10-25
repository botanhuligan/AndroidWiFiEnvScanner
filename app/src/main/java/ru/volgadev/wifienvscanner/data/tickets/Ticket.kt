package ru.volgadev.wifienvscanner.data.tickets

import android.util.Log
import com.google.gson.Gson
import ru.volgadev.wifienvscanner.Common
import ru.volgadev.wifilib.api.WiFiPoint
import java.util.Date

/**
 * Модель тикета соответствующая АПИ
 */
data class Ticket(var id: String? = null,
                  var title: String? = null,
                  var label: Label? = null,
                  var description: String? = null,
                  var speedTest: SpeedTest? = null,
                  var wifi_points: HashMap<String, List<WiFiPoint>>? = null,
                  var status: Status? = null,
                  var point: Point? = null
                  ) {

    val TAG: String = Common.APP_TAG.plus(".Ticket")

    fun toJson(): String {
        val jsonTicket = Gson().toJson(this)
        Log.d(TAG, "Ticket to Json $jsonTicket")
        return jsonTicket
    }

    companion object {
        val TAG: String = Common.APP_TAG.plus(".Ticket")

        fun fromJson(str: String): Ticket {
            val ticket: Ticket = Gson().fromJson<Ticket>(str, Ticket::class.java)
            return ticket
        }

        fun fromArrayJson(str: String): List<Ticket> {
            Log.d(TAG, "Parse $str")
            try {
                val tickets: List<Ticket> = Gson().fromJson(str, Array<Ticket>::class.java).toList()
                Log.d(TAG, "Parse " + tickets.size + " tickets")
                return tickets
            } catch (e:Exception){
                Log.e(TAG, "Bad parse result $e")
                return listOf()
            }
        }
    }
}


data class SpeedTestResult(var ping: Long, var download: Float)
data class SpeedTest(var resultMap: HashMap<String, SpeedTestResult>)

data class Point(var id: String? = "",
                 var x: Float? = 0f,
                 var y: Float? = 0f,
                 var location: Location? = null)

data class Location(var id: String? = null,
                    var img: String? = null,
                    var city: String? = null,
                    var address: String? = null,
                    var floor: Int = 0)

data class Status(var name: String? = null,
                    var title: String? = null)

data class Label(var name: String? = null,
                  var title: String? = null)

// enum class Label(var humanText: String) {
//     not_see("Not Visible WiFi"),
//     low_signal("Low Wifi Signal"),
//     noise("Noisy Channels"),
//     lost_traffic("Package Lost"),
//     other("Not Visible WiFi"),
//     unlabeled("Not Visible WiFi")
// }