package ru.volgadev.wifienvscanner.ui.tickets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.volgadev.wifienvscanner.Common
import ru.volgadev.wifienvscanner.data.tickets.Ticket
import ru.volgadev.wifienvscanner.data.tickets.TicketsAPI

class TicketsViewModel : ViewModel() {

    private val TAG: String = Common.APP_TAG.plus(".TicketsVM")

    /* список тикетов */
    var userTickets: MutableLiveData<List<Ticket>> = MutableLiveData()
    var ticketApiCallError: MutableLiveData<Boolean> = MutableLiveData()

    fun loadTickets() {
        val tickets = TicketsAPI.getTickets()
        if (tickets!=null) {
            userTickets.postValue(tickets)
        } else {
            ticketApiCallError.postValue(true)
        }
    }

}