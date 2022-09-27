package net.perfectdreams.loritta.cinnamon.discord.utils.profiles

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingUserProfile
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.awt.image.BufferedImage

abstract class StaticProfileCreator(loritta: LorittaBot, internalName: String) : ProfileCreator(loritta, internalName) {
    abstract suspend fun create(
        sender: ProfileUserInfoData,
        user: ProfileUserInfoData,
        userProfile: PuddingUserProfile,
        guild: Guild?,
        badges: List<BufferedImage>,
        locale: I18nContext,
        background: BufferedImage,
        aboutMe: String,
        allowedDiscordEmojis: List<Snowflake>?
    ): BufferedImage
}