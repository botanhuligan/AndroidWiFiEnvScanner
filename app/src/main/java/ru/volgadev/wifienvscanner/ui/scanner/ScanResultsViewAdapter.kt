package ru.volgadev.wifienvscanner.ui.scanner

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// import com.squareup.picasso.Picasso
import ru.volgadev.wifienvscanner.Common
import ru.volgadev.wifienvscanner.R
import ru.volgadev.wifilib.api.WiFiPoint

/* вешаем листенер для обработки нажатия на кнопку Call */
interface CallClickListener {
    fun onCallClick(contactId: String)
}

/**
 * Адаптер RecyclerView со списком контактов
 *
 * @author mmarashan
 */
class ScanResultsViewAdapter : RecyclerView.Adapter<ContactViewElementsHolder>() {

    private val TAG: String = Common.APP_TAG.plus("ContactsAdapter")

    private var contacts: List<WiFiPoint> = mutableListOf()
    // private var context: Context? = null
    protected var clickCallListener: CallClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewElementsHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_contact, parent, false)
        // context = parent.context
        return ContactViewElementsHolder(v)
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    override fun onBindViewHolder(holder: ContactViewElementsHolder, position: Int) {
        holder.name.text = contacts.get(position).SSID
        holder.serverId.text = "@".plus(contacts.get(position).RSSI)
        // if (contacts.get(position).avatarUrl != null) {
        //     Picasso.with(holder.avatar.context)
        //         .load(contacts.get(position).avatarUrl)
        //         .placeholder(ru.volgadev.jitsiclient.R.drawable.list_item_bg)
        //         .into(holder.avatar);
        // }
        holder.callButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                Log.d(TAG, "Click initCall to ".plus(holder.serverId.text.toString()))
                clickCallListener?.onCallClick(contacts[position].toString())
            }
        })
    }

    fun add(contact: WiFiPoint) {
        contacts = contacts.plus(contact)
        notifyDataSetChanged()
    }

    fun setCallClickListener(listener: CallClickListener) {
        clickCallListener = listener
    }
}

class ContactViewElementsHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    internal var name: TextView = itemView.findViewById<View>(R.id.person_name) as TextView
    internal var serverId: TextView = itemView.findViewById<View>(R.id.person_id) as TextView
    internal var avatar: ImageView = itemView.findViewById<View>(R.id.person_ava) as ImageView
    internal var callButton: Button =
        itemView.findViewById<Button>(R.id.init_call_btn) as Button
}