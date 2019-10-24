package ru.volgadev.wifienvscanner

import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import ru.volgadev.jitsiclient.util.ui.ActivityUtil.addFragment
import ru.volgadev.jitsiclient.util.ui.ActivityUtil.hideFragment
import ru.volgadev.wifienvscanner.ui.login.LoginFragment
import ru.volgadev.wifienvscanner.ui.login.LoginViewModel
import ru.volgadev.wifienvscanner.ui.tickets.TicketsFragment

class MainActivity : AppCompatActivity() {

    private val TAG: String = Common.APP_TAG.plus(".MainActvt")

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var loginFragment: LoginFragment

    private lateinit var ticketsFragment: TicketsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "On Create")
        super.onCreate(savedInstanceState)

        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_main)


        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)

        /* добавляем LoginFragment */
        loginFragment = LoginFragment()
        addFragment(this, R.id.fragment_place, loginFragment, true)

        loginViewModel.auth.observe(this, Observer {
            Log.d(TAG, "Show tickets fragment ".plus(it))
            if (it) {
                /*  добавляем PrepareCallFragment и делаем невидимым */
                Log.d(TAG, "Show tickets fragment 2")
                ticketsFragment = TicketsFragment()
                hideFragment(MainActivity@this, loginFragment)
                addFragment(this, R.id.fragment_place, ticketsFragment, true)
            }
        })

    }
}
