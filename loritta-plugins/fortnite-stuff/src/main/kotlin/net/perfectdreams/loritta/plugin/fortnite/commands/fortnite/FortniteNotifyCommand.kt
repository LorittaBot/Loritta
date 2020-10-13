package net.perfectdreams.loritta.plugin.fortnite.commands.fortnite

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandContext
import net.perfectdreams.loritta.plugin.fortnite.FortniteStuff
import net.perfectdreams.loritta.plugin.fortnite.tables.TrackedFortniteItems
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color

class FortniteNotifyCommand(val m: FortniteStuff) : DiscordAbstractCommandBase(m.loritta, listOf("fnnotify", "fortnitenotify", "fnnotificar", "fortnitenotificar"), CommandCategory.FORTNITE) {
	private val LOCALE_PREFIX = "commands.fortnite.notify"

	override fun command() = create {
		localizedDescription("${LOCALE_PREFIX}.description")
		needsToUploadFiles = true

		usage {
			argument(ArgumentType.TEXT) {}
		}

		examples {
			+ "Skull Trooper"
			+ "Ghoul Trooper"
			+ "Tsuki"
			+ "Savor the W"
		}

		executesDiscord {
			val fortniteItemsInCurrentLocale = m.itemsInfo[locale["commands.fortnite.shop.localeId"]]!!

			if (args.isEmpty()) {
				val alreadyTracking = transaction(Databases.loritta) {
					TrackedFortniteItems.select {
						(TrackedFortniteItems.trackedBy eq lorittaUser.profile.id)
					}.toMutableList()
				}

				val embed = EmbedBuilder().setTitle("${Emotes.DEFAULT_DANCE} ${locale["${LOCALE_PREFIX}.itemsThatYouWantToBeNotified"]}")

				for (tracked in alreadyTracking) {
					val item = fortniteItemsInCurrentLocale.firstOrNull { tracked[TrackedFortniteItems.itemId] == it["itemId"].string } ?: continue

					embed.appendDescription("**${item["item"]["name"].string}**\n")
				}

				embed.appendDescription("\n")
				embed.setColor(Color(0, 125, 187))
				embed.appendDescription(locale["${LOCALE_PREFIX}.howToAddOrRemove", "`" + locale["${LOCALE_PREFIX}.howToAddOrRemoveCommand", serverConfig.commandPrefix] + "`"])

				sendMessage(
						getUserMention(true),
						embed.build()
				)
				return@executesDiscord
			}

			val name = args.joinToString(" ")

			val items = m.itemsInfo.values.flatMap {
				it.filter {
					it["itemId"].string == name || it["item"]["name"].nullString?.contains(name, true) == true
				}
			}.distinctBy { it["itemId"].string }

			val embed = EmbedBuilder()

			if (items.size == 1) {
				// Pegar na linguagem do usuário
				val fortniteItemInCurrentLocale = fortniteItemsInCurrentLocale.first { it["itemId"].string == items.first()["itemId"].string }

				trackItem(this, fortniteItemInCurrentLocale)
			} else if (items.isNotEmpty()) {
				for (i in 0 until Math.min(9, items.size)) {
					val item = items[i].obj
					val fortniteItemInCurrentLocale = fortniteItemsInCurrentLocale.first { item["itemId"].string == it["itemId"].string }["item"].obj

					embed.setTitle("${Emotes.LORI_HM} ${locale["commands.fortnite.item.multipleItems"]}")
					embed.setColor(Color(0, 125, 187))
					embed.appendDescription("${Constants.INDEXES[i]} ${fortniteItemInCurrentLocale["name"].nullString} (${fortniteItemInCurrentLocale["typeName"].nullString})\n")
				}

				val result = sendMessage(getUserMention(true), embed.build())

				result.onReactionAddByAuthor(this) {
					val idx = Constants.INDEXES.indexOf(it.reactionEmote.name)

					// Caso seja uma reaçõa inválida ou que não tem no metadata, ignore!
					if (idx == -1 || (idx + 1) > items.size)
						return@onReactionAddByAuthor

					val item = items[idx]
					val fortniteItemInCurrentLocale = fortniteItemsInCurrentLocale.first { item["itemId"].string == it["itemId"].string }

					result.delete().queue()

					trackItem(this, fortniteItemInCurrentLocale)
				}

				// Adicionar os reactions
				for (i in 0 until Math.min(9, items.size)) {
					result.addReaction(Constants.INDEXES[i]).queue()
				}
			} else {
				reply(
						LorittaReply(
								locale["commands.fortnite.item.unknownItem", "`${name.stripCodeMarks()}`"],
								Constants.ERROR
						)
				)
			}
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
				}.count() != 0L
			}

			if (alreadyTracking) {
				transaction(Databases.loritta) {
					TrackedFortniteItems.deleteWhere {
						(TrackedFortniteItems.trackedBy eq context.lorittaUser.profile.id) and (TrackedFortniteItems.itemId eq item["itemId"].string)
					}
				}

				context.reply(
						LorittaReply(
								context.locale["${LOCALE_PREFIX}.removedTrack", itemName],
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
					LorittaReply(
							context.locale["${LOCALE_PREFIX}.trackingSuccess", itemName],
							Emotes.LORI_HAPPY
					)
			)

			if (isUpcoming) {
				replyList.add(
						LorittaReply(
								context.locale["${LOCALE_PREFIX}.upcomingItem"],
								Emotes.LORI_HM
						)
				)
			}

			context.reply(
					*replyList.toTypedArray()
			)
		} else {
			context.reply(
					LorittaReply(
							context.locale["${LOCALE_PREFIX}.notInItemShop"],
							Constants.ERROR
					)
			)
		}
	}
}