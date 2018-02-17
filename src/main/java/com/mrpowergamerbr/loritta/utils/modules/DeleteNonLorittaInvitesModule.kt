package com.mrpowergamerbr.loritta.utils.modules

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.userdata.InviteBlockerConfig
import com.mrpowergamerbr.loritta.userdata.PermissionsConfig
import com.mrpowergamerbr.loritta.utils.*
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import java.util.regex.Pattern

object DeleteNonLorittaInvitesModule {
	fun checkForInviteLinks(message: Message, guild: Guild, lorittaProfile: GuildLorittaUser, permissionsConfig: PermissionsConfig, inviteBlockerConfig: InviteBlockerConfig): Boolean {
		if (message.textChannel.id != "367373409761493004" && message.textChannel.id != "367389748807073815")
			return false

		val content = message.contentRaw
				.replace("\u200B", "")
				.replace("\\", "")

		val matcher = Pattern.compile("[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)").matcher(content)

		while (matcher.find()) {
			var url = matcher.group()
			if (url.contains("discord") && url.contains("gg")) {
				url = "discord.gg" + matcher.group(1).replace(".", "")
			}
			val inviteId = MiscUtils.getInviteId("http://$url") ?: MiscUtils.getInviteId("https://$url") ?: continue

			if (inviteId == "attachments")
				continue

			val asMention = message.author.asMention
			val name = message.author.name
			val effectiveName = message.member.effectiveName

			val discordResult = HttpRequest.get("https://canary.discordapp.com/api/v6/invite/$inviteId?with_counts=true")
					.userAgent(Constants.USER_AGENT)
					.body()

			val json = JSON_PARSER.parse(discordResult).obj

			val code = json["code"].string
			if (code == "10006")
				return false
			val guildId = json["guild"]["id"].string

			val sentGuild = lorittaShards.getGuildById(guildId)

			if (sentGuild == null) {
				if (inviteBlockerConfig.deleteMessage && guild.selfMember.hasPermission(message.textChannel, Permission.MESSAGE_MANAGE))
					message.delete().queue()

				try {
					message.author.openPrivateChannel().complete().sendMessage("**Você não pode divulgar convites de servidores que não possuem a Loritta (ou seja, eu)!** \uD83D\uDE45\n\n*Afinal, porque você está no meu servidor sendo que você nem me usa no seu servidor?* <:LorittaThinking3:345642819353837568>").complete()
				} catch (e: Exception) {}

				val staffChannel = guild.getTextChannelById("358774895850815488") ?: return true

				staffChannel.sendMessage("""$asMention tentou divulgar um servidor que não possui a Loritta! (Ou seja, eu! <:lori_yum:414222275223617546>)
					|**Usuário:** `${name}#${message.author.discriminator}`
					|**ID:** `${message.author.id}`
					|**ID da guild:** `${guildId}`
					|**Nome da guild:** `${json["guild"]["name"].string}`
					|**Invite ID:** ${inviteId}
				""".trimMargin()).complete()
				return true
			}
		}
		return false
	}
}