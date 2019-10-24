package ru.volgadev.wifienvscanner.ui.login

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import ru.volgadev.wifienvscanner.Common
import ru.volgadev.wifienvscanner.R

class LoginFragment : Fragment() {

    private val TAG: String = Common.APP_TAG.plus(".LoginFrgmnt")

    private lateinit var mLoginViewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mLoginViewModel =
            ViewModelProviders.of(this).get(LoginViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_login, container, false)

        val loginEditText: EditText = root.findViewById(R.id.loginEt)
        val passwordEditText: EditText = root.findViewById(R.id.passwordEt)
        val loginButton: Button = root.findViewById(R.id.loginBtn)

        // скрываем символы логина
        passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()

        /* сначала пробуем по сохраненным */
        loginEditText.freezesText = true
        passwordEditText.freezesText = true
        mLoginViewModel.loadSavedCredentials()
        if (mLoginViewModel.credentials!=null){
            Log.d(TAG, "Set saved credentials")
            loginEditText.setText(mLoginViewModel.credentials!!.login)
            passwordEditText.setText(mLoginViewModel.credentials!!.password)
            // mLoginViewModel.checkSavedCredentials()
        }


        // при нажатии на кнопку обнавляем логин-пароль и проверяем
        loginButton.setOnClickListener {
            mLoginViewModel.putCredentials(loginEditText.text.toString(),
                passwordEditText.text.toString()
            )
            mLoginViewModel.checkSavedCredentials()
        }

        /* обсерверы на флаги результатов проверки логина */
        mLoginViewModel.auth.observe(this, Observer {
            if (it) {
                Toast.makeText(view!!.context,"✓ Вход", Toast.LENGTH_LONG).show()
                Log.d(TAG, "Success auth")
            }
        })

        mLoginViewModel.badCredential.observe(this, Observer {
            if (it) {
                Toast.makeText(view!!.context,"❌ Неверные логин и пароль", Toast.LENGTH_SHORT).show()
                loginEditText.freezesText = false
                passwordEditText.freezesText = false
                Log.d(TAG, "Bad credential")
            }
        })

        return root
    }

}