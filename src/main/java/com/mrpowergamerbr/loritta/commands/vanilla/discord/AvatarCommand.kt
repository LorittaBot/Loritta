package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.util.*

class AvatarCommand : CommandBase() {
	override fun getDescription(): String {
		return "Pega o avatar de um usuário do Discord"
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
		embed.setDescription("**Clique [aqui](${getAvatar.effectiveAvatarUrl + "?size=2048"}) para baixar a imagem!**" + if (getAvatar.id == Loritta.config.clientId) "\n*Eu sei que eu sou muito fofa!* \uD83D\uDE0A" else "");
		embed.setTitle("\uD83D\uDDBC ${getAvatar.name}")
		embed.setImage(getAvatar.effectiveAvatarUrl + "?size=2048")
		context.sendMessage(embed.build());
	}
}