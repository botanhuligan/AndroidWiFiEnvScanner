package ru.volgadev.wifienvscanner.data.credentials

object CheckCredentialAPI {

    /**
     * Спрашивает у сервера, валидные ли логин-пароль. Если нет, дальше ничего с сервера приходить не будет
     */
    fun check(cred: Credential): Boolean {
        return (cred.login == "z" && cred.password == "z")
    }
}