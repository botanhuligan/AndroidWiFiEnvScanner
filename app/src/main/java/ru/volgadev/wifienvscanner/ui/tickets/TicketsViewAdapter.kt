package ru.volgadev.wifienvscanner.ui.tickets

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// import com.squareup.picasso.Picasso
import ru.volgadev.wifienvscanner.Common
import ru.volgadev.wifienvscanner.R
import ru.volgadev.wifienvscanner.data.tickets.Ticket

/* вешаем листенер для обработки нажатия на кнопку Call */
interface CallClickListener {
    fun onCallClick(contactId: String)
}

/**
 * Адаптер RecyclerView со списком тикетов
 *
 * @author mmarashan
 */
class TicketsViewAdapter : RecyclerView.Adapter<TicketElementHolder>() {

    private val TAG: String = Common.APP_TAG.plus("TicketsAdapter")

    private var contacts: ArrayList<Ticket> = arrayListOf()
    protected var clickCallListener: CallClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketElementHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_ticket, parent, false)
        return TicketElementHolder(v)
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    override fun onBindViewHolder(holder: TicketElementHolder, position: Int) {
        val item: Ticket = contacts[position]
        holder.title.text = "Тикет #"+item.id
        if (item.status!=null) holder.status.text = item.status!!.title
        if (item.description!=null) holder.comment.text = "Описание: "+item.description
        if (item.label!=null && item.label!!.title !="No Label") holder.label.text = "Тема: "+ item.label!!.title

        // holder.item.setOnClickListener {
        //     Log.d(TAG, "Click to ".plus(holder.title.text.toString()))
        // }
    }


    fun clear() {
        val size = contacts.size
        contacts.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun add(contact: Ticket) {
        contacts.add(contact)
        notifyDataSetChanged()
    }

    fun setCallClickListener(listener: CallClickListener) {
        clickCallListener = listener
    }
}

class TicketElementHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    internal var item: View = itemView

    internal var title: TextView = itemView.findViewById<View>(R.id.t_title) as TextView
    internal var label: TextView = itemView.findViewById<View>(R.id.t_label) as TextView
    internal var comment: TextView = itemView.findViewById<View>(R.id.t_description) as TextView
    internal var status: TextView = itemView.findViewById<View>(R.id.T_status) as TextView
}