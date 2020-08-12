package com.vkpapps.thunder.interfaces

import com.vkpapps.thunder.connection.ClientHelper

/***
 * @author VIJAY PATIDAR
 */


interface OnClientConnectionStateListener {
    fun onClientConnected(clientHelper: ClientHelper)
    fun onClientDisconnected(clientHelper: ClientHelper)
    fun onClientInformationChanged(clientHelper: ClientHelper)
}