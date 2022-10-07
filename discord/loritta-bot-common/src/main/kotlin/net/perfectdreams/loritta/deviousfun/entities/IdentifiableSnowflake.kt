package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.Snowflake
import kotlinx.datetime.toJavaInstant
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import java.time.OffsetDateTime
import java.time.ZoneOffset

interface IdentifiableSnowflake {
    val idSnowflake: Snowflake
    val idLong: Long
        get() = idSnowflake.toLong()
    val id: String
        get() = idSnowflake.toString()
    val timeCreated: OffsetDateTime
        get() = idSnowflake.timestamp.toJavaInstant().atOffset(ZoneOffset.UTC)
}