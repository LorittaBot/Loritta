package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import java.util.*

class InviteInfoCommand : AbstractCommand("inviteinfo", category = CommandCategory.DISCORD) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["INVITEINFO_Description"]
	}

	override fun getUsage(): String {
		return "ID do invite"
	}

	override fun getExample(): List<String> {
		return Arrays.asList("V7Kbh4z", "https://discord.gg/ZWt5mKB", "https://discord.gg/coredasantigas")
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var inviteId = context.args.getOrNull(0)

		if (inviteId != null) {
			if (!inviteId.matches(Regex("[A-z0-9]+"))) {
				inviteId = inviteId.replace("discord.gg/", "")
						.replace("https://", "")
						.replace("http://", "")
			}

			val inviteBody = HttpRequest.get("https://canary.discordapp.com/api/v6/invite/$inviteId?with_counts=true")
					.userAgent(Constants.USER_AGENT)
					.body()

			val payload = jsonParser.parse(inviteBody).obj

			val code = payload["code"]

			if (code.asJsonPrimitive.isNumber && code.int == 10006) {
				// Invite não existe!
				context.reply(
						LoriReply(
								locale["INVITEINFO_InviteDoesNotExist", inviteId.stripCodeMarks()],
								Constants.ERROR
						)
				)
			} else {
				val guild = payload["guild"].obj
				val verificationLevel = guild["verification_level"].int
				val name = guild["name"].string
				val splash = guild["splash"].nullString
				val id = guild["id"].string
				val icon = guild["icon"].nullString
				val inviter = payload["inviter"].nullObj
				val channel = payload["channel"].obj
				val features = guild["features"].array
				val approxMemberCount = payload["approximate_member_count"].int
				val approxPresenceCount = payload["approximate_presence_count"].int

				val embed = EmbedBuilder()

				embed.setColor(Constants.DISCORD_BLURPLE) // Cor do embed (Cor padrão do Discord)
				embed.setTitle("<:discord:314003252830011395> $name", null) // Nome da Guild

				embed.addField("💻 ID", id, true) // ID da Guild
				embed.addField("\uD83D\uDC6E ${locale["SERVERINFO_VerificationLevel"]}", verificationLevel.toString(), true) // ID da Guild
				embed.addField("\uD83D\uDC65 ${locale["SERVERINFO_MEMBERS"]}", "\uD83D\uDC81 **${locale["INVITEINFO_Active"]}:** ${approxPresenceCount}\n\uD83D\uDE34 **Offline:** ${approxMemberCount}", true)

				if (features.size() == 0) {
					embed.addField("✨ ${locale["SERVERINFO_Features"]}", "${locale["INVITEINFO_None"]}...", true) // ID da Guild
				} else {
					embed.addField("✨ ${locale["SERVERINFO_Features"]}", features.joinToString(", ", transform = { it.string }), true) // ID da Guild
				}

				if (icon != null) {
					embed.setThumbnail("https://cdn.discordapp.com/icons/$id/$icon.png")
				}

				if (splash != null) {
					embed.setImage("https://cdn.discordapp.com/splashes/$id/$splash.png?size=1024")
				}

				embed.addField("\uD83D\uDDE3 ${locale["INVITEINFO_ChannelInvite"]}", "`#${channel["name"].string}` (${channel["id"].string})", true)

				if (inviter != null) {
					val username = inviter["username"].string
					val discriminator = inviter["discriminator"].string
					val id = inviter["id"].string

					embed.addField("\uD83D\uDC4B ${locale["INVITEINFO_WhoInvited"]}", "`$username#$discriminator` ($id)", true)
				}

				context.sendMessage(context.getAsMention(true), embed.build()) // phew, agora finalmente poderemos enviar o embed!
			}
		} else {
			context.explain()
		}
	}
}