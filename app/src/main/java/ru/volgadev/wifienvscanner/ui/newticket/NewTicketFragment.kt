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
import ru.volgadev.wifienvscanner.util.Permission
import ru.volgadev.wifilib.api.WiFiPoint
import ru.volgadev.wifilib.impl.AndroidWiFiScanner
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Spinner
import android.widget.ArrayAdapter

class NewTicketFragment : Fragment() {

    private val TAG: String = APP_TAG.plus(".NewTcktFrgmnt")

    private lateinit var newTicketsViewModel: NewTicketViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        newTicketsViewModel =
            ViewModelProviders.of(this.activity!!).get(NewTicketViewModel::class.java)
        val root = inflater.inflate(ru.volgadev.wifienvscanner.R.layout.fragment_new_ticket, container, false)

        if (!AndroidWiFiScanner.checkWiFiServiceAvailable(activity!!.applicationContext)){
            showNeedPermissionMessage()
            Permission.makeEnableLocationServices(activity!!.applicationContext)
            Permission.requestWiFiPermissions(activity!!)
        }

        initScanWiFiElements(root)
        initCitySpinner(root)

        return root
    }

    private fun initScanWiFiElements(root: View){
        val scanResultRecyclerView: RecyclerView = root.findViewById(ru.volgadev.wifienvscanner.R.id.scanResultRecyclerView)
        scanResultRecyclerView.setHasFixedSize(true)
        scanResultRecyclerView.layoutManager =  LinearLayoutManager(root.context, LinearLayoutManager.VERTICAL, false)

        val scanResultsViewAdapter = ScanResultsViewAdapter()
        scanResultRecyclerView.adapter = scanResultsViewAdapter

        newTicketsViewModel.pointsList.observe(this, Observer {
            val pointsList: List<WiFiPoint> = it
            pointsList.onEach { point ->
                Log.d(TAG, "Show point ".plus(point.toString()))
                scanResultsViewAdapter.add(point)
            }
        })

        newTicketsViewModel.startScan(activity!!.applicationContext)
    }


    private fun initCitySpinner(root: View){
        /* спиннер с городами */
        val data: Array<out String> = arrayOf("Москва", "Самара", "Санкт-Петербург")
        // адаптер
        val adapter = ArrayAdapter<String>(this.context!!, android.R.layout.simple_spinner_item, data)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val spinner = root.findViewById(ru.volgadev.wifienvscanner.R.id.city_spinner) as Spinner
        spinner.adapter = adapter
        spinner.setSelection(0)
        // устанавливаем обработчик нажатия
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View,
                position: Int, id: Long
            ) {
                // показываем позиция нажатого элемента
                Toast.makeText(root.context, "Position = $position", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(arg0: AdapterView<*>) {}
        }

    }

    private fun showNeedPermissionMessage(){
        Toast.makeText(activity!!.applicationContext, "Дай разрешения", Toast.LENGTH_SHORT).show()
    }
}