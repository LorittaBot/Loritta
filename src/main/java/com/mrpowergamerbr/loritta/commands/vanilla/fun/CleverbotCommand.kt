package net.pocketdreams.loriplugins.cleverbot.commands

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.common.cache.CacheBuilder
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.JSON_PARSER
import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.getOrCreateWebhook
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import org.json.XML
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.TimeUnit

class CleverbotCommand : AbstractCommand("cleverbot", category = CommandCategory.FUN) {
	companion object {
		val cleverbots = CacheBuilder.newBuilder().expireAfterAccess(5L, TimeUnit.MINUTES).maximumSize(100).build<String, Cleverbot>().asMap()
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale["CLEVERBOT_DESCRIPTION"]
	}

	override fun getExample(): List<String> {
		return Arrays.asList("Como vai você?")
	}

	override fun hasCommandFeedback(): Boolean {
		return false
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val cleverbot = cleverbots.getOrDefault(context.guild.id, Cleverbot())
			cleverbots[context.guild.id] = cleverbot
			val query = context.args.joinToString(" ")

			val cleverbotResponse = cleverbot.sendStimulus(query)

			val response = cleverbotResponse.response.escapeMentions()
			val emotion = cleverbotResponse.emotion

			val emoji = when (emotion) {
				"serious" -> "\uD83D\uDE10"
				"concerned" -> "\uD83D\uDE1F"
				"flirty" -> "\uD83D\uDE18"
				"happy" -> "\uD83D\uDE04"
				"tired" -> "\uD83D\uDE2A"
				"gentle" -> "\uD83D\uDE0A"
				"sarcastic" -> "\uD83D\uDE43"
				"curious" -> "\uD83E\uDD14"
				"rude" -> "\uD83D\uDE44"
				"assertive" -> "\uD83D\uDE11"
				"questioning" -> "\uD83E\uDD14"
				"thoughtful" -> "\uD83E\uDD14"
				"amused" -> "\uD83D\uDE32"
				"agreeable" -> "\uD83D\uDE42"
				"excited" -> "\uD83D\uDE04"
				"calm" -> "\uD83D\uDE0C"
				"lazy" -> "\uD83D\uDE34"
				"jumpy" -> "\uD83D\uDC42"
				"bored" -> "\uD83D\uDE12"
				"tongue out" -> "\uD83D\uDE1B"
				"argumentative" -> "\uD83D\uDCAC"
				"naughty" -> "\uD83D\uDE0F"
				"smug" -> "\uD83D\uDE0F"
				"nice" -> "\uD83D\uDE0A"
				"doubting" -> "\uD83E\uDD14"
				"determined" -> "\uD83D\uDE00"
				"proud" -> "\uD83D\uDE24"
				"love" -> "❤"
				"joking" -> "\uD83D\uDE0A"
				"contemplative" -> "\uD83D\uDE14"
				"mocking" -> "\uD83D\uDE02"
				"unsure" -> "\uD83D\uDE15"
				"sure" -> "\uD83D\uDE0B"
				"alert" -> "\uD83D\uDEA8"
				"apologetic" -> "\uD83D\uDE47"
				"dancing" -> "\uD83D\uDC83"
				"very happy" -> "\uD83D\uDE04"
				"didactic" -> "✏"
				"cool" -> "\uD83D\uDE0E"
				"furious" -> "\uD83D\uDE20"
				"forgetful" -> "\uD83E\uDD37"
				"distracted" -> "\uD83E\uDD37"
				"angry" -> "\uD83D\uDE21"
				"hatred" -> "\uD83D\uDE21"
				"grumpy" -> "\uD83D\uDE3E"
				"worried" -> "\uD83D\uDE1F"
				"nosey" -> "\uD83D\uDC7A"
				"shy" -> "\uD83D\uDE33"
				"victorious" -> "✌"
				"sympathy" -> "\uD83E\uDD17"
				"stubborn" -> "\uD83D\uDE12"
				"singing" -> "\uD83D\uDE17"
				else -> ""
			}

			val webhook = getOrCreateWebhook(context.event.textChannel!!, "Cleverbot")
			context.sendMessage(webhook, DiscordMessage.builder()
					.username(context.locale["CLEVERBOT_PANTUFA"])
					.content(context.getAsMention(true) + "$response $emoji")
					.avatarUrl("https://loritta.website/assets/img/pantufa_avatar.png")
					.build())
			return
		} else {
			context.explain()
		}
	}

	class Cleverbot {
		private var sessionId = ""
		private val conversations = arrayListOf<String>("", "", "", "", "", "", "")
		private var lineRef = ""
		private var prevref = ""

		fun sendStimulus(stimulus: String): CleverbotResponse {
			// val stimulus = "Do you like Undertale?";
			val icognoCheck = "kk-eae-men-o-SAM-e-brabo-filler-"
			val vtext8 = conversations[6]
			val vtext7 = conversations[5]
			val vtext6 = conversations[4]
			val vtext5 = conversations[3]
			val vtext4 = conversations[2]
			val vtext3 = conversations[1]
			val vtext2 = conversations[0]

			val body = HttpRequest.post("http://app.cleverbot.com/webservicexml_ais_AYA")
					.userAgent("Dalvik/2.1.0 (Linux; U; Android 7.1.2; MotoG3-TE Build/NJH47F)")
					.send(String.format("stimulus=%s&sessionid=%s&vtext8=%s&vtext7=%s&vtext6=%s&vtext5=%s&vtext4=%s&vtext3=%s&vtext2=%s&prevref=%s&lineRef=%s&icognoCheck=$icognoCheck&icognoID=cleverandroid",
							URLEncoder.encode(stimulus, "UTF-8"),
							URLEncoder.encode(sessionId, "UTF-8"),
							URLEncoder.encode(vtext8, "UTF-8"),
							URLEncoder.encode(vtext7, "UTF-8"),
							URLEncoder.encode(vtext6, "UTF-8"),
							URLEncoder.encode(vtext5, "UTF-8"),
							URLEncoder.encode(vtext4, "UTF-8"),
							URLEncoder.encode(vtext3, "UTF-8"),
							URLEncoder.encode(vtext2, "UTF-8"),
							URLEncoder.encode(prevref, "UTF-8"),
							URLEncoder.encode(lineRef, "UTF-8")
					))
					.body()

			val xmlJSONObj = JSON_PARSER.parse(XML.toJSONObject(body).toString())
			val session = xmlJSONObj["webservicexml"]["session"].obj

			val response = session["response"].string
			lineRef = session["lineRef"].string
			prevref = session["prevref"].string
			val emotion = if (session.has("emotion")) {
				session["emotion"].string
			} else {
				""
			}

			sessionId = session["sessionid"].string

			conversations.removeAt(conversations.size - 2)
			conversations.removeAt(conversations.size - 1)
			conversations.add(stimulus)
			conversations.add(response)

			return CleverbotResponse(response, emotion)
		}

		class CleverbotResponse(val response: String, val emotion: String)
	}
}