package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.*
import com.mrpowergamerbr.loritta.utils.locale.*
import net.dv8tion.jda.core.*
import java.time.*

class PunishmentListCommand : AbstractCommand("punishmentlist", category=CommandCategory.ADMIN) {

    override fun getDescription(locale: BaseLocale): String {
        return locale["PUNISHMENTLIST_Description"]
    }

    override fun getExample(): List<String> {
        return listOf("159985870458322944")
    }

    override fun getDiscordPermissions(): List<Permission> {
        return listOf(Permission.KICK_MEMBERS)
    }

    override fun run(context: CommandContext, locale: BaseLocale) {
        val user = context.getUserAt(0)

        if (user != null) {
            val profile = context.config.getUserData(user.id)

            if (profile.punishments.isEmpty()) {
                context.reply(
                        locale["PUNISHMENTLIST_UserDoesntHavePunishments", user.asMention],
                        Constants.ERROR
                )
                return
            }

            val embed = EmbedBuilder().apply {
                setColor(Constants.DISCORD_BLURPLE)
            }

            profile.punishments.forEach {
                var expired = context.config.moderationConfig.warnExpiresIn
                embed.appendDescription("**${locale["BAN_PunishedBy"]}:** <@${it.author}>\n**${locale["BAN_PunishmentReason"]}:** ${it.reason}\n**${locale["KYM_DATE"]}:** ${OffsetDateTime.ofInstant(Instant.ofEpochMilli(it.punishmentTimestamp), ZoneId.systemDefault()).humanize(locale)}\n⸻\n")
            }

            context.sendMessage(context.getAsMention(true), embed.build())

        } else {
            this.explain(context)
        }
    }
}