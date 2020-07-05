package com.vkpapps.thunder.interfaces

import java.io.Serializable

/**
 * @author VIJAY PATIDAR
 */
interface OnObjectReceiveListener {
    fun onObjectReceive(obj: Serializable)
}