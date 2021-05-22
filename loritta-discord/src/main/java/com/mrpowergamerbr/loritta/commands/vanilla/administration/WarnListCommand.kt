package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Warn
import com.mrpowergamerbr.loritta.tables.Warns
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.utils.PunishmentAction
import org.jetbrains.exposed.sql.and

class WarnListCommand : AbstractCommand("punishmentlist", listOf("listadeavisos", "modlog", "modlogs", "infractions", "warnlist", "warns"), CommandCategory.MODERATION) {
	companion object {
		private val LOCALE_PREFIX = "commands.command"
	}

	override fun getDescriptionKey() = LocaleKeyData("$LOCALE_PREFIX.warnlist.description")
	override fun getExamplesKey() = LocaleKeyData("$LOCALE_PREFIX.warnlist.examples")

	override fun getUsage(): CommandArguments {
		return arguments {
			argument(ArgumentType.USER) {
				optional = false
			}
		}
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		val user = context.getUserAt(0)

		if (user != null) {
			val warns = loritta.newSuspendedTransaction {
				Warn.find { (Warns.guildId eq context.guild.idLong) and (Warns.userId eq user.idLong) }.sortedBy { it.receivedAt } .toMutableList()
			}

			if (warns.isEmpty()) {
				context.reply(
						context.locale["$LOCALE_PREFIX.warnlist.userDoesntHaveWarns", user.asMention],
						Constants.ERROR
				)
				return
			}

			val warnPunishments = AdminUtils.retrieveWarnPunishmentActions(context.config)

			val embed = EmbedBuilder().apply {
				setColor(Constants.DISCORD_BLURPLE)
				setAuthor(user.name, null, user.effectiveAvatarUrl)
				setTitle("\uD83D\uDE94 ${context.locale["$LOCALE_PREFIX.warnlist.title"]}")

				val warn = warns.size
				val nextPunishment = warnPunishments.firstOrNull { it.warnCount == warn + 1 }

				if (nextPunishment != null) {
					val type = when (nextPunishment.punishmentAction) {
						PunishmentAction.BAN -> context.locale["$LOCALE_PREFIX.ban.punishAction"]
						PunishmentAction.KICK -> context.locale["$LOCALE_PREFIX.kick.punishAction"]
						PunishmentAction.MUTE -> context.locale["$LOCALE_PREFIX.mute.punishAction"]
						else -> throw RuntimeException("Punishment $nextPunishment is not supported")
					}.toLowerCase()
					setFooter(context.locale["$LOCALE_PREFIX.warnlist.nextPunishment", type], null)
				}

				warns.forEachIndexed({ idx, warn -> 
					addField(
							context.locale["$LOCALE_PREFIX.warn.punishAction"],
							"""**${context.locale["$LOCALE_PREFIX.warnlist.common"]} #${idx + 1}**
								|**${context.locale["$LOCALE_PREFIX.ban.punishedBy"]}:** <@${warn.punishedById}>
								|**${context.locale["$LOCALE_PREFIX.ban.punishmentReason"]}:** ${warn.content}
								|**${context.locale["$LOCALE_PREFIX.warnlist.date"]}:** ${warn.receivedAt.humanize(locale)}
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