package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class UserId(val value: ULong)

fun UserId(value: Long) = UserId(value.toULong())