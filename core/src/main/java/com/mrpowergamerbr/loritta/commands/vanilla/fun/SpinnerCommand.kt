package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale

class SpinnerCommand : AbstractCommand("spinner", listOf("fidget", "fidgetspinner"), category = CommandCategory.FUN) {
	var spinningSpinners: MutableMap<String, FidgetSpinner> = mutableMapOf<String, FidgetSpinner>()

	data class FidgetSpinner(var emoji: String, var threadId: Long, var forTime: Int, var spinnedAt: Long, var lastRerotation: Long)

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["SPINNER_DESCRIPTION"]
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		// TODO: Fix
		/* if (context.args.isNotEmpty()) {
			val arg = context.args[0]
			val page = if (context.args.size == 2) { context.args[1].toIntOrNull() ?: 1 } else { 1 }
			if (arg == "rank") {
				val documents = loritta.mongo
						.getDatabase(loritta.config.databaseName)
						.getCollection("users")
						.aggregate(listOf(
								Aggregates.match(Filters.exists("spinnerScores")),
								project(
										Projections.include("_id", "spinnerScores")
								),
								Document("\$unwind", "\$spinnerScores"),
								sort(Document("spinnerScores.forTime", -1)),
								skip(5 * (page - 1)),
								limit(5)
						)
						).iterator()

				val aggregateTime = loritta.mongo
						.getDatabase(loritta.config.databaseName)
						.getCollection("users")
						.aggregate(listOf(
								Document("\$unwind", "\$spinnerScores"),
								Document("\$group", Document("_id", null).append("total", Document("\$sum", "\$spinnerScores.forTime")))
						)
						)

				val rankHeader = ImageIO.read(File(Loritta.ASSETS, "rank_header.png"))
				val base = BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB_PRE)
				val graphics = base.graphics as Graphics2D

				graphics.setRenderingHint(
						java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
						java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

				graphics.color = Color(30, 33, 36)
				graphics.fillRect(0, 0, 400, 37)
				graphics.color = Color.WHITE
				// graphics.drawImage(serverIcon, 259, -52, null)

				graphics.drawImage(rankHeader, 0, 0, null)

				val oswaldRegular10 = Constants.OSWALD_REGULAR
						.deriveFont(10F)

				val oswaldRegular12 = oswaldRegular10
						.deriveFont(12F)

				val oswaldRegular16 = oswaldRegular10
						.deriveFont(16F)

				val oswaldRegular20 = oswaldRegular10
						.deriveFont(20F)

				graphics.font = oswaldRegular16

				ImageUtils.drawCenteredString(graphics, "Ranking Global de Spinners", Rectangle(0, 0, 400, 26), oswaldRegular16)

				var idx = 0
				var currentY = 37;

				val document = aggregateTime.first()
				println(document)
				var total = document.getLong("total")
				documents.use {
					var index = 0
					while (it.hasNext()) {
						val document = it.next()
						val spinnerScore = document["spinnerScores"] as Document
						val forTime = spinnerScore.getLong("forTime")
						val userId = document.getString("_id")

						val user = lorittaShards.getUserById(userId)

						if (user != null) {
							// val userProfile = loritta.getLorittaProfileForUser(id)
							val file = java.io.File(Loritta.FRONTEND, "static/assets/img/backgrounds/" + user.id + ".png")
							val imageFile = if (file.exists()) file else java.io.File(Loritta.FRONTEND, "static/assets/img/backgrounds/default_background.png")

							val rankBackground = ImageIO.read(imageFile)
							graphics.drawImage(rankBackground.getScaledInstance(400, 300, BufferedImage.SCALE_SMOOTH)
									.toBufferedImage()
									.getSubimage(0, idx * 52, 400, 53), 0, currentY, null)

							graphics.color = Color(0, 0, 0, 127)
							graphics.fillRect(0, currentY, 400, 53)

							graphics.color = Color(255, 255, 255)

							graphics.font = oswaldRegular20

							ImageUtils.drawTextWrap(user.name, 143, currentY + 21, 9999, 9999, graphics.fontMetrics, graphics)

							graphics.font = oswaldRegular16

							ImageUtils.drawTextWrap("${forTime} segundos", 144, currentY + 38, 9999, 9999, graphics.fontMetrics, graphics)

							graphics.font = oswaldRegular10

							// ImageUtils.drawTextWrap("Nível " + userData.getCurrentLevel().currentLevel, 145, currentY + 48, 9999, 9999, graphics.fontMetrics, graphics)

							val avatar = LorittaUtils.downloadImage(user.effectiveAvatarUrl).getScaledInstance(143, 143, BufferedImage.SCALE_SMOOTH)

							var editedAvatar = BufferedImage(143, 143, BufferedImage.TYPE_INT_ARGB)
							val avatarGraphics = editedAvatar.graphics as Graphics2D

							val path = Path2D.Double()
							path.moveTo(0.0, 45.0)
							path.lineTo(132.0, 45.0)
							path.lineTo(143.0, 98.0)
							path.lineTo(0.0, 98.0)
							path.closePath()

							avatarGraphics.clip = path

							avatarGraphics.drawImage(avatar, 0, 0, null)

							editedAvatar = editedAvatar.getSubimage(0, 45, 143, 53)
							graphics.drawImage(editedAvatar, 0, currentY, null)

							val emoji = spinnerScore.getString("emoji")
							val image = when (emoji) {
								"<:spinner8:411981187565879306>" -> "https://cdn.discordapp.com/emojis/344292269836206082.png"
								"<:spinner2:411981187830251520>" -> "https://cdn.discordapp.com/emojis/327245670052397066.png"
								"<:spinner3:411981187586850816>" -> "https://cdn.discordapp.com/emojis/327246151591919627.png"
								"<:spinner4:411981187758686208>" -> "https://cdn.discordapp.com/emojis/344292269764902912.png"
								"<:spinner5:411981188048224266>" -> "https://cdn.discordapp.com/emojis/344292269160923147.png"
								"<:spinner6:411981187289186316>" -> "https://cdn.discordapp.com/emojis/344292270125613056.png"
								"<:spinner7:411981187553165325>" -> "https://cdn.discordapp.com/emojis/344292270268350464.png"
								"<:spinner1:411981187419078657>" -> "https://cdn.discordapp.com/emojis/327243530244325376.png"
								else -> "https://cdn.discordapp.com/emojis/366047906689581085.png"
							}

							val spinner = LorittaUtils.downloadImage(image).getScaledInstance(49, 49, BufferedImage.SCALE_SMOOTH)
							graphics.drawImage(spinner, 400 - 49 - 2, currentY + 2, null)

							idx++
							currentY += 53;
						}
						index++
					}
				}

				var _total = total * 1000

				val days = TimeUnit.MILLISECONDS.toDays(_total)
				_total -= TimeUnit.DAYS.toMillis(days)
				val hours = TimeUnit.MILLISECONDS.toHours(_total)
				_total -= TimeUnit.HOURS.toMillis(hours)
				val minutes = TimeUnit.MILLISECONDS.toMinutes(_total)
				_total -= TimeUnit.MINUTES.toMillis(minutes)
				val seconds = TimeUnit.MILLISECONDS.toSeconds(_total)

				graphics.font = oswaldRegular12
				ImageUtils.drawCenteredString(graphics, "No total, ${days}d ${hours}h ${minutes}m ${seconds}s foram gastos girando spinners! (Wow, quanto tempo!)", Rectangle(0, 11, 400, 28), oswaldRegular12)

				context.sendFile(base.makeRoundedCorners(15), "spinner_rank.png", context.getAsMention(true))
				return
			}
		}
		if (spinningSpinners.contains(context.userHandle.id)) {
			val spinner = spinningSpinners[context.userHandle.id]!!

			val diff = (System.currentTimeMillis() - spinner.lastRerotation) / 1000

			if (diff in spinner.forTime - 5 .. spinner.forTime + 5 || (context.userHandle.id == "123170274651668480" && context.args.getOrNull(0) == "force")) {
				val time = Loritta.RANDOM.nextInt(10, 61)

				// Ao passar do tempo, mais difícil fica regirar um spinner (cansaço)
				val bound = ((diff / 10) + 5).toInt()

				var lowerBound = Math.max(0, time - Loritta.RANDOM.nextInt(-bound, bound + 1))
				var upperBound = Math.max(0, time - Loritta.RANDOM.nextInt(-bound, bound + 1))

				if (lowerBound > upperBound) {
					val temp = upperBound
					upperBound = lowerBound
					lowerBound = temp
				}

				val template = ImageIO.read(File(Loritta.ASSETS + "spinner_respin.png")) // Template
				val graphics = template.graphics as Graphics2D

				graphics.setRenderingHint(
						java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
						java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

				val whitneyBold = 	FileInputStream(File(Loritta.ASSETS + "whitney-bold.ttf")).use {
					Font.createFont(Font.TRUETYPE_FONT, it)
				}

				val whitneyBold17 = whitneyBold.deriveFont(17f)

				graphics.font = whitneyBold17

				graphics.color = Color.BLACK

				val text = context.locale["SPINNER_MAGIC_BALL", lowerBound, upperBound].replace("**", "")
				ImageUtils.drawTextWrapSpaces(text, 49, 20, 195, 9999999, graphics.fontMetrics, graphics)
				ImageUtils.drawTextWrapSpaces(text, 51, 20, 195, 9999999, graphics.fontMetrics, graphics)
				ImageUtils.drawTextWrapSpaces(text, 50, 19, 195, 9999999, graphics.fontMetrics, graphics)
				ImageUtils.drawTextWrapSpaces(text, 50, 21, 195, 9999999, graphics.fontMetrics, graphics)

				graphics.color = Color.WHITE

				ImageUtils.drawTextWrapSpaces(text, 50, 20, 195, 9999999, graphics.fontMetrics, graphics)

				context.reply(
						template,
						"respinned.png",
						LoriReply(
								message = context.locale["SPINNER_RESPINNED"],
								prefix = spinner.emoji
						)
				)

				delay(time * 1000)

				if (spinningSpinners.contains(context.userHandle.id)) {
					val spinner = spinningSpinners[context.userHandle.id]!!

					if (spinner.threadId != Thread.currentThread().id) {
						return
					}
					val diff = (System.currentTimeMillis() - spinner.spinnedAt) / 1000

					context.reply(
							LoriReply(
									message = context.locale["SPINNER_SPINNED", diff],
									prefix = spinner.emoji
							),
							LoriReply(
									message = context.locale["SPINNER_ViewRank", context.config.commandPrefix],
									prefix = "\uD83C\uDFC6"
							)
					)

					spinningSpinners.remove(context.userHandle.id)
					val profile = loritta.getLorittaProfileForUser(context.userHandle.id)
					profile.spinnerScores.add(MongoLorittaProfile.SpinnerScore(spinner.emoji, diff))
					loritta save profile
				}

				spinner.lastRerotation = System.currentTimeMillis()
				spinner.threadId = Loritta.RANDOM.nextLong(0, Long.MAX_VALUE)
				spinner.forTime = time
				spinningSpinners.put(context.userHandle.id, spinner)
			} else {
				val diff = (System.currentTimeMillis() - spinner.spinnedAt) / 1000

				context.reply(
						LoriReply(
								message = "${context.locale["SPINNER_OUCH"]} ${context.locale["SPINNER_SPINNED", diff]}",
								prefix = spinner.emoji
						),
						LoriReply(
								message = context.locale["SPINNER_ViewRank", context.config.commandPrefix],
								prefix = "\uD83C\uDFC6"
						)
				)

				spinningSpinners.remove(context.userHandle.id)

				val profile = loritta.getLorittaProfileForUser(context.userHandle.id)
				profile.spinnerScores.add(MongoLorittaProfile.SpinnerScore(spinner.emoji, diff))
				loritta save profile
			}
			return
		}
		var time = Loritta.RANDOM.nextInt(10, 61); // Tempo que o Fidget Spinner irá ficar rodando

		var random = listOf("<:spinner1:411981187419078657>", "<:spinner2:411981187830251520>", "<:spinner3:411981187586850816>", "<:spinner4:411981187758686208>", "<:spinner5:411981188048224266>", "<:spinner6:411981187289186316>", "<:spinner7:411981187553165325>", "<:spinner8:411981187565879306>") // Pegar um spinner aleatório
		var spinnerEmoji = random[Loritta.RANDOM.nextInt(random.size)]

		var lowerBound = Math.max(0, time - Loritta.RANDOM.nextInt(-5, 6))
		var upperBound = Math.max(0, time - Loritta.RANDOM.nextInt(-5, 6))

		if (lowerBound > upperBound) {
			val temp = upperBound;
			upperBound = lowerBound
			lowerBound = temp
		}

		val msg = context.reply(
				LoriReply(
						message = context.locale["SPINNER_SPINNING"],
						prefix = spinnerEmoji
				),
				LoriReply(
						message = "*" + context.locale["SPINNER_MAGIC_BALL", lowerBound, upperBound] + "*",
						prefix = "\uD83D\uDD2E"
				)
		)

		delay(time * 1000)
		Thread.sleep((time * 1000).toLong())

		if (spinningSpinners.contains(context.userHandle.id)) {
			val spinner = spinningSpinners[context.userHandle.id]!!

			if (spinner.threadId != Thread.currentThread().id) {
				return
			}
			msg.delete().queue()

			context.reply(
					LoriReply(
							message = context.locale["SPINNER_SPINNED", time],
							prefix = spinnerEmoji
					),
					LoriReply(
							message = context.locale["SPINNER_ViewRank", context.config.commandPrefix],
							prefix = "\uD83C\uDFC6"
					)
			)

			spinningSpinners.remove(context.userHandle.id)
			val profile = loritta.getLorittaProfileForUser(context.userHandle.id)
			profile.spinnerScores.add(MongoLorittaProfile.SpinnerScore(spinner.emoji, time.toLong()))
			loritta save profile
		}

		val fidgetSpinner = FidgetSpinner(spinnerEmoji, Loritta.RANDOM.nextLong(0, Long.MAX_VALUE), time, System.currentTimeMillis(), System.currentTimeMillis())

		spinningSpinners.put(context.userHandle.id, fidgetSpinner) */
	}
}