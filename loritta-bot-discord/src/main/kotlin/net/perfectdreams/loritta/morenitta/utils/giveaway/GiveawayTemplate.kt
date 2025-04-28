package net.perfectdreams.loritta.morenitta.utils.giveaway

import kotlinx.serialization.Serializable

@Serializable
data class GiveawayTemplate(
    val name: String,
    val description: String,
    val imageUrl: String?,
    val thumbnailUrl: String?,
    val color: Int,
    val duration: String,
    val reaction: String,
    val channelId: Long?,
    val numberOfWinners: Int,
    val roleIds: List<Long>,
    val allowedRoleIds: List<Long>,
    val allowedRolesIsAndCondition: Boolean,
    val deniedRoleIds: List<Long>,
    val deniedRolesIsAndCondition: Boolean,
    val needsToGetDailyBeforeParticipating: Boolean,
    val extraEntries: List<GiveawayRoleExtraEntry>,
    val extraEntriesShouldStack: Boolean = false
) {
    @Serializable
    data class GiveawayRoleExtraEntry(
        val roleId: Long,
        val weight: Int
    )
}