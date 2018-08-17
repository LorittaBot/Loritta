package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import java.util.*

class AvatarCommand : AbstractCommand("avatar", category = CommandCategory.DISCORD) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["AVATAR_DESCRIPTION"]
	}

	override fun getUsage(): String {
		return "nome do usuário"
	}

	override fun getExample(): List<String> {
		return Arrays.asList("@Loritta")
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var getAvatar = context.getUserAt(0)

		if (getAvatar == null) {
			getAvatar = context.userHandle
		}

		var embed = EmbedBuilder();
		embed.setColor(Constants.DISCORD_BLURPLE) // Cor do embed (Cor padrão do Discord)
		var description = "**${context.locale["AVATAR_CLICKHERE", getAvatar.effectiveAvatarUrl + "?size=2048"]}**"

		if (getAvatar.id == Loritta.config.clientId)
			description += "\n*${context.locale["AVATAR_LORITTACUTE"]}* \uD83D\uDE0A"
		if (getAvatar.id == "390927821997998081")
			description += "\n*${context.locale["AVATAR_PantufaCute"]}* \uD83D\uDE0A"

		embed.setDescription(description);
		embed.setTitle("\uD83D\uDDBC ${getAvatar.name}")
		embed.setImage(getAvatar.effectiveAvatarUrl + if (!getAvatar.effectiveAvatarUrl.endsWith(".gif")) "?size=2048" else "")
		context.sendMessage(context.getAsMention(true), embed.build())
	}
}