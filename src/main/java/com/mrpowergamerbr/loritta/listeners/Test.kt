package com.mrpowergamerbr.loritta.listeners

import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.TimeUnit

val handledUsernameChanges = Caffeine.newBuilder().expireAfterWrite(15, TimeUnit.SECONDS).maximumSize(100)
        .build<String, Long>().asMap()

fun main(args: Array<String>) {
    handledUsernameChanges.put("Test", System.currentTimeMillis())

    handledUsernameChanges.remove("Test")
}