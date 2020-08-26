package com.vkpapps.sendkr.utils

import java.math.BigInteger
import java.security.MessageDigest
import kotlin.random.Random

/***
 * @author VIJAY PATIDAR
 */
object HashUtils {
    fun getHashValue(input: ByteArray): String {
        val digest: MessageDigest = MessageDigest.getInstance("MD5")
        val bytes = digest.digest(input)
        val bigInteger = BigInteger(1, bytes)
        return bigInteger.toString(16).trim()
    }

    fun getRandomId(length: Int = 30): String {
        return getHashValue(Random.nextBytes(length))
    }
}