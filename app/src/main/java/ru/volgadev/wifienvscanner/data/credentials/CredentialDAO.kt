package ru.volgadev.wifienvscanner.data.credentials

object CredentialDAO {

    /**
     * Загружает лошин-пароль из памяти. Если нулл - не сохранены
     */
    fun loadCredential(): Credential? {
        return Credential("z", "z")
    }

    fun putCredentials(credential: Credential){
        // TODO: хранить в sharedPref в зашифрованном виде
    }
}