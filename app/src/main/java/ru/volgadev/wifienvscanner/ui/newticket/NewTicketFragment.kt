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
import android.widget.Button
import android.widget.EditText
import ru.volgadev.wifienvscanner.R
import java.lang.Exception

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
        initOptionalFields(root)

        return root
    }


    private fun initScanWiFiElements(root: View){
        val scanResultRecyclerView: RecyclerView = root.findViewById(ru.volgadev.wifienvscanner.R.id.scanResultRecyclerView)
        scanResultRecyclerView.setHasFixedSize(true)
        scanResultRecyclerView.layoutManager =  LinearLayoutManager(root.context, LinearLayoutManager.VERTICAL, false)

        val scanResultsViewAdapter = ScanResultsViewAdapter()
        scanResultRecyclerView.adapter = scanResultsViewAdapter

        newTicketsViewModel.pointsList.observe(this, Observer {
            Log.d(TAG, "New WiFi scan result "+it.size)
            val pointsList: List<WiFiPoint> = it
            scanResultsViewAdapter.clear()
            pointsList.onEach { point ->
                scanResultsViewAdapter.add(point)
            }
        })

        val scanBtn: Button = root.findViewById(R.id.to_scan_btn)
        scanBtn.setOnClickListener{
            Log.d(TAG, "Start scan wifi network")
            newTicketsViewModel.startScan(activity!!.applicationContext)
        }

    }


    private fun initOptionalFields(root: View){

        // описание
        val textComment = root.findViewById(R.id.text_comment) as EditText
        // этаж
        val floor = root.findViewById(R.id.text_floor) as EditText

        /* спиннер с городами */
        val citiesArr: Array<out String> = newTicketsViewModel.getCities()
        val adapter = ArrayAdapter<String>(this.context!!, android.R.layout.simple_spinner_item, citiesArr)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val spinnerCity = root.findViewById(R.id.city_spinner) as Spinner
        spinnerCity.adapter = adapter

        /* спиннер с корпусами */
        val spinnerCorp = root.findViewById(R.id.corpus_spinner) as Spinner
        var addressArr: Array<out String>? = null

        // устанавливаем обработчики выбора
        spinnerCity.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View,
                position: Int, id: Long
            ) {

                newTicketsViewModel.setCity(citiesArr[position])

                addressArr = newTicketsViewModel.getCorpusByCity(citiesArr[position])
                val adapterCorp = ArrayAdapter<String>(view.context!!, android.R.layout.simple_spinner_item, addressArr!!)
                adapterCorp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCorp.adapter = adapterCorp

                spinnerCorp.visibility = View.VISIBLE
            }

            override fun onNothingSelected(arg0: AdapterView<*>) {}
        }
        spinnerCorp.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View,
                position: Int, id: Long
            ) {
                if (addressArr!=null) newTicketsViewModel.setCity(addressArr!![position])
                floor.visibility = View.VISIBLE
            }

            override fun onNothingSelected(arg0: AdapterView<*>) {}
        }


        // сначала делаем спинер с корпусами невидимым
        spinnerCorp.visibility = View.GONE
        floor.visibility = View.GONE


        val sendBtn: Button = root.findViewById(R.id.to_send_btn)
        sendBtn.setOnClickListener{
            Log.d(TAG, "Send ticket")
            // проставляем этаж
            try {
                if (floor.text.toString().trim().isNotEmpty()) {
                    val floorInt = Integer.parseInt(floor.text.toString())
                    newTicketsViewModel.setFloor(floorInt)
                }
            } catch (e: Exception){
                Log.w(TAG, "Bad floor number")
            }
            // поле комментарий
            newTicketsViewModel.setDescription(textComment.text.toString())

            // отправляем тикет
            newTicketsViewModel.sendTicket()

        }

    }

    private fun showNeedPermissionMessage(){
        Toast.makeText(activity!!.applicationContext, "Дай разрешения", Toast.LENGTH_SHORT).show()
    }
}