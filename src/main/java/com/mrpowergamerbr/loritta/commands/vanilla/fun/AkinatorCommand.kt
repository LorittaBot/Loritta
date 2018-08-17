package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import org.json.XML
import java.awt.Color
import kotlin.collections.set

class AkinatorCommand : AbstractCommand("akinator", category = CommandCategory.FUN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("AKINATOR_DESCRIPTION")
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_MANAGE)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	fun getApiEndpoint(localeId: String): String {
		return when (localeId) {
			"default", "pt-pt", "pt-funk" -> "http://62.4.22.192:8166"
			"tr-tr" -> "http://62.4.22.192:8164"
			"pl-pl" -> "http://37.187.149.213:8143"
			"ru-ru" -> "http://62.4.22.192:8169"
			"nl-nl" -> "http://62.210.100.133:8158"
			"kr-kr" -> "http://62.4.22.192:8168"
			"ja-jp" -> "http://178.33.63.63:8012"
			"it-it" -> "http://62.210.100.133:8159"
			"he-il" -> "http://178.33.63.63:8006"
			"fr-fr" -> "http://62.4.22.192:8165"
			"es-es" -> "http://62.210.100.133:8160"
			"ar-sa" -> "http://62.210.100.133:8155"
			"ch-ch" -> "http://158.69.225.49:8150"
			else -> "http://62.210.100.133:8157"
		}
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val apiEndpoint = getApiEndpoint(context.config.localeId)
		val response = HttpRequest.get("$apiEndpoint/ws/new_session.php?base=0&partner=410&premium=0&player=Android-Phone&uid=6fe3a92130c49446&do_geoloc=1&prio=0&constraint=ETAT%3C%3E'AV'&channel=0&only_minibase=0")
				.body()

		val xmlJSONObj = XML.toJSONObject(response);

		val jsonPrettyPrintString = xmlJSONObj.toString(4);

		val jsonSession = jsonParser.parse(jsonPrettyPrintString).obj["RESULT"]

		val identification = jsonSession["PARAMETERS"]["IDENTIFICATION"].obj

		val channel = identification["CHANNEL"].long
		val session = identification["SESSION"].long
		val signature = identification["SIGNATURE"].long

		val stepInfo = jsonSession["PARAMETERS"]["STEP_INFORMATION"]

		val question = stepInfo["QUESTION"].string
		val progression = stepInfo["PROGRESSION"].double
		val step = stepInfo["STEP"].int
		val answers = stepInfo["ANSWERS"]["ANSWER"].array

		var text = "[`";
		for (i in 0..100 step 10) {
			if (progression >= i) {
				text += "█";
			} else {
				text += ".";
			}
		}
		text += "`]"

		var reactionInfo = ""

		for ((idx, answer) in answers.withIndex()) {
			reactionInfo += Constants.INDEXES[idx] + " ${answer.string}\n"
		}

		val builder = EmbedBuilder().apply {
			setTitle("<:akinator:383613256939470849> Akinator (${context.handle.effectiveName})")
			setThumbnail("${Loritta.config.websiteUrl}assets/img/akinator_embed.png")
			setDescription(question + "\n\n$progression% $text\n\n$reactionInfo")
			setColor(Color(20, 158, 255))
		}

		context.metadata["channel"] = channel
		context.metadata["session"] = session
		context.metadata["signature"] = signature
		context.metadata["step"] = step

		val message = context.sendMessage(context.getAsMention(true), builder.build())

		message.onReactionAddByAuthor(context) {
			val apiEndpoint = getApiEndpoint(context.config.localeId)

			it.reaction.removeReaction(context.userHandle).complete()
			if (context.metadata.contains("channel")) {
				val channel = context.metadata["channel"] as Long
				val session = context.metadata["session"] as Long
				val signature = context.metadata["signature"] as Long
				var step = context.metadata["step"] as Int
				val answer = when {
					it.reactionEmote.name == "1⃣" -> 0
					it.reactionEmote.name == "2⃣" -> 1
					it.reactionEmote.name == "3⃣" -> 2
					it.reactionEmote.name == "4⃣" -> 3
					it.reactionEmote.name == "5⃣" -> 4
					else -> 0
				}

				val response = if (it.reactionEmote.name == "⏪") {
					HttpRequest.get("$apiEndpoint/ws/cancel_answer.php?base=0&channel=$channel&session=$session&signature=$signature&step=$step")
							.body()
				} else {
					HttpRequest.get("$apiEndpoint/ws/answer.php?base=0&channel=$channel&session=$session&signature=$signature&step=$step&answer=$answer")
							.body()
				}

				val xmlJSONObj = XML.toJSONObject(response);

				val jsonPrettyPrintString = xmlJSONObj.toString(4);

				val jsonResult = jsonParser.parse(jsonPrettyPrintString).obj["RESULT"]

				if (jsonResult["COMPLETION"].string == "KO - TIMEOUT") {
					val builder = EmbedBuilder().apply {
						setTitle("<:akinator:383613256939470849> Akinator")
						setDescription(context.locale.get("AKINATOR_TIMEOUT"))
						setColor(Color(20, 158, 255))
					}

					context.metadata.remove("channel")
					context.metadata.remove("signature")
					context.metadata.remove("session")
					context.metadata.remove("step")

					message.clearReactions().complete()
					message.editMessage(builder.build()).complete()
					return@onReactionAddByAuthor
				}

				if (jsonResult["COMPLETION"].string == "WARN - NO QUESTION") {
					val builder = EmbedBuilder().apply {
						setTitle("<:akinator:383613256939470849> Akinator")
						setDescription(context.locale.get("AKINATOR_NoQuestion"))
						setColor(Color(20, 158, 255))
					}

					context.metadata.remove("channel")
					context.metadata.remove("signature")
					context.metadata.remove("session")
					context.metadata.remove("step")

					message.clearReactions().complete()
					message.editMessage(builder.build()).complete()
					return@onReactionAddByAuthor
				}

				try {
					val jsonAnswer = jsonResult["PARAMETERS"]

					val question = jsonAnswer["QUESTION"].string
					val progression = jsonAnswer["PROGRESSION"].double
					step = jsonAnswer["STEP"].int
					val answers = jsonAnswer["ANSWERS"]["ANSWER"].array

					if (95 >= progression) {
						var text = "[`";
						for (i in 0..100 step 10) {
							if (progression >= i) {
								text += "█";
							} else {
								text += ".";
							}
						}
						text += "`]"

						var reactionInfo = ""

						for ((idx, answer) in answers.withIndex()) {
							reactionInfo += Constants.INDEXES[idx] + " ${answer.string}\n"
						}

						val builder = EmbedBuilder().apply {
							setTitle("<:akinator:383613256939470849> Akinator (${context.handle.effectiveName})")
							setThumbnail("https://loritta.website/assets/img/akinator_embed.png")
							setDescription(question + "\n\n$progression% $text\n\n$reactionInfo")
							setColor(Color(20, 158, 255))
						}

						context.metadata["channel"] = channel
						context.metadata["session"] = session
						context.metadata["signature"] = signature
						context.metadata["step"] = step

						message.editMessage(builder.build()).complete()

						if (message.reactions.filter { it.reactionEmote.name == "⏪" }.count() == 0) {
							if (step > 0) {
								message.addReaction("⏪").complete()
							}
						} else {
							if (step == 0) {
								message.reactions.forEach {
									if (it.reactionEmote.name == "⏪") {
										it.removeReaction(context.userHandle).complete()
									}
								}
							}
						}
					} else {
						val response = HttpRequest.get("$apiEndpoint/ws/list.php?base=0&channel=$channel&session=$session&signature=$signature&step=$step&size=1&max_pic_width=360&max_pic_height=640&mode_question=0")
								.body()

						val xmlJSONObj = XML.toJSONObject(response);

						val jsonPrettyPrintString = xmlJSONObj.toString(4);

						val jsonAnswer = jsonParser.parse(jsonPrettyPrintString).obj["RESULT"]["PARAMETERS"]["ELEMENTS"]["ELEMENT"]

						val builder = EmbedBuilder().apply {
							setTitle("<:akinator:383613256939470849> ${jsonAnswer["NAME"].string}")
							setImage(jsonAnswer["ABSOLUTE_PICTURE_PATH"].string)
							setDescription(jsonAnswer["DESCRIPTION"].string)
							addField("Ranking", "#${jsonAnswer["RANKING"].string}", false)
							setColor(Color(20, 158, 255))
						}

						context.metadata.remove("channel")
						context.metadata.remove("signature")
						context.metadata.remove("session")
						context.metadata.remove("step")

						message.clearReactions().complete()
						message.editMessage(builder.build()).complete()
					}
				} catch (e: Exception) {
					logger.error(response, e)
				}
			}
		}

		for (emote in Constants.INDEXES.subList(0, 5)) {
			message.addReaction(emote).complete()
		}
	}
}