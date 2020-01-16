package nl.bezorgdirect.mijnbd.helpers

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun Fragment.addFragment(frameId: Int, fragment: Fragment){
    fragmentManager?.beginTransaction()?.add(frameId, fragment)?.commit()
}

fun Fragment.replaceFragment(frameId: Int, fragment: Fragment) {
    fragmentManager?.beginTransaction()?.replace(frameId, fragment)?.commit()
}

fun Fragment.removeFragment(fragment: Fragment) {
    fragmentManager?.beginTransaction()?.remove(fragment)?.commit()
}

fun AppCompatActivity.addFragment(frameId: Int, fragment: Fragment){
   supportFragmentManager.beginTransaction().add(frameId, fragment).commit()
}

fun AppCompatActivity.replaceFragment(frameId: Int, fragment: Fragment) {
    supportFragmentManager.beginTransaction().replace(frameId, fragment).commit()
}

fun AppCompatActivity.removeFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction().remove(fragment).commit()
}