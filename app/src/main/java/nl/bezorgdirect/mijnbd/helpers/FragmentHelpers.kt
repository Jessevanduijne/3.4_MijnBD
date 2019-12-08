package nl.bezorgdirect.mijnbd.helpers

import androidx.fragment.app.Fragment


public fun Fragment.addFragment(frameId: Int, fragment: Fragment){
    fragmentManager?.beginTransaction()?.add(frameId, fragment)?.commit()
}

public fun Fragment.replaceFragment(frameId: Int, fragment: Fragment) {
    fragmentManager?.beginTransaction()?.replace(frameId, fragment)?.commit()
}

public fun Fragment.removeFragment(fragment: Fragment) {
    fragmentManager?.beginTransaction()?.remove(fragment)?.commit()
}