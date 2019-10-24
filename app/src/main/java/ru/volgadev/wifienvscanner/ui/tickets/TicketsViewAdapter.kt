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

    private var contacts: List<Ticket> = mutableListOf()
    protected var clickCallListener: CallClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketElementHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_contact, parent, false)
        return TicketElementHolder(v)
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    override fun onBindViewHolder(holder: TicketElementHolder, position: Int) {
        holder.title.text = contacts[position].title

        holder.item.setOnClickListener {
            Log.d(TAG, "Click to ".plus(holder.title.text.toString()))
        }
    }

    fun add(contact: Ticket) {
        contacts = contacts.plus(contact)
        notifyDataSetChanged()
    }

    fun setCallClickListener(listener: CallClickListener) {
        clickCallListener = listener
    }
}

class TicketElementHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    internal var item: View = itemView
    internal var title: TextView = itemView.findViewById<View>(R.id.person_name) as TextView
    // internal var serverId: TextView = itemView.findViewById<View>(R.id.person_id) as TextView
    // internal var avatar: ImageView = itemView.findViewById<View>(R.id.person_ava) as ImageView
    // internal var callButton: Button =
    //     itemView.findViewById<Button>(R.id.init_call_btn) as Button
}