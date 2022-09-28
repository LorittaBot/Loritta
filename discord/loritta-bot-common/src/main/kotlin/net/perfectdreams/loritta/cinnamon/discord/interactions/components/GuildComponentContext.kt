package net.perfectdreams.loritta.cinnamon.discord.interactions.components

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import net.perfectdreams.discordinteraktions.common.components.ComponentContext
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext as CinnamonComponentContext

class GuildComponentContext(
    loritta: LorittaBot,
    i18nContext: I18nContext,
    locale: BaseLocale,
    user: User,
    interaKTionsContext: ComponentContext,
    val guildId: Snowflake,
    val member: Member
) : CinnamonComponentContext(loritta, i18nContext, locale, user, interaKTionsContext)