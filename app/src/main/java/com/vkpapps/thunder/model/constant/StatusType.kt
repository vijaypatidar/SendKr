package com.vkpapps.thunder.model.constant

object StatusType {
    /**
     * initially assigned when file transferring is in pending queue or retry operation performed
     */
    const val STATUS_PENDING = 0

    /**
     * automatically assigned when transferring is in progress
     */
    const val STATUS_ONGOING = 3

    /**
     * automatically assigned when transferring get completed
     */
    const val STATUS_COMPLETED = 1

    /**
     * automatically assigned when transferring get failed
     */
    const val STATUS_FAILED = 2

    /**
     * pause file transferring
     */
    const val STATUS_PAUSE = 5

    /**
     * resend file from beginning
     */
    const val STATUS_RETRY = 7

    /**
     * resume file sharing to specific position
     */
    const val STATUS_RESUME = 8
}
