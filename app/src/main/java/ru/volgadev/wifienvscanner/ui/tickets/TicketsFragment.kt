package ru.volgadev.wifienvscanner.ui.tickets

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.volgadev.jitsiclient.util.ui.ActivityUtil
import ru.volgadev.wifienvscanner.Common
import ru.volgadev.wifienvscanner.R
import ru.volgadev.wifienvscanner.data.tickets.Ticket
import ru.volgadev.wifienvscanner.ui.newticket.NewTicketFragment
import ru.volgadev.wifienvscanner.ui.newticket.NewTicketViewModel

class TicketsFragment : Fragment() {

    private val TAG: String = Common.APP_TAG.plus(".TicketsFrgmnt")

    private lateinit var ticketsViewModel: TicketsViewModel
    private lateinit var newTicketsViewModel: NewTicketViewModel

    private lateinit var newTicketBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        ticketsViewModel =
            ViewModelProviders.of(this.activity!!).get(TicketsViewModel::class.java)
        newTicketsViewModel =
            ViewModelProviders.of(this.activity!!).get(NewTicketViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_tickets, container, false)

        val ticketsRecyclerView: RecyclerView = root.findViewById(R.id.ticketsRecyclerView)
        ticketsRecyclerView.setHasFixedSize(true)
        ticketsRecyclerView.layoutManager =  LinearLayoutManager(root.context)

        val ticketsViewAdapter = TicketsViewAdapter()
        ticketsRecyclerView.adapter = ticketsViewAdapter

        /* вешаем обработчик нажания элемента */
        ticketsViewAdapter.setCallClickListener(object : CallClickListener {
            override fun onCallClick(ticketId: String) {
                Log.d(TAG, "Handle call click to $ticketId")
            }
        })

        ticketsViewModel.userTickets.observe(this, Observer {
            val ticketsList: List<Ticket> = it
            ticketsViewAdapter.clear()
            ticketsList.onEach { ticket ->
                ticketsViewAdapter.add(ticket)
            }
        })

        /* обработчик нажатия на кнопку нового тикета */
        newTicketBtn = root.findViewById<Button>(R.id.new_ticket_btn)
        newTicketBtn.setOnClickListener {
            Log.d(TAG, "Start create new ticket")
            newTicketsViewModel.createNewTicket()
        }

        Thread {ticketsViewModel.loadTickets()}.start()

        return root
    }
}