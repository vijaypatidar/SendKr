package com.vkpapps.sendkr.interfaces

import com.vkpapps.sendkr.connection.ClientHelper

/***
 * @author VIJAY PATIDAR
 */


interface OnClientConnectionStateListener {
    fun onClientConnected(clientHelper: ClientHelper)
    fun onClientDisconnected(clientHelper: ClientHelper)
    fun onClientInformationChanged(clientHelper: ClientHelper)
}