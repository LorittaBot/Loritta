package net.perfectdreams.loritta.plugin.fortnite.commands.fortnite

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.utils.*
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.fortnite.FortniteStuff
import net.perfectdreams.loritta.utils.extensions.readImage
import java.awt.Color
import java.awt.FontMetrics
import java.awt.Rectangle
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.TimeUnit

class FortniteStatsCommand(val m: FortniteStuff) : DiscordAbstractCommandBase(m.loritta, listOf("fortnitestats", "fnstats", "fortniteprofile", "fnprofile"), CommandCategory.FORTNITE) {
	private val LOCALE_PREFIX = "commands.command.fnstats"

	override fun command() = create {
		localizedDescription("${LOCALE_PREFIX}.description")
		localizedExamples("${LOCALE_PREFIX}.examples")

		needsToUploadFiles = true

		usage {
			argument(ArgumentType.TEXT) {}
		}

		executesDiscord {
			if (args.isEmpty())
				explainAndExit()

			val arg0 = args.joinToString(" ")

			val payloadV2 = HttpRequest.get("https://fortnite-api.com/v1/stats/br/v2?name=${arg0.encodeToUrl()}")
					.header("Authorization", com.mrpowergamerbr.loritta.utils.loritta.config.fortniteApi.token)
					.body()

			val statsV2 = JsonParser.parseString(payloadV2).obj

			if (statsV2["status"].nullInt != 200) {
				reply(
						LorittaReply(
								locale["${LOCALE_PREFIX}.unknownPlayer", "`${arg0.stripCodeMarks()}`"],
								Constants.ERROR
						)
				)
				return@executesDiscord
			}
			val dataV2 = statsV2["data"].obj

			val overall = dataV2["stats"]["all"]["overall"]
			val solo = dataV2["stats"]["all"]["solo"]
			val duo = dataV2["stats"]["all"]["duo"]
			val squad = dataV2["stats"]["all"]["squad"]
			val ltm = dataV2["stats"]["all"]["ltm"]

			val wins = overall["wins"].int
			val matchesPlayed = overall["matches"].int
			val totalKills = overall["kills"].int
			val totalMinutesPlayed = overall["minutesPlayed"].int

			val foundGameModes = mapOf<String, GameMode>(
					"defaultsolo" to GameMode(
							solo["matches"].int,
							solo["wins"].int,
							solo["kills"].int,
							solo["score"].int
					),
					"defaultduos" to GameMode(
							duo["matches"].int,
							duo["wins"].int,
							duo["kills"].int,
							duo["score"].int
					),
					"defaultsquad" to GameMode(
							squad["matches"].int,
							squad["wins"].int,
							squad["kills"].int,
							squad["score"].int
					)
			)

			val userName = dataV2["account"]["name"].string

			val image = readImage(File(loritta.instanceConfig.loritta.folders.assets, "fortnite_stats.png"))
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

			fun drawGameModeStats(gameMode: FortniteStatsCommand.GameMode, name: String, color: Color, x: Int, y: Int, isTeamRumble: Boolean) {
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

			val defaultSoloGameMode = foundGameModes["defaultsolo"]!!
			val defaultDuosGameMode = foundGameModes["defaultduos"]!!
			val defaultSquadGameMode = foundGameModes["defaultsquad"]!!
			// val defaultTeamRumbleGameMode = foundGameModes["teamrumble"]

			drawGameModeStats(defaultSoloGameMode, locale["${LOCALE_PREFIX}.solo"], Color(0, 123, 255), 589, 186, false)
			drawGameModeStats(defaultDuosGameMode, locale["${LOCALE_PREFIX}.duos"], Color(124, 229, 67), 589, 393, false)
			drawGameModeStats(defaultSquadGameMode, locale["${LOCALE_PREFIX}.squad"], Color(255, 132, 0), 589, 600, false)
			// drawGameModeStats(defaultTeamRumbleGameMode, locale["${LOCALE_PREFIX}.rumble"], Color(255, 0, 132), 589, 807, true)

			sendImage(JVMImage(image), "fortnite-stats.png")
		}
	}


	private fun getPrettyPercentage(percentage: Double): String {
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

	class GameMode(
			val matchesPlayed: Int,
			val placeTop1: Int,
			val kills: Int,
			val score: Int
	)
}