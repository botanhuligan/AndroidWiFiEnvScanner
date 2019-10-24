package ru.volgadev.jitsiclient.util.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 * Вспомогательные функции в работе активити
 *
 * @author mmarashan
 */
object ActivityUtil {

    fun addFragment(fragmentActivity: FragmentActivity, viewId: Int, fragment: Fragment, show: Boolean){
        val tx = fragmentActivity.supportFragmentManager.beginTransaction()
        tx.replace(viewId, fragment)
        tx.addToBackStack(null)
        if (!show) tx.hide(fragment)
        else tx.show(fragment)
        tx.commit()
    }

    fun showFragment(fragmentActivity: FragmentActivity, fragment: Fragment){
        val tx = fragmentActivity.supportFragmentManager.beginTransaction()
        tx.addToBackStack(null)
        tx.setCustomAnimations(
            android.R.animator.fade_in,
            android.R.animator.fade_out)
        tx.show(fragment)
        tx.commit()
    }

    fun hideFragment(fragmentActivity: FragmentActivity, fragment: Fragment){
        val tx = fragmentActivity.supportFragmentManager.beginTransaction()
        tx.addToBackStack(null)
        tx.hide(fragment)
        tx.commit()
    }

}