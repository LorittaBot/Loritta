package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.humanize
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.time.Instant
import java.time.ZoneId

class UserInfoCommand : CommandBase() {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("USERINFO_DESCRIPTION")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.DISCORD
	}

	override fun getLabel(): String {
		return "userinfo"
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext) {
		val user = if (context.message.mentionedUsers.isNotEmpty()) {
			context.message.mentionedUsers[0]
		} else {
			if (context.args.isNotEmpty()) {
				LorittaUtils.getUserFromContext(context, 0)
			} else {
				null
			}
		}

		if (user == null) {
			this.explain(context);
			return
		}

		if (!context.guild.isMember(user)) {
			context.sendMessage(LorittaUtils.ERROR + " **|** " + context.locale.get("USERINFO_NOT_MEMBER"))
			return
		}

		val member = context.guild.getMember(user)

		val embed = EmbedBuilder()

		embed.apply {
			embed.setThumbnail(member.user.effectiveAvatarUrl)
			embed.setTitle("<:discord:314003252830011395> ${member.effectiveName}", null)
			embed.setColor(Color(114, 137, 218)) // Cor do embed (Cor padrão do Discord)
			embed.addField("\uD83D\uDCBB " + context.locale.get("USERINFO_TAG_DO_DISCORD"), "${member.user.name}#${member.user.discriminator}", true)
			embed.addField("\uD83D\uDCBB " + context.locale.get("USERINFO_ID_DO_DISCORD"), member.user.id, true)
			embed.addField("\uD83D\uDCC5 " + context.locale.get("USERINFO_ACCOUNT_CREATED"), member.user.creationTime.humanize(), true)
			embed.addField("\uD83C\uDF1F " + context.locale.get("USERINFO_ACCOUNT_JOINED"), member.joinDate.humanize(), true)

			val sharedServers = lorittaShards.getMutualGuilds(member.user)

			var servers = sharedServers.joinToString(separator = ", ", transform = { "**${it.name}**"})

			if (servers.length >= 1024) {
				servers = servers.substring(0..1020) + "...";
			}

			embed.addField("\uD83C\uDF0E " + context.locale.get("USERINFO_SHARED_SERVERS") + " (${sharedServers.size})", servers, true)
			embed.addField("\uD83D\uDCE1 " + context.locale.get("USERINFO_STATUS"), member.onlineStatus.name, true)

			val roles = member.roles.joinToString(separator = ", ", transform = { "**${it.name}**"});

			embed.addField("\uD83D\uDCBC " + context.locale.get("USERINFO_ROLES"), if (roles.isNotEmpty()) roles else context.locale.get("USERINFO_NO_ROLE") + " \uD83D\uDE2D", true)

			val profile = loritta.getLorittaProfileForUser(user.id)

			val offset = Instant.ofEpochMilli(profile.lastMessageSent).atZone(ZoneId.systemDefault()).toOffsetDateTime();

			embed.addField("\uD83D\uDC40 " + context.locale.get("USERINFO_LAST_SEEN"), offset.humanize(), true)
		}

		context.sendMessage(context.getAsMention(true), embed.build()) // phew, agora finalmente poderemos enviar o embed!
	}
}