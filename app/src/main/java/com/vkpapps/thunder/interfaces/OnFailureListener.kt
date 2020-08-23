package com.vkpapps.thunder.interfaces

interface OnFailureListener<T> {
    fun onFailure(t: T)
}