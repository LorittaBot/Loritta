package net.perfectdreams.loritta.deviouscache.data

import dev.kord.common.entity.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class DeviousMemberData(
    val nick: String?,
    val roles: List<LightweightSnowflake>,
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
            data.roles.map { it.toLightweightSnowflake() },
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
            data.roles.map { it.toLightweightSnowflake() },
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
            data.roles.map { it.toLightweightSnowflake() },
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