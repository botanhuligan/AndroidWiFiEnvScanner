package ru.volgadev.wifienvscanner.ui.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.volgadev.wifienvscanner.Common
import ru.volgadev.wifienvscanner.data.credentials.CheckCredentialAPI
import ru.volgadev.wifienvscanner.data.credentials.Credential
import ru.volgadev.wifienvscanner.data.credentials.CredentialDAO

class LoginViewModel : ViewModel() {

    private val TAG: String = Common.APP_TAG.plus(".LoginVM")

    var credentials: Credential? = null

    // флажок о том, что успешно пройлена авторизация
    var auth: MutableLiveData<Boolean> = MutableLiveData()
    // флажок о том, что были проверены и не успешно
    var badCredential: MutableLiveData<Boolean> = MutableLiveData()


    fun loadSavedCredentials() {
        credentials = CredentialDAO.loadCredential()
    }

    fun checkSavedCredentials() {
        credentials = CredentialDAO.loadCredential()
        if (credentials == null) return
        val checkResult = CheckCredentialAPI.check(credentials!!)
        if (checkResult) {
            Log.d(TAG, "Good credentials")
            auth.postValue(true)
            badCredential.postValue(false)
        } else {
            Log.d(TAG, "Bad credentials")
            auth.postValue(false)
            badCredential.postValue(true)
        }
    }

    fun putCredentials(login: String, password: String) {
        Log.d(TAG, "Put credentials")
        credentials = Credential(login, password)
        if (credentials!=null) CredentialDAO.putCredentials(credentials!!)
    }
}