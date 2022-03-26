package net.perfectdreams.loritta.cinnamon.platform.modals

import dev.kord.common.entity.Snowflake
import net.perfectdreams.discordinteraktions.common.entities.InteractionMember
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.discordinteraktions.common.modals.GuildModalSubmitContext
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon

open class GuildModalSubmitContext(
    loritta: LorittaCinnamon,
    i18nContext: I18nContext,
    user: User,
    interaKTionsContext: GuildModalSubmitContext,
    val guildId: Snowflake,
    val member: InteractionMember
) : ModalSubmitContext(loritta, i18nContext, user, interaKTionsContext)