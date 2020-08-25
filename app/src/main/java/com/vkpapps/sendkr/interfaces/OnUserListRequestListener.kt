package com.vkpapps.sendkr.interfaces

import com.vkpapps.sendkr.connection.ClientHelper

/**
 * @author VIJAY PATIDAR
 */
interface OnUserListRequestListener {
    fun onRequestUsers(): List<ClientHelper>
}