package net.perfectdreams.loritta.plugin.fortnite.commands.fortnite

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.platform.discord.commands.LorittaDiscordCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.plugin.fortnite.FortniteStuff
import java.awt.Color
import java.awt.FontMetrics
import java.awt.Rectangle
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

class FortniteStatsCommand(val m: FortniteStuff) : LorittaDiscordCommand(arrayOf("fortnitestats", "fnstats", "fortniteprofile", "fnprofile"), CommandCategory.FORTNITE) {
	companion object {
		private val LOCALE_PREFIX = "commands.fortnite.stats"
	}

	override val needsToUploadFiles: Boolean
		get() = true

	override fun getUsage(locale: BaseLocale): CommandArguments {
		return arguments {
			this.argument(ArgumentType.TEXT) {}
		}
	}

	override fun getExamples(locale: BaseLocale): List<String> {
		return listOf(
				"Ninja",
				"SypherPK",
				"MrPowerGamerBR"
		)
	}

	override fun getDescription(locale: BaseLocale) = locale["$LOCALE_PREFIX.description"]

	@Subcommand
	suspend fun root(context: DiscordCommandContext, locale: BaseLocale) {
		if (context.args.isEmpty()) {
			context.explain()
			return
		}

		val arg0 = context.args.joinToString(" ")

		val payloadV2 = HttpRequest.get("https://fnapi.me/api/stats2v/username?name=${arg0?.encodeToUrl()}")
				.header("Authorization", com.mrpowergamerbr.loritta.utils.loritta.config.fortniteApi.token)
				.body()

		val statsV2 = jsonParser.parse(payloadV2).obj

		if (statsV2["error"].nullString != null) {
			context.reply(
					LoriReply(
							locale["$LOCALE_PREFIX.unknownPlayer", "`${arg0.stripCodeMarks()}`"],
							Constants.ERROR
					)
			)
			return
		}
		val dataV2 = statsV2["data"].obj

		val inputTypesV2 = listOf(
				"mouseAndKeyboard",
				"touch",
				"controller"
		)

		var wins = 0
		var matchesPlayed = 0
		var totalKills = 0
		var totalMinutesPlayed = 0

		val foundGameModes = mutableMapOf<String, GameMode>()

		for (inputType in inputTypesV2) {
			fun fillGameModeInfo(name: String, obj: JsonObject, fillGlobal: Boolean) {
				if (fillGlobal) {
					wins += obj["placeTop1"].nullInt ?: 0
					matchesPlayed += obj["matchesPlayed"].nullInt ?: 0
					totalKills += obj["kills"].nullInt ?: 0
				}

				val gameMode = foundGameModes.getOrPut(name, { GameMode() })
				gameMode.matchesPlayed += obj["matchesPlayed"].nullInt ?: 0
				gameMode.placeTop1 += obj["placeTop1"].nullInt ?: 0
				gameMode.kills += obj["kills"].nullInt ?: 0
				gameMode.score += obj["score"].nullInt ?: 0
			}

			val inputTypeWrapper = dataV2[inputType].nullObj ?: continue

			val gameModes = inputTypeWrapper["gameMode"].nullObj ?: continue

			val defaultSolo = gameModes["defaultsolo"].nullObj
			val defaultDuo = gameModes["defaultduo"].nullObj
			val defaultSquad = gameModes["defaultsquad"].nullObj
			val showdownAlt = gameModes["showdownalt"].nullObj
			val respawn = gameModes["respawn"].nullObj

			if (respawn != null) {
				val teamRumble = respawn["24"].nullObj

				if (teamRumble != null) {
					fillGameModeInfo("teamrumble", teamRumble, false)
				}
			}

			if (defaultSolo != null)
				fillGameModeInfo("defaultsolo", defaultSolo, true)

			if (defaultDuo != null)
				fillGameModeInfo("defaultduo", defaultDuo, true)

			if (defaultSquad != null)
				fillGameModeInfo("defaultsquad", defaultSquad, true)

			if (showdownAlt != null) {
				val defaultSolo = showdownAlt["solo"].nullObj
				val defaultDuo = showdownAlt["duos"].nullObj
				val defaultTrio = showdownAlt["trios"].nullObj
				val defaultSquad = showdownAlt["squad"].nullObj

				if (defaultSolo != null)
					fillGameModeInfo("showdownsolo", defaultSolo, true)

				if (defaultDuo != null)
					fillGameModeInfo("showdownduos", defaultDuo, true)

				if (defaultTrio != null)
					fillGameModeInfo("showdowntrios", defaultTrio, true)

				if (defaultSquad != null)
					fillGameModeInfo("showdownsquads", defaultSquad, true)
			}

			// Para o total minutes played, vamos fazer algo recursivo
			fun findTotalMinutesPlayed(element: JsonObject) {
				for ((name, value) in element.entrySet()) {
					if (value.isJsonObject) {
						findTotalMinutesPlayed(value.obj)
					}
				}

				totalMinutesPlayed += element["minutesPlayed"].nullInt ?: 0
			}

			findTotalMinutesPlayed(gameModes)
		}

		val userName = statsV2["general"]["userName"].string

		val image = ImageIO.read(File(loritta.instanceConfig.loritta.folders.assets, "fortnite_stats.png"))
		val graphics = image.graphics.enableFontAntiAliasing()

		val titleFont = Constants.BURBANK_BIG_CONDENSED_BLACK.deriveFont(109f)

		graphics.font = titleFont
		graphics.color = Color(0, 0, 0, 100)
		ImageUtils.drawCenteredString(graphics, userName, Rectangle(0, 14 + 6, 1353, 14 + 110 + 14 + 6), titleFont)

		graphics.color = Color.WHITE
		ImageUtils.drawCenteredString(graphics, userName, Rectangle(0, 14, 1353, 14 + 110 + 14), titleFont)

		val verticalHeaderFont = Constants.BURBANK_BIG_CONDENSED_BLACK.deriveFont(42f)

		graphics.color = Color(119, 123, 150)
		graphics.font = verticalHeaderFont

		ImageUtils.drawCenteredString(graphics, locale["${LOCALE_PREFIX}.wins"].toUpperCase(), Rectangle(50, 198, 143, 63), verticalHeaderFont)
		ImageUtils.drawCenteredString(graphics, locale["${LOCALE_PREFIX}.killDeathRatio"].toUpperCase(), Rectangle(232, 198, 89, 63), verticalHeaderFont)
		ImageUtils.drawCenteredString(graphics, "${locale["${LOCALE_PREFIX}.wins"]} %".toUpperCase(), Rectangle(352, 198, 184, 63), verticalHeaderFont)

		val verticalBodyFont = Constants.BURBANK_BIG_CONDENSED_BLACK.deriveFont(44f)

		graphics.color = Color.WHITE
		graphics.font = verticalBodyFont

		ImageUtils.drawCenteredString(graphics, wins.toString(), Rectangle(50, 253, 143, 63), verticalHeaderFont)
		ImageUtils.drawCenteredString(graphics, getPrettyPercentage(totalKills.toDouble() / matchesPlayed), Rectangle(232, 253, 89, 63), verticalHeaderFont)
		ImageUtils.drawCenteredString(graphics, getPrettyPercentage(wins.toDouble() / matchesPlayed), Rectangle(352, 253, 184, 63), verticalHeaderFont)

		val inlineBodyStatsFont = Constants.BURBANK_BIG_CONDENSED_BLACK.deriveFont(50f)
		graphics.font = inlineBodyStatsFont
		graphics.color = Color(119, 123, 150)

		graphics.drawString(locale["${LOCALE_PREFIX}.eliminations"].toUpperCase(), 44, 378)
		graphics.drawString(locale["${LOCALE_PREFIX}.averageElims"].toUpperCase(), 44, 455)
		graphics.drawString(locale["${LOCALE_PREFIX}.matchesPlayed"].toUpperCase(), 44, 533)
		graphics.drawString(locale["${LOCALE_PREFIX}.timePlaying"].toUpperCase(), 44, 612)

		graphics.color = Color.WHITE

		val elimAverage = getPrettyPercentage(totalKills.toDouble() / matchesPlayed)
		graphics.drawString(totalKills.toString(), 539 - graphics.fontMetrics.stringWidth(totalKills.toString()), 378)
		graphics.drawString(elimAverage, 539 - graphics.fontMetrics.stringWidth(elimAverage), 455)
		graphics.drawString(matchesPlayed.toString(), 539 - graphics.fontMetrics.stringWidth(matchesPlayed.toString()), 533)

		var jvmUpTime = totalMinutesPlayed.toLong()
		val days = TimeUnit.MINUTES.toDays(jvmUpTime)
		jvmUpTime -= TimeUnit.DAYS.toMinutes(days)
		val hours = TimeUnit.MINUTES.toHours(jvmUpTime)
		jvmUpTime -= TimeUnit.HOURS.toMinutes(hours)
		val minutes = TimeUnit.MINUTES.toMinutes(jvmUpTime)

		val fancyTimePlaying = "${days}d${hours}h${minutes}m"
		graphics.drawString(fancyTimePlaying, 539 - graphics.fontMetrics.stringWidth(fancyTimePlaying.toString()), 612)

		fun drawGameModeStats(gameMode: GameMode, name: String, color: Color, x: Int, y: Int, isTeamRumble: Boolean) {
			if (isTeamRumble) {
				graphics.drawImage(makeFortniteHeader(graphics.fontMetrics, color, name.toUpperCase()), x + 48 + 335, y, null)
			} else {
				graphics.drawImage(makeFortniteHeader(graphics.fontMetrics, color, name.toUpperCase()), x + 48, y, null)
			}
			val matchesPlayed = gameMode.matchesPlayed

			graphics.font = Constants.BURBANK_BIG_CONDENSED_BLACK.deriveFont(40f)
			graphics.color = Color.WHITE
			val fontMetrics = graphics.fontMetrics

			graphics.drawString(locale["${LOCALE_PREFIX}.matchesCount", matchesPlayed], x + 730 - fontMetrics.stringWidth("${matchesPlayed} Partidas") - 16, y + 48 - 9)

			val inlineBodyStatsFont = Constants.BURBANK_BIG_CONDENSED_BLACK.deriveFont(42f)
			graphics.font = inlineBodyStatsFont
			graphics.color = Color(119, 123, 150)

			if (!isTeamRumble) {
				ImageUtils.drawCenteredString(graphics, locale["${LOCALE_PREFIX}.wins"].toUpperCase(), Rectangle(x + 18, y + 70, 136, 49), inlineBodyStatsFont)
				ImageUtils.drawCenteredString(graphics, "${locale["${LOCALE_PREFIX}.wins"]} %".toUpperCase(), Rectangle(x + 171, y + 70, 173, 49), inlineBodyStatsFont)
			}
			ImageUtils.drawCenteredString(graphics, locale["${LOCALE_PREFIX}.elims"].toUpperCase(), Rectangle(x + 364, y + 70, 89, 49), inlineBodyStatsFont)
			ImageUtils.drawCenteredString(graphics, locale["${LOCALE_PREFIX}.killDeathRatio"].toUpperCase(), Rectangle(x + 492, y + 70, 57, 49), inlineBodyStatsFont)
			ImageUtils.drawCenteredString(graphics, locale["${LOCALE_PREFIX}.score"].toUpperCase(), Rectangle(x + 581, y + 70, 119, 49), inlineBodyStatsFont)

			graphics.color = Color.WHITE

			if (!isTeamRumble) {
				ImageUtils.drawCenteredString(graphics, gameMode.placeTop1.toString(), Rectangle(x + 18, y + 70 + 45, 136, 49), inlineBodyStatsFont)
				ImageUtils.drawCenteredString(graphics, getPrettyPercentage(gameMode.placeTop1.toDouble() / matchesPlayed), Rectangle(x + 171, y + 70 + 45, 173, 49), inlineBodyStatsFont)
			}

			ImageUtils.drawCenteredString(graphics, gameMode.kills.toString(), Rectangle(x + 364, y + 70 + 45, 89, 49), inlineBodyStatsFont)
			ImageUtils.drawCenteredString(graphics, getPrettyPercentage(gameMode.kills.toDouble() / matchesPlayed), Rectangle(x + 492, y + 70 + 45, 57, 49), inlineBodyStatsFont)
			ImageUtils.drawCenteredString(graphics, gameMode.score.toString(), Rectangle(x + 581, y + 70 + 45, 119, 49), inlineBodyStatsFont)
		}

		val defaultSoloGameMode = foundGameModes["defaultsolo"] ?: GameMode()
		val defaultDuosGameMode = foundGameModes["defaultduo"] ?: GameMode()
		val defaultSquadGameMode = foundGameModes["defaultsquad"] ?: GameMode()
		val defaultTeamRumbleGameMode = foundGameModes["teamrumble"] ?: GameMode()

		drawGameModeStats(defaultSoloGameMode, locale["${LOCALE_PREFIX}.solo"], Color(0, 123, 255), 589, 186, false)
		drawGameModeStats(defaultDuosGameMode, locale["${LOCALE_PREFIX}.duos"], Color(124, 229, 67), 589, 393, false)
		drawGameModeStats(defaultSquadGameMode, locale["${LOCALE_PREFIX}.squad"], Color(255, 132, 0), 589, 600, false)
		drawGameModeStats(defaultTeamRumbleGameMode, locale["${LOCALE_PREFIX}.rumble"], Color(255, 0, 132), 589, 807, true)

		context.sendFile(image, "fortnite-stats.png", " ")
	}

	fun getPrettyPercentage(percentage: Double): String {
		val fmt = "%.2f".format(percentage)

		if (fmt == "0.00")
			return "<0.01"
		if (fmt == "NaN")
			return "0.00"

		return fmt
	}

	private fun makeFortniteHeader(fontMetrics: FontMetrics, color: Color, str: String): BufferedImage {
		val header = str
		val width = fontMetrics.stringWidth(header)

		val subHeader = BufferedImage(14 + width + 14 + 14, 48, BufferedImage.TYPE_INT_ARGB)
		val subHeaderGraphics = subHeader.graphics.enableFontAntiAliasing()

		subHeaderGraphics.font = Constants.BURBANK_BIG_CONDENSED_BLACK.deriveFont(40f)

		subHeaderGraphics.color = color
		subHeaderGraphics.fillRect(0, 0, 1024, 48)

		subHeaderGraphics.color = Color.WHITE
		subHeaderGraphics.drawString(header, 0, 48 - 9)

		val subHeaderApplyPath = BufferedImage(subHeader.width, subHeader.height, BufferedImage.TYPE_INT_ARGB)

		val path = Path2D.Double()
		path.moveTo(0.0, 0.0)
		path.lineTo( width.toDouble() + 8.0, 0.0)
		path.lineTo(width.toDouble() + 8.0 + 14.0, 48.0)
		path.lineTo(0.0, 48.0)
		path.closePath()

		val sHAPG = subHeaderApplyPath.graphics
		sHAPG.clip = path
		sHAPG.drawImage(subHeader, 0, 0, null)

		return subHeaderApplyPath
	}

	class GameMode {
		var matchesPlayed = 0
		var placeTop1 = 0
		var kills = 0
		var score = 0
	}
}