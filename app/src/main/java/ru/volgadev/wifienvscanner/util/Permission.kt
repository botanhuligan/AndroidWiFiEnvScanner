package ru.volgadev.wifienvscanner.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

private const val TAG: String = "PermissionUtil"

/* класс, оброрачивающий работу с android-permissions
*
* @author mmarashan
*
* */
object Permission {


    /* проверяет, выдано ли разрешение на [androidPermission] */
    fun checkPermissionGranted(context: Context, androidPermission: String): Boolean {
        Log.d(TAG, "Check permission ".plus(androidPermission))
        return ContextCompat.checkSelfPermission(context, androidPermission) == PackageManager.PERMISSION_GRANTED
    }

    /* нужно ли показывать объяснение для чего приложению нужно требуемое разрешение */
    fun checkNeedDescribeRationale(activity: Activity, androidPermission: String): Boolean {
        Log.d(TAG, "Check rationale to describe permission ".plus(androidPermission))
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, androidPermission)
    }

    /* запрашивает разрешение */
    fun requestPermission(activity: Activity, androidPermission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(androidPermission),
            requestCode
        )
    }

    fun requestPermissions(activity: Activity, androidPermissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(
            activity,
            androidPermissions,
            requestCode
        )
    }

    /**  запрашивает разрешение для работы с wi-fi
     * @warning для коректной работы с версий OC выше 8.0 также необходим запрос на включение
     * сервиса данных о местоположении. см. makeEnableLocationServices(context)
     */
    fun requestWiFiPermissions(activity: Activity) {

        // особенности разрешений для wi-fi: https://developer.android.com/guide/topics/connectivity/wifi-scan#java
        // 8.0-8.1
        if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.O) {
            requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CHANGE_WIFI_STATE
                ),
                1
            )
            makeEnableLocationServices(activity.applicationContext)
        } else {
            requestPermission(
                activity,
                Manifest.permission.CHANGE_WIFI_STATE,
                1
            )
        }
    }

    /* включает сервис по определению местоположения */
    fun makeEnableLocationServices(context: Context) {
        val lm: LocationManager =
            context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!gpsEnabled && !networkEnabled) {
            // TODO: запуск не рассказывая пользователю, зачем. возможно надо показывать AlertDialog
            context.startActivity(Intent(ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }
}