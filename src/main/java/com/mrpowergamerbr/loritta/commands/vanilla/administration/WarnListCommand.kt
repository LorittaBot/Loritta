package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.ModerationConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import java.awt.Color
import java.time.Instant

class WarnListCommand : AbstractCommand("warnlist", listOf("listadeavisos", "modlog", "modlogs"), CommandCategory.ADMIN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["WARNLIST_Description"]
	}

	override fun getExample(): List<String> {
		return listOf("159985870458322944")
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val user = LorittaUtils.getUserFromContext(context, 0)

		if (user != null) {
			val profile = context.config.getUserData(user.id)

			if (profile.warns.isEmpty()) {
				context.reply(
						locale["WARNLIST_UserDoesntHaveWarns", user.asMention],
						Constants.ERROR
				)
				return
			}

			val embed = EmbedBuilder().apply {
				setColor(Constants.DISCORD_BURPLE)
			}

			profile.warns.forEach {
				var expired = context.config.moderationConfig.warnExpiresIn
				embed.appendDescription("**${locale["BAN_PunishedBy"]}:** <@${it.punishedBy}>\n**${locale["BAN_PunishmentReason"]}:** ${it.reason}\n**${locale["KYM_DATE"]}:** ${it.time.humanize(locale)}\n⸻\n")
			}

			context.sendMessage(context.getAsMention(true), embed.build())
		} else {
			this.explain(context)
		}
	}
}