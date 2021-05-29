package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.nullObj
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.encodeToUrl
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.extensions.isValidUrl
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply

class InviteInfoCommand : AbstractCommand("inviteinfo", category = CommandCategory.DISCORD) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.inviteinfo.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.inviteinfo.examples")

	// TODO: Fix Usage

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		var inviteId = context.args.getOrNull(0)

		if (inviteId != null) {
			if (!inviteId.matches(Regex("[A-z0-9]+"))) {
				if (inviteId.isValidUrl()) {
					// If it is a valid URL, try getting the last element of the URL
					// https://discord.gg/loritta
					// https://discord.com/invite/jjjM5tPwS9
					inviteId = inviteId.substringAfterLast("/")
				} else {
					context.explain()
					return
				}
			}

			val inviteBody = HttpRequest.get("https://canary.discordapp.com/api/v6/invite/${inviteId.encodeToUrl()}?with_counts=true")
					.userAgent(Constants.USER_AGENT)
					.body()

			val payload = JsonParser.parseString(inviteBody).obj

			val code = payload["code"]

			if (code.asJsonPrimitive.isNumber && code.int == 10006) {
				// Invite não existe!
				context.reply(
						LorittaReply(
								context.locale["commands.command.inviteinfo.doesntExists", inviteId.stripCodeMarks()],
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
				embed.addField("\uD83D\uDC6E ${locale["commands.command.serverinfo.verificationLevel"]}", verificationLevel.toString(), true) // ID da Guild
				embed.addField("\uD83D\uDC65 ${locale["commands.command.serverinfo.members"]}", "\uD83D\uDC81 **${locale["commands.command.inviteinfo.active"]}:** ${approxPresenceCount}\n\uD83D\uDE34 **Offline:** $approxMemberCount", true)

				if (features.size() == 0) {
					embed.addField("✨ ${locale["commands.command.serverinfo.features"]}", locale["commands.command.inviteinfo.none"], true) // ID da Guild
				} else {
					embed.addField("✨ ${locale["commands.command.serverinfo.features"]}", features.joinToString(", ", transform = { it.string }), true) // ID da Guild
				}

				if (icon != null) {
					embed.setThumbnail("https://cdn.discordapp.com/icons/$id/$icon.png")
				}

				if (splash != null) {
					embed.setImage("https://cdn.discordapp.com/splashes/$id/$splash.png?size=1024")
				}

				embed.addField("\uD83D\uDDE3 ${locale["commands.command.inviteinfo.channelInvite"]}", "`#${channel["name"].string}` (${channel["id"].string})", true)

				if (inviter != null) {
					val username = inviter["username"].string
					val discriminator = inviter["discriminator"].string
					val id = inviter["id"].string

					embed.addField("\uD83D\uDC4B ${locale["commands.command.inviteinfo.whoInvited"]}", "`$username#$discriminator` ($id)", true)
				}

				val discordGuild = lorittaShards.queryGuildById(id)

				if (discordGuild != null) {
					embed.setFooter("\uD83D\uDE0A ${context.locale["commands.command.inviteinfo.inThisServer"]}")
				} else {
					embed.setFooter("\uD83D\uDE2D ${context.locale["commands.command.inviteinfo.notOnTheServer"]}")
				}

				context.sendMessage(context.getAsMention(true), embed.build()) // phew, agora finalmente poderemos enviar o embed!
			}
		} else {
			context.explain()
		}
	}
}