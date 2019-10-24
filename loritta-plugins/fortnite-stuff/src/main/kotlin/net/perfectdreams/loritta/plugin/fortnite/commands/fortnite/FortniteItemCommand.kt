package net.perfectdreams.loritta.plugin.fortnite.commands.fortnite

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.extensions.edit
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
import net.perfectdreams.loritta.utils.Emotes
import java.awt.Color

class FortniteItemCommand(val m: FortniteStuff) : LorittaDiscordCommand(arrayOf("fortniteitem", "fnitem"), CommandCategory.FORTNITE) {
	companion object {
		private val LOCALE_PREFIX = "commands.fortnite.item"
	}

	override fun getDescription(locale: BaseLocale) = locale["commands.fortnite.item.description"]

	override fun getUsage(locale: BaseLocale): CommandArguments {
		return arguments {
			this.argument(ArgumentType.TEXT) {}
		}
	}

	override fun getExamples(locale: BaseLocale): List<String> {
		return listOf(
				"Tsuki",
				"Glow",
				"Savor the W",
				"Jaywalking",
				"Kitsune",
				"13dfe12e98005d104710b724cafd26d42432ce81"
		)
	}

	@Subcommand
	suspend fun root(context: DiscordCommandContext, locale: BaseLocale) {
		if (context.args.isEmpty()) {
			context.explain()
			return
		}

		val name = context.args.joinToString(" ")

		val items = m.itemsInfo.values.flatMap {
			it.filter {
				it["itemId"].string == name || it["item"]["name"].nullString?.contains(name, true) == true
			}
		}.distinctBy { it["itemId"].string }

		val fortniteItemsInCurrentLocale = m.itemsInfo[locale["commands.fortnite.shop.localeId"]]!!

		val embed = EmbedBuilder()

		if (items.size == 1) {
			// Pegar na linguagem do usuário
			val fortniteItemInCurrentLocale = fortniteItemsInCurrentLocale.first { it["itemId"].string == items.first()["itemId"].string }

			context.sendMessage(
					displayItemInfo(fortniteItemInCurrentLocale, locale).build()
			)
		} else if (items.isNotEmpty()) {
			for (i in 0 until Math.min(9, items.size)) {
				val item = items[i].obj
				val fortniteItemInCurrentLocale = fortniteItemsInCurrentLocale.first { item["itemId"].string == it["itemId"].string }["item"].obj

				embed.setTitle("${Emotes.LORI_HM} ${locale["${LOCALE_PREFIX}.multipleItems"]}")
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

				result.handle.edit(context.getAsMention(true), displayItemInfo(fortniteItemInCurrentLocale, locale).build(), true)
			}

			// Adicionar os reactions
			for (i in 0 until Math.min(9, items.size)) {
				result.handle.addReaction(Constants.INDEXES[i]).queue()
			}
		} else {
			context.reply(
					LoriReply(
							locale["$LOCALE_PREFIX.unknownItem", "`${name.stripCodeMarks()}`"],
							Constants.ERROR
					)
			)
		}
	}

	fun displayItemInfo(item: JsonElement, locale: BaseLocale): EmbedBuilder {
		val fortniteItem = item["item"].obj
		val source = fortniteItem["source"].nullString
		val upcoming = fortniteItem["upcoming"].nullBool ?: true

		val embed = EmbedBuilder()
				.setTitle("${Emotes.DEFAULT_DANCE} ${fortniteItem["name"].nullString}")
				.setDescription(fortniteItem["description"].nullString)

		embed.addField("\uD83D\uDD16 ${locale["$LOCALE_PREFIX.type"]}", fortniteItem["typeName"].nullString, true)

		embed.addField("⭐ ${locale["$LOCALE_PREFIX.rarity"]}", fortniteItem["rarityName"].nullString, true)

		if (source != null) {
			val beautifulSource = when {
				// Passe de Batalha
				source.startsWith("battlepass") -> {
					val seasonTotal = source.substring((source.indexOf("battlepass (season") + "battlepass (season".length) until (source.length - 1)).toInt()

					val chapter = ((seasonTotal - 1) / 10) + 1
					val season = ((seasonTotal - 1) % 10) + 1

					locale["$LOCALE_PREFIX.battlePass", chapter, season]
				}
				source == "starterpack" -> {
					locale["$LOCALE_PREFIX.starterPack"]
				}
				source == "shop" -> {
					locale["$LOCALE_PREFIX.shop"]
				}
				source.startsWith("firstwin") -> {
					val seasonOrAnySeason = source.substring((source.indexOf("firstwin (") + "firstwin (".length) until (source.length - 1))

					if (seasonOrAnySeason == "anywin") {
						locale["$LOCALE_PREFIX.firstWinAny"]
					} else {
						val seasonTotal = seasonOrAnySeason.replace("season", "").toInt()
						val chapter = ((seasonTotal - 1)  / 10) + 1
						val season = ((seasonTotal - 1) % 10) + 1

						locale["$LOCALE_PREFIX.firstWin", chapter, season]
					}
				}
				source == "promo" -> {
					locale["$LOCALE_PREFIX.promo"]
				}
				else -> source
			}

			embed.addField("\uD83D\uDD0E ${locale["$LOCALE_PREFIX.source"]}", beautifulSource, true) // both name and value must be set
		}

		if (fortniteItem["costType"].nullString == "vbucks") {
			embed.addField("<:vbucks:635158614109192199> ${locale["$LOCALE_PREFIX.cost"]}", fortniteItem["cost"].nullInt.toString(), true)
		}

		embed.addField("\uD83D\uDE80 ${locale["$LOCALE_PREFIX.alreadyReleased"]}", locale["loritta.fancyBoolean.${!upcoming}"], true)

		val image = fortniteItem["images"]["background"].nullString

		embed.setThumbnail(image)
		embed.setColor(
				FortniteStuff.convertRarityToColor(fortniteItem["rarity"].nullString ?: "???")
		)

		embed.addField("\uD83D\uDCBB ID", "`${item["itemId"].string}`", true)

		return embed
	}
}