package ru.volgadev.wifienvscanner.ui.tickets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.volgadev.wifienvscanner.Common
import ru.volgadev.wifienvscanner.data.tickets.Ticket
import ru.volgadev.wifienvscanner.data.tickets.TicketsAPI
import ru.volgadev.wifilib.api.WiFiPoint

class TicketsViewModel : ViewModel() {

    private val TAG: String = Common.APP_TAG.plus(".TicketsVM")

    /* список тикетов */
    var userTickets: MutableLiveData<ArrayList<Ticket>> = MutableLiveData()


    fun loadTickets() {
        val tickets = TicketsAPI.getTickets()
        userTickets.postValue(tickets)
    }

    fun addTicket(ticket: Ticket) {
        TicketsAPI.addTicket(ticket)
    }

}