package com.vkpapps.thunder.interfaces

import com.vkpapps.thunder.connection.ClientHelper

/**
 * @author VIJAY PATIDAR
 */
interface OnUserListRequestListener {
    fun onRequestUsers(): List<ClientHelper>
}