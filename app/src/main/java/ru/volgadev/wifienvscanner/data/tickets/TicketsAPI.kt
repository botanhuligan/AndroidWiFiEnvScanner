package ru.volgadev.wifienvscanner.data.tickets

/**
 * Апи для работы с тикетами из базы данных
 */
object TicketsAPI {

    private var testTickets: ArrayList<Ticket> = ArrayList<Ticket>().
        apply{
            add(Ticket("Заявка 1", "Заявка 1"))
            add(Ticket("Заявка 2", "Заявка 2"))
            add(Ticket("Заявка 3", "Заявка 3"))
        }

    fun getTickets():ArrayList<Ticket>{
        return testTickets
    }

    fun addTicket(ticket: Ticket){
        testTickets.add(ticket)
    }

}