package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.util.*

class AvatarCommand : CommandBase() {
	override fun getDescription(locale: BaseLocale): String {
		return locale.AVATAR_DESCRIPTION.msgFormat()
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.DISCORD
	}

	override fun getUsage(): String {
		return "nome do usuário"
	}

	override fun getExample(): List<String> {
		return Arrays.asList("@Loritta")
	}

	override fun getLabel(): String {
		return "avatar"
	}

	override fun run(context: CommandContext) {
		var getAvatar = context.userHandle

		if (context.message.mentionedUsers.isNotEmpty()) {
			getAvatar = context.message.mentionedUsers[0]
		}
		var embed = EmbedBuilder();
		embed.setColor(Color(114, 137, 218)) // Cor do embed (Cor padrão do Discord)
		embed.setDescription("**${context.locale.AVATAR_CLICKHERE.msgFormat(getAvatar.effectiveAvatarUrl + "?size=2048")}**" + if (getAvatar.id == Loritta.config.clientId) "\n*${context.locale.AVATAR_LORITTACUTE.msgFormat()}* \uD83D\uDE0A" else "");
		embed.setTitle("\uD83D\uDDBC ${getAvatar.name}")
		embed.setImage(getAvatar.effectiveAvatarUrl + "?size=2048")
		context.sendMessage(embed.build());
	}
}