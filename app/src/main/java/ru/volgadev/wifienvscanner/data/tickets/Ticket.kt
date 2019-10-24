package ru.volgadev.wifienvscanner.data.tickets

import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import ru.volgadev.wifilib.api.WiFiPoint
import java.util.Date
import kotlinx.serialization.*

/**
 * Модель тикета соответствующая АПИ
 */
@Serializable
data class Ticket(val id: String?,
                  val title: String?,
                  val label: Label?,
                  @ContextualSerialization val dateTime: Date,
                  val description: String?,
                  val speedTest: SpeedTest?,
                  val wifiPoints: List<@ContextualSerialization WiFiPoint>,
                  val status: String,
                  val point: Point
                  ){

    fun toJson(): String {
        val json = Json(JsonConfiguration.Stable)
        // serializing objects
        val jsonData = json.stringify(Ticket.serializer(), this)
        return jsonData
    }

    companion object {
        fun fromJson(str: String): Ticket {
            val json = Json(JsonConfiguration.Stable)
            val obj = json.parse(Ticket.serializer(), str)
            return obj
        }
    }
}


@Serializable
data class SpeedTestResult(val ping: Float, val upload: Float, val download: Float)
@Serializable
data class SpeedTest(val resultMap: Map<String, SpeedTestResult>)

@Serializable
data class Point(val id: String?,
                 val x: Float?,
                 val y: Float?,
                 val location: Location?)

@Serializable
data class Location(val id: String?, val img: String?, val city: String?, val address: String?, val floor: Int)

enum class Label(val humanText: String) {
    not_see("Not Visible WiFi"),
    low_signal("Low Wifi Signal"),
    noise("Noisy Channels"),
    lost_traffic("Package Lost"),
    other("Not Visible WiFi"),
    unlabeled("Not Visible WiFi")
}