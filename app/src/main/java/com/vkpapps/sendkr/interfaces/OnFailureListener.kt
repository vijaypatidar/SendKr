package com.vkpapps.sendkr.interfaces

interface OnFailureListener<T> {
    fun onFailure(t: T)
}