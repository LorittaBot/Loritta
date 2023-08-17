package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class UserId(val value: ULong)

// I don't know why but you need to explictly set the return type here
// If not, IDEA complains that you are trying to use the "UserId(ULong)" version when passing a Long.
fun UserId(value: Long): UserId = UserId(value.toULong())