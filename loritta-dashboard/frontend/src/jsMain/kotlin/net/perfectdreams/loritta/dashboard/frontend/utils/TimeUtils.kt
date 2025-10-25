package net.perfectdreams.loritta.dashboard.frontend.utils

import js.date.Date

/**
 * Gets current system time in milliseconds since certain moment in the past, only delta between two subsequent calls makes sense.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.util.date.getTimeMillis)
 */
fun getTimeMillis(): Long = Date().getTime().toLong()
