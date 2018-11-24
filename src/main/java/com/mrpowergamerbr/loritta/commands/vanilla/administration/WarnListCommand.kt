package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.dao.Warn
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Warns
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

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
			val warns = transaction(Databases.loritta) {
				Warn.find { (Warns.guildId eq context.guild.idLong) and (Warns.userId eq user.idLong) }.toMutableList()
			}

			if (warns.isEmpty()) {
				context.reply(
						locale["WARNLIST_UserDoesntHaveWarns", user.asMention],
						Constants.ERROR
				)
				return
			}

			val embed = EmbedBuilder().apply {
				setColor(Constants.DISCORD_BLURPLE)
			}

			warns.forEach {
				var expired = context.config.moderationConfig.warnExpiresIn
				embed.appendDescription("**${locale["BAN_PunishedBy"]}:** <@${it.punishedById}>\n**${locale["BAN_PunishmentReason"]}:** ${it.content}\n**${locale["KYM_DATE"]}:** ${it.receivedAt.humanize(locale)}\n⸻\n")
			}

			context.sendMessage(context.getAsMention(true), embed.build())
		} else {
			this.explain(context)
		}
	}
}