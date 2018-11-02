package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission

class WarnListCommand : AbstractCommand("warnlist", listOf("listadeavisos", "modlog", "modlogs"), CommandCategory.ADMIN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["WARNLIST_Description"]
	}

	override fun getUsage(locale: BaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.USER) {
				optional = false
			}
			argument(ArgumentType.TEXT) {
				optional = true
			}
		}
	}

	override fun getExamples(): List<String> {
		return listOf("159985870458322944")
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val user = context.getUserAt(0)

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
				setColor(Constants.DISCORD_BLURPLE)
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