package ru.volgadev.wifienvscanner.ui.newticket

import android.graphics.Color
import android.net.wifi.WifiManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

// import com.squareup.picasso.Picasso
import ru.volgadev.wifienvscanner.Common
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
class ScanResultsViewAdapter : RecyclerView.Adapter<ScanResElementsHolder>() {

    private val TAG: String = Common.APP_TAG.plus("ContactsAdapter")

    private var points: ArrayList<WiFiPoint> = ArrayList()
    // private var context: Context? = null
    protected var clickCallListener: CallClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanResElementsHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(ru.volgadev.wifienvscanner.R.layout.list_item_wifi_point, parent, false)
        // context = parent.context
        return ScanResElementsHolder(v)
    }

    override fun getItemCount(): Int {
        return points.size
    }

    override fun onBindViewHolder(holder: ScanResElementsHolder, position: Int) {
        val point: WiFiPoint = points[position]
        if (point.connected) {
            holder.ssid.text = point.SSID + " ✓ (Подключено)"
        } else {
            holder.ssid.text = point.SSID
        }
        val chanel = convertFrequencyToChannel(point.frequency)
        if (chanel >0) holder.chanel.text = "Канал: "+ chanel
        holder.signal_level.text = point.RSSI.toString()
        if (point.capabilities!=null && point.capabilities!!.isNotEmpty()) {
            holder.capabilities.text = Gson().toJson(point.capabilities!![0])
        }

        val percent =  WifiManager.calculateSignalLevel(point.RSSI, 100)
        if (percent < 30) {
            holder.signal_icon.setCardBackgroundColor(Color.RED)
        } else if (percent < 85) {
            holder.signal_icon.setCardBackgroundColor(Color.YELLOW)
        } else {
            holder.signal_icon.setCardBackgroundColor(Color.GREEN)
        }

        holder.mac_address.text = "MAC: "+point.BSSID

    }


    fun convertFrequencyToChannel(freq: Int): Int {
        return if (freq >= 2412 && freq <= 2484) {
            (freq - 2412) / 5 + 1
        } else if (freq >= 5170 && freq <= 5825) {
            (freq - 5170) / 5 + 34
        } else {
            -1
        }
    }

    fun clear() {
        val size = points.size
        points.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun add(contact: WiFiPoint) {
        points.add(contact)
        notifyDataSetChanged()
    }

    fun setCallClickListener(listener: CallClickListener) {
        clickCallListener = listener
    }
}

class ScanResElementsHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

    internal var ssid: TextView = itemView.findViewById<View>(ru.volgadev.wifienvscanner.R.id.ssid) as TextView
    internal var capabilities: TextView = itemView.findViewById<View>(ru.volgadev.wifienvscanner.R.id.capabilities) as TextView
    internal var chanel: TextView = itemView.findViewById<View>(ru.volgadev.wifienvscanner.R.id.chanel) as TextView
    internal var signal_level: TextView = itemView.findViewById<View>(ru.volgadev.wifienvscanner.R.id.signal_level) as TextView
    internal var mac_address: TextView = itemView.findViewById<View>(ru.volgadev.wifienvscanner.R.id.mac_address) as TextView
    internal var signal_icon: CardView = itemView.findViewById<View>(ru.volgadev.wifienvscanner.R.id.signal_icon) as CardView

}