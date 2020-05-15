package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Warn
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Warns
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.utils.PunishmentAction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class WarnListCommand : AbstractCommand("punishmentlist", listOf("listadeavisos", "modlog", "modlogs", "infractions", "warnlist", "warns"), CommandCategory.ADMIN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["WARNLIST_Description"]
	}

	override fun getUsage(locale: LegacyBaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.USER) {
				optional = false
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

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
		val user = context.getUserAt(0)

		if (user != null) {
			val warns = transaction(Databases.loritta) {
				Warn.find { (Warns.guildId eq context.guild.idLong) and (Warns.userId eq user.idLong) }.sortedBy { it.receivedAt } .toMutableList()
			}

			if (warns.isEmpty()) {
				context.reply(
						locale["WARNLIST_UserDoesntHaveWarns", user.asMention],
						Constants.ERROR
				)
				return
			}

			val warnPunishments = AdminUtils.retrieveWarnPunishmentActions(context.config)

			val embed = EmbedBuilder().apply {
				setColor(Constants.DISCORD_BLURPLE)
				setAuthor(user.name, null, user.effectiveAvatarUrl)
				setTitle("\uD83D\uDE94 ${locale["WARN_Title"]}")

				val warn = warns.size
				val nextPunishment = warnPunishments.firstOrNull { it.warnCount == warn + 1 }

				if (nextPunishment != null) {
					val type = when (nextPunishment.punishmentAction) {
						PunishmentAction.BAN -> locale["BAN_PunishAction"]
						PunishmentAction.SOFT_BAN -> locale["SOFTBAN_PunishAction"]
						PunishmentAction.KICK -> locale["KICK_PunishAction"]
						PunishmentAction.MUTE -> locale["MUTE_PunishAction"]
						else -> throw RuntimeException("Punishment $nextPunishment is not supported")
					}.toLowerCase()
					setFooter(locale["WARN_NextPunishment", type], null)
				}

				warns.forEachIndexed({ idx, warn -> 
					addField(
							locale["WARN_PunishAction"],
							"""**${locale["WARN_Common"]} #${idx + 1}**
								|**${locale["BAN_PunishedBy"]}:** <@${warn.punishedById}>
								|**${locale["BAN_PunishmentReason"]}:** ${warn.content}
								|**${locale["KYM_DATE"]}:** ${warn.receivedAt.humanize(locale)}
							""".trimMargin(),
							false
					)
				})
			}

			val message = context.sendMessage(context.getAsMention(true), embed.build())

			/* message.onReactionAddByAuthor(context) {
				val idx = Constants.INDEXES.indexOf(it.reactionEmote.name)

				val warn = warns.getOrNull(idx)

				if (warn != null) {
					val punisher = lorittaShards.getUserById(warn.punishedById)
					val embed = EmbedBuilder().apply {
						setColor(Constants.DISCORD_BLURPLE)
						setAuthor(user.name, null, user.effectiveAvatarUrl)
						setTitle("\uD83D\uDE94 Aviso")
						if (punisher != null)
							setThumbnail(punisher.effectiveAvatarUrl)
						addField(
								locale["BAN_PunishedBy"],
								"<@${warn.punishedById}>",
								true
						)
						addField(
								locale["BAN_PunishmentReason"],
								"${warn.content}",
								true
						)
						addField(
								locale["KYM_DATE"],
								warn.receivedAt.humanize(locale),
								true
						)
					}

					val _message = message.edit(context.getAsMention(true), embed.build())
				}
			}

			for (i in 0 until warns.size) {
				message.addReaction(Constants.INDEXES[i]).queue()
			} */
		} else {
			this.explain(context)
		}
	}
}