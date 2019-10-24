package ru.volgadev.wifienvscanner

import androidx.test.runner.AndroidJUnit4
import okhttp3.Call
import org.junit.Test

import ru.volgadev.wifienvscanner.data.tickets.Label
import ru.volgadev.wifienvscanner.data.tickets.Location
import ru.volgadev.wifienvscanner.data.tickets.Point
import ru.volgadev.wifienvscanner.data.tickets.Ticket
import ru.volgadev.wifienvscanner.data.tickets.TicketsAPI
import ru.volgadev.wifilib.api.AuthScheme
import ru.volgadev.wifilib.api.Capability
import ru.volgadev.wifilib.api.CipherMethod
import ru.volgadev.wifilib.api.KeyManagementAlgorithm
import ru.volgadev.wifilib.api.TopologyMode
import ru.volgadev.wifilib.api.WiFiPoint
import okhttp3.Callback
import okhttp3.Response
import org.junit.runner.RunWith
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

@RunWith(AndroidJUnit4::class)
class TicketApiUnitTest {

    private val logger = Logger.getLogger(TicketApiUnitTest::class.java.toString())


    @Test
    @Throws(InterruptedException::class)
    fun testGetTickets() {
        val tickets = TicketsAPI.getTickets()
        if (tickets == null || tickets.isNotEmpty()) {
            logger.info( "Error when load tickets")
            assert(false)
        } else {
            logger.info( "Success load tickets")
            tickets.forEach { logger.info(it.toString()) }
            assert(true)
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSendTicket() {

        logger.info( "Start test")

        val ticket = Ticket()
        val wifiPoint = WiFiPoint("ssid",
            "11:11:11:11", -10,
            false,
            listOf(Capability(AuthScheme.CCKM, KeyManagementAlgorithm.EAP, CipherMethod.TKIP)),
            TopologyMode.BSS,
            false,
            true,
            2484
            )
        ticket.wifi_points = HashMap()
        ticket.wifi_points!!["points"] = arrayListOf<WiFiPoint>(wifiPoint)
        ticket.point = Point()

        ticket.point!!.location = Location("0", "", "Москва", "Кутузовская 32")
        ticket.description = "test"
        ticket.label = Label("test", "test")

        logger.log(Level.INFO, "Send ticket synch")
        TicketsAPI.sendTicket(ticket)

        Thread.sleep(1000)

        logger.info( "Send ticket asynch")
        TicketsAPI.sendTicket(ticket, object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                logger.log(Level.WARNING, "Error when send ticket ".plus(e.message))
                assert(false)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    logger.info( "Success send ticket "+response.body()!!.string())
                    assert(true)
                } else {
                    logger.info( "Bad send ticket "+response.body()!!.string())
                    assert(false)
                }
            }
        })

        logger.info( "Sleep")
        Thread.sleep(3000)
    }
}
