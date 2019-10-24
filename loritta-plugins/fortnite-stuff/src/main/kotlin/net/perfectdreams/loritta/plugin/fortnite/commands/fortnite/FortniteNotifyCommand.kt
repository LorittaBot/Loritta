package net.perfectdreams.loritta.plugin.fortnite.commands.fortnite

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.platform.discord.commands.LorittaDiscordCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.plugin.fortnite.FortniteStuff
import net.perfectdreams.loritta.plugin.fortnite.extendedtables.TrackedFortniteItems
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color

class FortniteNotifyCommand(val m: FortniteStuff) : LorittaDiscordCommand(arrayOf("fortnitenotify", "fortnitenotificar", "fnnotify", "fnnotificar"), CommandCategory.FORTNITE) {
	companion object {
		private val LOCALE_PREFIX = "commands.fortnite.notify"
	}

	override fun getDescription(locale: BaseLocale) = locale["commands.fortnite.notify.description"]

	override fun getUsage(locale: BaseLocale): CommandArguments {
		return arguments {
			this.argument(ArgumentType.TEXT) {}
		}
	}

	override fun getExamples(locale: BaseLocale): List<String> {
		return listOf(
				"Skull Trooper",
				"Ghoul Trooper",
				"Tsuki",
				"Savor the W"
		)
	}

	@Subcommand
	suspend fun root(context: DiscordCommandContext, locale: BaseLocale) {
		val fortniteItemsInCurrentLocale = m.itemsInfo[locale["commands.fortnite.shop.localeId"]]!!

		if (context.args.isEmpty()) {
			val alreadyTracking = transaction(Databases.loritta) {
				TrackedFortniteItems.select {
					(TrackedFortniteItems.trackedBy eq context.lorittaUser.profile.id)
				}.toMutableList()
			}

			val embed = EmbedBuilder().setTitle("${Emotes.DEFAULT_DANCE} ${locale["$LOCALE_PREFIX.itemsThatYouWantToBeNotified"]}")

			for (tracked in alreadyTracking) {
				val item = fortniteItemsInCurrentLocale.first { tracked[TrackedFortniteItems.itemId] == it["itemId"].string }

				embed.appendDescription("**${item["item"]["name"].string}**\n")
			}

			embed.appendDescription("\n")
			embed.setColor(Color(0, 125, 187))
			embed.appendDescription(locale["$LOCALE_PREFIX.howToAddOrRemove", "`" + locale["$LOCALE_PREFIX.howToAddOrRemoveCommand", "${context.config.commandPrefix}"] + "`"])

			context.sendMessage(
					context.getAsMention(true),
					embed.build()
			)
			return
		}

		val name = context.args.joinToString(" ")

		val items = m.itemsInfo.values.flatMap {
			it.filter {
				it["itemId"].string == name || it["item"]["name"].nullString?.contains(name, true) == true
			}
		}.distinctBy { it["itemId"].string }

		val embed = EmbedBuilder()

		if (items.size == 1) {
			// Pegar na linguagem do usuário
			val fortniteItemInCurrentLocale = fortniteItemsInCurrentLocale.first { it["itemId"].string == items.first()["itemId"].string }

			trackItem(context, fortniteItemInCurrentLocale)
		} else if (items.isNotEmpty()) {
			for (i in 0 until Math.min(9, items.size)) {
				val item = items[i].obj
				val fortniteItemInCurrentLocale = fortniteItemsInCurrentLocale.first { item["itemId"].string == it["itemId"].string }["item"].obj

				embed.setTitle("${Emotes.LORI_HM} ${locale["commands.fortnite.item.multipleItems"]}")
				embed.setColor(Color(0, 125, 187))
				embed.appendDescription("${Constants.INDEXES[i]} ${fortniteItemInCurrentLocale["name"].nullString} (${fortniteItemInCurrentLocale["typeName"].nullString})\n")
			}

			val result = context.sendMessage(context.getAsMention(true), embed.build())

			result.handle.onReactionAddByAuthor(context) {
				val idx = Constants.INDEXES.indexOf(it.reactionEmote.name)

				// Caso seja uma reaçõa inválida ou que não tem no metadata, ignore!
				if (idx == -1 || (idx + 1) > items.size)
					return@onReactionAddByAuthor

				val item = items[idx]
				val fortniteItemInCurrentLocale = fortniteItemsInCurrentLocale.first { item["itemId"].string == it["itemId"].string }

				result.handle.delete().queue()

				trackItem(context, fortniteItemInCurrentLocale)
			}

			// Adicionar os reactions
			for (i in 0 until Math.min(9, items.size)) {
				result.handle.addReaction(Constants.INDEXES[i]).queue()
			}
		} else {
			context.reply(
					LoriReply(
							locale["commands.fortnite.item.unknownItem", "`${name.stripCodeMarks()}`"],
							Constants.ERROR
					)
			)
		}
	}

	suspend fun trackItem(context: DiscordCommandContext, item: JsonElement) {
		val itemWrapper = item["item"].obj
		val isUpcoming = itemWrapper["upcoming"].bool
		val itemName = itemWrapper["name"].string

		if ((itemWrapper["source"].nullString ?: "shop") == "shop") {
			val alreadyTracking = transaction(Databases.loritta) {
				TrackedFortniteItems.select {
					(TrackedFortniteItems.trackedBy eq context.lorittaUser.profile.id) and (TrackedFortniteItems.itemId eq item["itemId"].string)
				}.count() != 0
			}

			if (alreadyTracking) {
				transaction(Databases.loritta) {
					TrackedFortniteItems.deleteWhere {
						(TrackedFortniteItems.trackedBy eq context.lorittaUser.profile.id) and (TrackedFortniteItems.itemId eq item["itemId"].string)
					}
				}

				context.reply(
						LoriReply(
								context.locale["$LOCALE_PREFIX.removedTrack", itemName],
								Emotes.LORI_CRYING
						)
				)
				return
			}

			transaction(Databases.loritta) {
				TrackedFortniteItems.insert {
					it[trackedBy] = context.lorittaUser.profile.id
					it[itemId] = item["itemId"].string
				}
			}

			val replyList = mutableListOf(
					LoriReply(
							context.locale["$LOCALE_PREFIX.trackingSuccess", itemName],
							Emotes.LORI_HAPPY
					)
			)

			if (isUpcoming) {
				replyList.add(
						LoriReply(
								context.locale["$LOCALE_PREFIX.upcomingItem"],
								Emotes.LORI_HM
						)
				)
			}

			context.reply(
					*replyList.toTypedArray()
			)
		} else {
			context.reply(
					LoriReply(
							context.locale["$LOCALE_PREFIX.notInItemShop"],
							Constants.ERROR
					)
			)
		}
	}
}