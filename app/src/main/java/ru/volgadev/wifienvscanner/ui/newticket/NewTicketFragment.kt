package ru.volgadev.wifienvscanner.ui.newticket

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.volgadev.wifienvscanner.Common.APP_TAG
import ru.volgadev.wifienvscanner.R
import ru.volgadev.wifienvscanner.util.Permission
import ru.volgadev.wifilib.api.WiFiPoint
import ru.volgadev.wifilib.impl.AndroidWiFiScanner

class NewTicketFragment : Fragment() {

    private val TAG: String = APP_TAG.plus(".ScanFragment")

    private lateinit var newTicketsViewModel: NewTicketViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        newTicketsViewModel =
            ViewModelProviders.of(this.activity!!).get(NewTicketViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_new_ticket, container, false)

        if (!AndroidWiFiScanner.checkWiFiServiceAvailable(activity!!.applicationContext)){
            showNeedPermissionMessage()
            Permission.makeEnableLocationServices(activity!!.applicationContext)
            Permission.requestWiFiPermissions(activity!!)
        }

        val scanResultRecyclerView: RecyclerView = root.findViewById(R.id.scanResultRecyclerView)
        scanResultRecyclerView.setHasFixedSize(true)
        scanResultRecyclerView.layoutManager =  LinearLayoutManager(root.context)

        val scanResultsViewAdapter = ScanResultsViewAdapter()
        scanResultRecyclerView.adapter = scanResultsViewAdapter

        /* вешаем обработчик нажания кнопки initCall у элемента */
        scanResultsViewAdapter.setCallClickListener(object : CallClickListener {
            override fun onCallClick(contactId: String) {
                Log.d(TAG, "Handle call click to $contactId")
            }
        })

        newTicketsViewModel.pointsList.observe(this, Observer {
            val pointsList: List<WiFiPoint> = it
            pointsList.onEach { point ->
                Log.d(TAG, "Show point ".plus(point.toString()))
                scanResultsViewAdapter.add(point)
            }

        })

        newTicketsViewModel.startScan(activity!!.applicationContext)

        return root
    }

    private fun showNeedPermissionMessage(){
        Toast.makeText(activity!!.applicationContext, "Дай разрешения", Toast.LENGTH_SHORT).show()
    }
}