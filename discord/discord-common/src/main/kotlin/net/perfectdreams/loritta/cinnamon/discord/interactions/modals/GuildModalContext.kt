package net.perfectdreams.loritta.cinnamon.discord.interactions.modals

import dev.kord.common.entity.Snowflake
import net.perfectdreams.discordinteraktions.common.entities.InteractionMember
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon

open class GuildModalContext(
    loritta: LorittaCinnamon,
    i18nContext: I18nContext,
    user: User,
    interaKTionsContext: net.perfectdreams.discordinteraktions.common.modals.GuildModalContext,
    val guildId: Snowflake,
    val member: InteractionMember
) : ModalContext(loritta, i18nContext, user, interaKTionsContext)