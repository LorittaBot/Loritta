package net.perfectdreams.loritta.plugin.fortnite.commands.fortnite

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordCommandContext
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
	private val LOCALE_PREFIX = "commands.command.fnnotify"

	override fun command() = create {
		localizedDescription("${LOCALE_PREFIX}.description")
		localizedExamples("commands.category.fortnite.itemsExamples")

		needsToUploadFiles = true

		usage {
			argument(ArgumentType.TEXT) {}
		}

		executesDiscord {
			val fortniteItemsInCurrentLocale = m.itemsInfo[locale["commands.command.fnshop.localeId"]]!!

			if (args.isEmpty()) {
				val alreadyTracking = transaction(Databases.loritta) {
					TrackedFortniteItems.select {
						(TrackedFortniteItems.trackedBy eq lorittaUser.profile.id)
					}.toMutableList()
				}

				val embed = EmbedBuilder().setTitle("${Emotes.DEFAULT_DANCE} ${locale["${LOCALE_PREFIX}.itemsThatYouWantToBeNotified"]}")

				for (tracked in alreadyTracking) {
					val item = fortniteItemsInCurrentLocale.firstOrNull { tracked[TrackedFortniteItems.itemId] == it["id"].string } ?: continue

					embed.appendDescription("**${item["name"].string}**\n")
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

			FortniteStuff.getFortniteItemByName(
					m,
					locale,
					this,
					name,
					onSuccess = { element, message ->
						message?.delete()?.queue()

						trackItem(this, element)
					},
					onFailure = {
						reply(
								LorittaReply(
										locale["commands.command.fnitem.unknownItem", "`${name.stripCodeMarks()}`"],
										Constants.ERROR
								)
						)
					}
			)
		}
	}

	suspend fun trackItem(context: DiscordCommandContext, item: JsonElement) {
		val itemWrapper = item.obj
		val itemName = itemWrapper["name"].string
		val source = itemWrapper["gameplayTags"].nullArray?.firstOrNull { it.string.startsWith("Cosmetics.Source") }
				?.string

		if (source == "Cosmetics.Source.ItemShop") {
			val alreadyTracking = loritta.newSuspendedTransaction {
				TrackedFortniteItems.select {
					(TrackedFortniteItems.trackedBy eq context.lorittaUser.profile.id) and (TrackedFortniteItems.itemId eq item["id"].string)
				}.count() != 0L
			}

			if (alreadyTracking) {
				loritta.newSuspendedTransaction {
					TrackedFortniteItems.deleteWhere {
						(TrackedFortniteItems.trackedBy eq context.lorittaUser.profile.id) and (TrackedFortniteItems.itemId eq item["id"].string)
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

			loritta.newSuspendedTransaction {
				TrackedFortniteItems.insert {
					it[trackedBy] = context.lorittaUser.profile.id
					it[itemId] = item["id"].string
				}
			}

			val replyList = mutableListOf(
					LorittaReply(
							context.locale["${LOCALE_PREFIX}.trackingSuccess", itemName],
							Emotes.LORI_HAPPY
					)
			)

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