package net.perfectdreams.loritta.cinnamon.discord.interactions.modals

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot

open class GuildModalContext(
    loritta: LorittaBot,
    i18nContext: I18nContext,
    locale: BaseLocale,
    user: User,
    interaKTionsContext: net.perfectdreams.discordinteraktions.common.modals.GuildModalContext,
    val guildId: Snowflake,
    val member: Member
) : ModalContext(loritta, i18nContext, locale, user, interaKTionsContext)