package net.perfectdreams.loritta.cinnamon.pudding.entities

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.GuildProfile

class PuddingGuildProfile(
    private val pudding: Pudding,
    val data: GuildProfile
) {
    companion object;

    val userId by data::userId
    val guildId by data::guildId
    val xp by data::xp
}