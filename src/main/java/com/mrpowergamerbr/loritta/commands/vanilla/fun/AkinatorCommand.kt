package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import org.json.XML
import java.awt.Color

class AkinatorCommand : CommandBase() {
	override fun getLabel(): String {
		return "akinator"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.get("AKINATOR_DESCRIPTION")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN
	}

	override fun run(context: CommandContext) {
		val response = HttpRequest.get("http://api-pt3.akinator.com/ws/new_session.php?base=0&partner=410&premium=0&player=Android-Phone&uid=6fe3a92130c49446&do_geoloc=1&prio=0&constraint=ETAT%3C%3E'AV'&channel=0&only_minibase=0")
				.body()

		val xmlJSONObj = XML.toJSONObject(response);

		val jsonPrettyPrintString = xmlJSONObj.toString(4);

		val jsonSession = JsonParser().parse(jsonPrettyPrintString).obj["RESULT"]

		val identification = jsonSession["PARAMETERS"]["IDENTIFICATION"].obj

		val channel = identification["CHANNEL"].long
		val session = identification["SESSION"].long
		val signature = identification["SIGNATURE"].long

		val stepInfo = jsonSession["PARAMETERS"]["STEP_INFORMATION"]

		var question = stepInfo["QUESTION"].string
		var progression = stepInfo["PROGRESSION"].double
		var step = stepInfo["STEP"].int
		var answers = stepInfo["ANSWERS"]["ANSWER"].array

		var text = "[`";
		for (i in 0..100 step 10) {
			if (progression >= i) {
				text += "█";
			} else {
				text += ".";
			}
		}
		text += "`]"

		val indexes = listOf("1⃣",
				"2⃣",
				"3⃣",
				"4⃣",
				"5⃣")

		var reactionInfo = ""

		for ((idx, answer) in answers.withIndex()) {
			reactionInfo += indexes[idx] + " ${answer.string}\n"
		}

		val builder = EmbedBuilder().apply {
			setTitle("<:akinator:348903800540758017> Akinator (${context.handle.effectiveName})")
			setThumbnail("https://loritta.website/assets/img/akinator_embed.png")
			setDescription(question + "\n\n$progression% $text\n\n$reactionInfo")
			setColor(Color(20, 158, 255))
		}

		context.metadata["channel"] = channel
		context.metadata["session"] = session
		context.metadata["signature"] = signature
		context.metadata["step"] = step

		val message = context.sendMessage(context.getAsMention(true), builder.build())

		for (emote in indexes) {
			message.addReaction(emote).complete();
		}
	}

	override fun onCommandReactionFeedback(context: CommandContext, e: GenericMessageReactionEvent, msg: Message) {
		if (e.user == context.userHandle && e is MessageReactionAddEvent) {
			e.reaction.removeReaction(context.userHandle).complete()
			if (context.metadata.contains("channel")) {
				val channel = context.metadata["channel"] as Long
				val session = context.metadata["session"] as Long
				val signature = context.metadata["signature"] as Long
				var step = context.metadata["step"] as Int
				var answer = if (e.reactionEmote.name == "1⃣") {
					0
				} else if (e.reactionEmote.name == "2⃣") {
					1
				}  else if (e.reactionEmote.name == "3⃣") {
					2
				}  else if (e.reactionEmote.name == "4⃣") {
					3
				}  else if (e.reactionEmote.name == "5⃣") {
					4
				}  else {
					0
				}

				val response = if (e.reactionEmote.name == "⏪") {
					HttpRequest.get("http://api-pt3.akinator.com/ws/cancel_answer.php?base=0&channel=$channel&session=$session&signature=$signature&step=$step")
							.body()
				} else {
					HttpRequest.get("http://api-pt3.akinator.com/ws/answer.php?base=0&channel=$channel&session=$session&signature=$signature&step=$step&answer=$answer")
							.body()
				}

				val xmlJSONObj = XML.toJSONObject(response);

				val jsonPrettyPrintString = xmlJSONObj.toString(4);

				println(jsonPrettyPrintString)

				val jsonResult = JsonParser().parse(jsonPrettyPrintString).obj["RESULT"]

				if (jsonResult["COMPLETION"].string == "KO - TIMEOUT") {
					val builder = EmbedBuilder().apply {
						setTitle("<:akinator:348903800540758017> Akinator")
						setDescription(context.locale.get("AKINATOR_TIMEOUT"))
						setColor(Color(20, 158, 255))
					}

					context.metadata.remove("channel")
					context.metadata.remove("signature")
					context.metadata.remove("session")
					context.metadata.remove("step")

					msg.clearReactions().complete()
					msg.editMessage(builder.build()).complete()

					return
				}
				val jsonAnswer = jsonResult["PARAMETERS"]

				var question = jsonAnswer["QUESTION"].string
				var progression = jsonAnswer["PROGRESSION"].double
				step = jsonAnswer["STEP"].int
				var answers = jsonAnswer["ANSWERS"]["ANSWER"].array

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

					val indexes = listOf("1⃣",
							"2⃣",
							"3⃣",
							"4⃣",
							"5⃣")

					var reactionInfo = ""

					for ((idx, answer) in answers.withIndex()) {
						reactionInfo += indexes[idx] + " ${answer.string}\n"
					}

					val builder = EmbedBuilder().apply {
						setTitle("<:akinator:348903800540758017> Akinator (${context.handle.effectiveName})")
						setThumbnail("https://loritta.website/assets/img/akinator_embed.png")
						setDescription(question + "\n\n$progression% $text\n\n$reactionInfo")
						setColor(Color(20, 158, 255))
					}

					context.metadata["channel"] = channel
					context.metadata["session"] = session
					context.metadata["signature"] = signature
					context.metadata["step"] = step

					msg.editMessage(builder.build()).complete()

					if (msg.reactions.filter { it.emote.name == "⏪"}.count() == 0) {
						if (step > 0) {
							msg.addReaction("⏪").complete()
						}
					} else {
						if (step == 0) {
							msg.reactions.forEach {
								if (it.emote.name == "⏪") {
									it.removeReaction(context.userHandle).complete()
								}
							}
						}
					}
				} else {
					val response = HttpRequest.get("http://api-pt3.akinator.com/ws/list.php?base=0&channel=$channel&session=$session&signature=$signature&step=$step&size=1&max_pic_width=360&max_pic_height=640&mode_question=0")
							.body()

					val xmlJSONObj = XML.toJSONObject(response);

					val jsonPrettyPrintString = xmlJSONObj.toString(4);

					val jsonAnswer = JsonParser().parse(jsonPrettyPrintString).obj["RESULT"]["PARAMETERS"]["ELEMENTS"]["ELEMENT"]

					val builder = EmbedBuilder().apply {
						setTitle("<:akinator:348903800540758017> ${jsonAnswer["NAME"].string}")
						setImage(jsonAnswer["ABSOLUTE_PICTURE_PATH"].string)
						setDescription(jsonAnswer["DESCRIPTION"].string)
						addField("Ranking", "#${jsonAnswer["RANKING"].string}", false)
						setColor(Color(20, 158, 255))
					}

					context.metadata.remove("channel")
					context.metadata.remove("signature")
					context.metadata.remove("session")
					context.metadata.remove("step")

					msg.clearReactions().complete()
					msg.editMessage(builder.build()).complete()
				}
			}
		}
	}
}