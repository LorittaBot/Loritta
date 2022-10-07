package net.perfectdreams.loritta.deviousfun.cache

import dev.kord.common.entity.*
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.OptionalBoolean
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviousMemberData(
    val nick: String?,
    val roles: List<Snowflake>,
    val joinedAt: Instant,
    val premiumSince: Instant?,
    // These are nullable because they aren't present in the DiscordUpdatedGuildMember!
    val deaf: Boolean?,
    val mute: Boolean?,
    val pending: Boolean,
    val avatar: String?,
    val communicationDisabledUntil: Instant?
) {
    companion object {
        fun from(data: DiscordGuildMember) = DeviousMemberData(
            data.nick.value,
            data.roles,
            data.joinedAt,
            data.premiumSince.value,
            data.deaf.discordBoolean,
            data.mute.discordBoolean,
            data.pending.discordBoolean,
            data.avatar.value,
            data.communicationDisabledUntil.value
        )

        fun from(data: DiscordAddedGuildMember) = DeviousMemberData(
            data.nick.value,
            data.roles,
            data.joinedAt,
            data.premiumSince.value,
            data.deaf,
            data.mute,
            data.pending.discordBoolean,
            data.avatar.value,
            data.communicationDisabledUntil.value
        )

        fun from(data: DiscordUpdatedGuildMember, oldData: DeviousMemberData?) = DeviousMemberData(
            data.nick.value,
            data.roles,
            data.joinedAt,
            data.premiumSince.value,
            // If the old data is present, we will use it, if not, keep it as null
            oldData?.deaf,
            oldData?.mute,
            data.pending.discordBoolean,
            data.avatar.value,
            data.communicationDisabledUntil.value
        )
    }
}