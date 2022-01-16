package net.perfectdreams.loritta.cinnamon.platform.components

import dev.kord.common.entity.Snowflake
import net.perfectdreams.discordinteraktions.common.components.ComponentContext
import net.perfectdreams.discordinteraktions.common.entities.Member
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext as CinnamonComponentContext

class GuildComponentContext(
    loritta: LorittaCinnamon,
    i18nContext: I18nContext,
    user: User,
    interaKTionsContext: ComponentContext,
    val guildId: Snowflake,
    val member: Member
) : CinnamonComponentContext(loritta, i18nContext, user, interaKTionsContext)