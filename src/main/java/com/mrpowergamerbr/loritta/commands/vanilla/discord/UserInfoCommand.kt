package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.humanize
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.LORITTA_SHARDS
import net.dv8tion.jda.core.EmbedBuilder
import java.time.Instant
import java.time.ZoneId

class UserInfoCommand : CommandBase("userinfo") {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("USERINFO_DESCRIPTION")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.DISCORD
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext) {
		var user = LorittaUtils.getUserFromContext(context, 0)

		if (user == null) {
			user = context.userHandle
		}

		val member = if (context.guild.isMember(user)) {
			context.guild.getMember(user)
		} else {
			null
		}

		val embed = EmbedBuilder()

		embed.apply {
			setThumbnail(user.effectiveAvatarUrl)
			var nickname = user.name

			if (member != null) {
				nickname = member.effectiveName
			}

			setTitle("<:discord:314003252830011395> $nickname", null)
			setColor(Constants.DISCORD_BURPLE) // Cor do embed (Cor padrão do Discord)

			val lorittaProfile = loritta.getLorittaProfileForUser(user.id)
			val usernameChanges = lorittaProfile.usernameChanges
			if (usernameChanges.isEmpty()) {
				usernameChanges.add(LorittaProfile.UsernameChange(user.creationTime.toEpochSecond() * 1000, user.name, user.discriminator))
			}

			val sortedChanges = lorittaProfile.usernameChanges.sortedBy { it.changedAt }
			var alsoKnownAs = "**" + context.locale.get("USERINFO_ALSO_KNOWN_AS") + "**\n" + sortedChanges.joinToString(separator = "\n",  transform = {
				"${it.username}#${it.discriminator} (" + Instant.ofEpochMilli(it.changedAt).atZone(ZoneId.systemDefault()).toOffsetDateTime().humanize() + ")"
			})
			// Verificar tamanho do "alsoKnownAs" e, se necessário, cortar
			var alsoKnownAsLines = alsoKnownAs.split("\n").reversed()

			var aux = mutableListOf<String>()

			var length = 0
			for (line in alsoKnownAsLines) {
				if (length + line.length >= 2000) {
					break
				}
				aux.add(line)
				length += line.length
			}
			setDescription(aux.reversed().joinToString(separator = "\n"))

			addField("\uD83D\uDCBB " + context.locale.get("USERINFO_TAG_DO_DISCORD"), "${user.name}#${user.discriminator}", true)
			addField("\uD83D\uDCBB " + context.locale.get("USERINFO_ID_DO_DISCORD"), user.id, true)
			addField("\uD83D\uDCC5 " + context.locale.get("USERINFO_ACCOUNT_CREATED"), user.creationTime.humanize(), true)
			if (member != null)
				addField("\uD83C\uDF1F " + context.locale.get("USERINFO_ACCOUNT_JOINED"), member.joinDate.humanize(), true)

			val sharedServers = LORITTA_SHARDS.getMutualGuilds(user)

			var servers = sharedServers.joinToString(separator = ", ", transform = { "${it.name}"})

			if (servers.length >= 1024) {
				servers = servers.substring(0..1020) + "...";
			}

			embed.addField("\uD83C\uDF0E " + context.locale["USERINFO_SHARED_SERVERS"] + " (${sharedServers.size})", servers, true)
			if (member != null) {
				addField("\uD83D\uDCE1 " + context.locale["USERINFO_STATUS"], member.onlineStatus.name, true)

				val roles = member.roles.joinToString(separator = ", ", transform = { "${it.name}" });

				addField("\uD83D\uDCBC " + context.locale["USERINFO_ROLES"], if (roles.isNotEmpty()) roles else context.locale.get("USERINFO_NO_ROLE") + " \uD83D\uDE2D", true)
			}

			val profile = loritta.getLorittaProfileForUser(user.id)

			val offset = Instant.ofEpochMilli(profile.lastMessageSent).atZone(ZoneId.systemDefault()).toOffsetDateTime();

			addField("\uD83D\uDC40 " + context.locale["USERINFO_LAST_SEEN"], offset.humanize(), true)
		}

		context.sendMessage(context.getAsMention(true), embed.build()) // phew, agora finalmente poderemos enviar o embed!
	}
}