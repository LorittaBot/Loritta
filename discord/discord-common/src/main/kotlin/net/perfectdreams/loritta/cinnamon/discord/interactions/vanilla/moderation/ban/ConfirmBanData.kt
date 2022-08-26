package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.ban

import dev.kord.common.entity.Snowflake
import dev.kord.core.cache.data.GuildData
import dev.kord.core.cache.data.MemberData
import dev.kord.core.cache.data.UserData
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.data.SingleUserComponentData

@Serializable
class ConfirmBanData(
    val reason: String?,
    val sendPunishmentViaDirectMessage: Boolean,
    val sendPunishmentToPunishmentLog: Boolean,
    val punishmentLogChannelId: Snowflake?,
    val guild: GuildData,
    val punisher: UserData,
    val users: List<UserWithMemberData>
) : SingleUserComponentData {
    override val userId = punisher.id

    @Serializable
    data class UserWithMemberData(
        val userData: UserData,
        val memberData: MemberData?
    )
}