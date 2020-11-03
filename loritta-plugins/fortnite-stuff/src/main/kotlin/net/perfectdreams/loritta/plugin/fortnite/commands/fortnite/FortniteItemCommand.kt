package net.perfectdreams.loritta.plugin.fortnite.commands.fortnite

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.edit
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.fortnite.FortniteStuff
import net.perfectdreams.loritta.utils.Emotes
import java.awt.Color

class FortniteItemCommand(val m: FortniteStuff) : DiscordAbstractCommandBase(m.loritta, listOf("fortniteitem", "fnitem"), CommandCategory.FORTNITE) {
	private val LOCALE_PREFIX = "commands.fortnite.item"

	override fun command() = create {
		localizedDescription("${LOCALE_PREFIX}.description")
		needsToUploadFiles = true

		usage {
			argument(ArgumentType.TEXT) {}
		}

		examples {
			+ "Tsuki"
			+ "Glow"
			+ "Savor the W"
			+ "Jaywalking"
			+ "Kitsune"
			+ "13dfe12e98005d104710b724cafd26d42432ce81"
		}

		executesDiscord {
			if (args.isEmpty())
				explainAndExit()

			val name = args.joinToString(" ")

			val items = m.itemsInfo.values.flatMap {
				it.filter {
					it["id"].string == name || it["name"].nullString?.contains(name, true) == true
				}
			}.distinctBy { it["id"].string }

			println("ITEMS INFO")
			m.itemsInfo.forEach {
				println(it.key)
			}

			val fortniteItemsInCurrentLocale = m.itemsInfo[locale["commands.fortnite.shop.localeId"]]!!

			val embed = EmbedBuilder()

			if (items.size == 1) {
				// Pegar na linguagem do usuário
				val fortniteItemInCurrentLocale = fortniteItemsInCurrentLocale.first { it["id"].string == items.first()["id"].string }

				sendMessage(
						displayItemInfo(fortniteItemInCurrentLocale, locale).build()
				)
			} else if (items.isNotEmpty()) {
				for (i in 0 until Math.min(9, items.size)) {
					val item = items[i].obj
					val fortniteItemInCurrentLocale = fortniteItemsInCurrentLocale.first { item["id"].string == it["id"].string }.obj

					embed.setTitle("${Emotes.LORI_HM} ${locale["$LOCALE_PREFIX.multipleItems"]}")
					embed.setColor(Color(0, 125, 187))
					embed.appendDescription("${Constants.INDEXES[i]} ${fortniteItemInCurrentLocale["name"].nullString} (${fortniteItemInCurrentLocale["type"]["value"].nullString})\n")
				}

				val result = sendMessage(embed.build())

				result.onReactionAddByAuthor(this) {
					val idx = Constants.INDEXES.indexOf(it.reactionEmote.name)

					// Caso seja uma reaçõa inválida ou que não tem no metadata, ignore!
					if (idx == -1 || (idx + 1) > items.size)
						return@onReactionAddByAuthor

					val item = items[idx]
					val fortniteItemInCurrentLocale = fortniteItemsInCurrentLocale.first { item["id"].string == it["id"].string }

					result.edit(getUserMention(true), displayItemInfo(fortniteItemInCurrentLocale, locale).build(), true)
				}

				// Adicionar os reactions
				for (i in 0 until Math.min(9, items.size)) {
					result.addReaction(Constants.INDEXES[i]).queue()
				}
			} else {
				reply(
						LorittaReply(
								locale["$LOCALE_PREFIX.unknownItem", "`${name.stripCodeMarks()}`"],
								Constants.ERROR
						)
				)
			}
		}
	}

	fun displayItemInfo(item: JsonElement, locale: BaseLocale): EmbedBuilder {
		val fortniteItem = item.obj
		val source: String? = null // fortniteItem["source"].nullString
		val upcoming = false // fortniteItem["upcoming"].nullBool ?: true

		val embed = EmbedBuilder()
				.setTitle("${Emotes.DEFAULT_DANCE} ${fortniteItem["name"].nullString}")
				.setDescription(fortniteItem["description"].nullString)

		embed.addField("\uD83D\uDD16 ${locale["${LOCALE_PREFIX}.type"]}", fortniteItem["type"]["displayValue"].nullString, true)

		embed.addField("⭐ ${locale["${LOCALE_PREFIX}.rarity"]}", fortniteItem["rarity"]["displayValue"].nullString, true)

		if (source != null) {
			val beautifulSource = when {
				// Passe de Batalha
				source.startsWith("battlepass") -> {
					val seasonTotal = source.substring((source.indexOf("battlepass (season") + "battlepass (season".length) until (source.length - 1)).toInt()

					val chapter = ((seasonTotal - 1) / 10) + 1
					val season = ((seasonTotal - 1) % 10) + 1

					locale["${LOCALE_PREFIX}.battlePass", chapter, season]
				}
				source == "starterpack" -> {
					locale["${LOCALE_PREFIX}.starterPack"]
				}
				source == "shop" -> {
					locale["${LOCALE_PREFIX}.shop"]
				}
				source.startsWith("firstwin") -> {
					val seasonOrAnySeason = source.substring((source.indexOf("firstwin (") + "firstwin (".length) until (source.length - 1))

					if (seasonOrAnySeason == "anywin") {
						locale["${LOCALE_PREFIX}.firstWinAny"]
					} else {
						val seasonTotal = seasonOrAnySeason.replace("season", "").toInt()
						val chapter = ((seasonTotal - 1)  / 10) + 1
						val season = ((seasonTotal - 1) % 10) + 1

						locale["${LOCALE_PREFIX}.firstWin", chapter, season]
					}
				}
				source == "promo" -> {
					locale["${LOCALE_PREFIX}.promo"]
				}
				else -> source
			}

			embed.addField("\uD83D\uDD0E ${locale["${LOCALE_PREFIX}.source"]}", beautifulSource, true) // both name and value must be set
		}

		/* if (fortniteItem["costType"].nullString == "vbucks") {
			embed.addField("<:vbucks:635158614109192199> ${locale["${LOCALE_PREFIX}.cost"]}", fortniteItem["cost"].nullInt.toString(), true)
		} */

		embed.addField("\uD83D\uDE80 ${locale["${LOCALE_PREFIX}.alreadyReleased"]}", locale["loritta.fancyBoolean.${!upcoming}"], true)

		val image = fortniteItem["images"]["icon"].nullString

		embed.setThumbnail(image)
		embed.setColor(
				FortniteStuff.convertRarityToColor(fortniteItem["rarity"]["value"].nullString ?: "???")
		)

		embed.addField("\uD83D\uDCBB ID", "`${item["id"].string}`", true)

		return embed
	}
}