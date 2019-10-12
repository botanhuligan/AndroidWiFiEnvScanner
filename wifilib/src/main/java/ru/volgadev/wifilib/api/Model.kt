package ru.volgadev.wifilib.api

import java.util.Objects

/**
 * API модели для внутренней работы с с состоянием WI-FI соединения
 *
 * @author mmarashan
 */

/* схема аутентификации */
enum class AuthScheme {
    WPA3,
    WPA2,
    WPA, // Wi-Fi Protected Access
    OTHER, // включает в себя Shared Key Authentication и др. использующие mac-address-based и WEP
    CCKM, // Cisco Centralized Key Managment
    OPEN // Open Authentication. Может быть со скрытым Captive Portal Detection - запрос аутентификации через браузер
}

/* алгоритм ввода ключей */
enum class KeyManagementAlgorithm {
    IEEE8021X, // по стандарту
    EAP, // Extensible Authentication Protocol, расширяемый протокол аутентификации
    PSK, // Pre-Shared Key — каждый узел вводит пароль для доступа к сети
    WEP, // в WEP пароль является ключом шифрования (No auth key)
    SAE, // Simultaneous Authentication of Equals - может быть в WPA3
    OWE, // Opportunistic Wireless Encryption - в роутерах новых поколений, публичных сетях типа OPEN
    NONE // может быть без шифрования в OPEN, OTHER
}

/* метод шифрования */
enum class CipherMethod {
    WEP, // Wired Equivalent Privacy, Аналог шифрования трафика в проводных сетях
    TKIP, // Temporal Key Integrity Protocol
    CKIP, // Cisco Key Integrity Protocol
    CCMP, // Counter Mode with Cipher Block Chaining Message Authentication Code Protocol,
    // протокол блочного шифрования с кодом аутентичности сообщения и режимом сцепления блоков и счетчика
    // на основе AES
    NONE // может быть без шифрования в OPEN, OTHER
}

/* набор методов шифрования и протоколов, по которым может работать точка */
data class Capability(
    var authScheme: AuthScheme? = null,
    var keyManagementAlgorithm: KeyManagementAlgorithm? = null,
    var cipherMethod: CipherMethod? = null
)

/* Режим работы WiFi (или топология сетей WiFi) */
enum class TopologyMode {
    IBSS, // Эпизодическая сеть (Ad-Hoc или IBSS – Independent Basic Service Set).
    BSS, // Основная зона обслуживания Basic Service Set (BSS) или Infrastructure Mode.
    ESS // Расширенная зона обслуживания ESS – Extended Service Set.
}

/* данные о точке wi-fi */
data class WiFiPoint(
    val SSID: String,
    val BSSID: String,
    val RSSI: Int,
    val connected: Boolean = false,
    val capabilities: List<Capability>? = null,
    val topologyMode: TopologyMode? = null,
    // открыто подключение по wps (Wi-Fi Protected Setup)
    val availableWps: Boolean = false,
    val isVisible: Boolean = true,
    val frequency: Int = 0
) {

    /* по частоте определяет номер канала
    * https://en.wikipedia.org/wiki/List_of_WLAN_channels
    * */
    val chanel: Int
        get() {
            return when (frequency) {
                in 2412..2484 -> (frequency - 2412) / 5 + 1
                in 5170..5825 -> (frequency - 5170) / 5 + 34
                else -> -1
            }
        }

    /* wi-fi точка идентифицируется по названию и mac-адресу */
    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        return (this.SSID == (other as WiFiPoint).SSID && (this.BSSID == (other).BSSID))
    }

    override fun hashCode(): Int {
        return Objects.hash(this.SSID, this.BSSID)
    }
}
