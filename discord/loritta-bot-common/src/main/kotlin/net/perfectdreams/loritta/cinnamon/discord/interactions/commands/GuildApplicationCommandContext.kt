package net.perfectdreams.loritta.cinnamon.discord.interactions.commands

import dev.kord.common.entity.Snowflake
import net.perfectdreams.discordinteraktions.common.commands.GuildApplicationCommandContext
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.LorittaBot

class GuildApplicationCommandContext(
    loritta: LorittaBot,
    i18nContext: I18nContext,
    user: User,
    override val interaKTionsContext: GuildApplicationCommandContext,
    val guildId: Snowflake,
    val member: Member
) : ApplicationCommandContext(loritta, i18nContext, user, interaKTionsContext)