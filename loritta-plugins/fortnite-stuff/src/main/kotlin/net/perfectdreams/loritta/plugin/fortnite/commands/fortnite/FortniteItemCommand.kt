package net.perfectdreams.loritta.plugin.fortnite.commands.fortnite

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.nullArray
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonElement
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.edit
import net.perfectdreams.loritta.common.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.fortnite.FortniteStuff
import net.perfectdreams.loritta.utils.Emotes

class FortniteItemCommand(val m: FortniteStuff) : DiscordAbstractCommandBase(m.loritta, listOf("fortniteitem", "fnitem"), CommandCategory.FORTNITE) {
	private val LOCALE_PREFIX = "commands.command.fnitem"

	override fun command() = create {
		localizedDescription("${LOCALE_PREFIX}.description")
		localizedExamples("commands.category.fortnite.itemsExamples")

		needsToUploadFiles = true

		usage {
			argument(ArgumentType.TEXT) {}
		}

		executesDiscord {
			if (args.isEmpty())
				explainAndExit()

			val name = args.joinToString(" ")

			FortniteStuff.getFortniteItemByName(
					m,
					locale,
					this,
					name,
					onSuccess = { element, message ->
						if (message == null) {
							sendMessage(
									displayItemInfo(element, locale).build()
							)
						} else {
							message.edit(getUserMention(true), displayItemInfo(element, locale).build(), true)
						}
					},
					onFailure = {
						reply(
								LorittaReply(
										locale["$LOCALE_PREFIX.unknownItem", "`${name.stripCodeMarks()}`"],
										Constants.ERROR
								)
						)
					}
			)
		}
	}

	private fun displayItemInfo(item: JsonElement, locale: BaseLocale): EmbedBuilder {
		val fortniteItem = item.obj
		val source = fortniteItem["gameplayTags"].nullArray?.firstOrNull { it.string.startsWith("Cosmetics.Source") }
				?.string

		val embed = EmbedBuilder()
				.setTitle("${Emotes.DEFAULT_DANCE} ${fortniteItem["name"].nullString}")
				.setDescription(fortniteItem["description"].nullString)

		embed.addField("\uD83D\uDD16 ${locale["${LOCALE_PREFIX}.type"]}", fortniteItem["type"]["displayValue"].nullString, true)

		embed.addField("⭐ ${locale["${LOCALE_PREFIX}.rarity"]}", fortniteItem["rarity"]["displayValue"].nullString, true)

		if (source != null) {
			val splitByDot = source.split(".")
			// Examples:
			// "Cosmetics.Source.StarterPack"
			// "Cosmetics.Source.ItemShop"
			// "Cosmetics.Source.Season4.FirstWin"
			val arg0 = splitByDot.getOrNull(0)
			val arg1 = splitByDot.getOrNull(1)
			val arg2 = splitByDot.getOrNull(2)
			val arg3 = splitByDot.getOrNull(3)

			val beautifulSource = when {
				// Battle Pass
				arg3 == "BattlePass" && arg2 != null -> {
					val seasonTotal = arg2.removePrefix("Season").toInt()

					val chapter = ((seasonTotal - 1) / 10) + 1
					val season = ((seasonTotal - 1) % 10) + 1

					locale["${LOCALE_PREFIX}.battlePass", chapter, season]
				}
				// Starter Pack
				arg2 == "StarterPack" -> {
					locale["${LOCALE_PREFIX}.starterPack"]
				}
				// Item Shop
				arg2 == "ItemShop" -> {
					locale["${LOCALE_PREFIX}.shop"]
				}
				// First Win Stuff
				arg3 == "FirstWin" && arg2 != null -> {
					if (arg2 == "AnySeason") {
						locale["${LOCALE_PREFIX}.firstWinAny"]
					} else {
						val seasonTotal = arg2.removePrefix("Season").toInt()
						val chapter = ((seasonTotal - 1)  / 10) + 1
						val season = ((seasonTotal - 1) % 10) + 1

						locale["${LOCALE_PREFIX}.firstWin", chapter, season]
					}
				}
				// Promotion
				source == "Cosmetics.Source.Promo" -> {
					locale["${LOCALE_PREFIX}.promo"]
				}
				else -> source
			}

			embed.addField("\uD83D\uDD0E ${locale["${LOCALE_PREFIX}.source"]}", beautifulSource, true) // both name and value must be set
		}

		val image = fortniteItem["images"]["icon"].nullString

		embed.setThumbnail(image)
		embed.setColor(
				FortniteStuff.convertRarityToColor(fortniteItem["rarity"]["value"].nullString ?: "???")
		)

		embed.addField("\uD83D\uDCBB ID", "`${item["id"].string}`", true)

		return embed
	}
}