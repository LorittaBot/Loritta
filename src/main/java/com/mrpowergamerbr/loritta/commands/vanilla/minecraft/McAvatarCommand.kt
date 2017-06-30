package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import java.awt.Color

class McAvatarCommand : CommandBase() {
	override fun getLabel(): String {
		return "mcavatar"
	}

	override fun getDescription(): String {
		return "Mostra o avatar de uma conta do Minecraft, caso a conta não exista ele irá mostrar a skin padrão (Steve)"
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.MINECRAFT
	}

	override fun getUsage(): String {
		return "nickname"
	}

	override fun getExample(): List<String> {
		return listOf("Monerk")
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			val nickname = context.args[0]

			val bufferedImage = LorittaUtils.downloadImage("https://crafatar.com/avatars/$nickname?size=128&overlay")
			val builder = EmbedBuilder()

			builder.setColor(Color.DARK_GRAY)
			builder.setImage("attachment://avatar.png")

			builder.setTitle("<:grass:330435576392318978> Avatar de " + nickname)

			val message = MessageBuilder().append(context.getAsMention(true)).setEmbed(builder.build()).build()

			context.sendFile(bufferedImage, "avatar.png", message)
		} else {
			context.explain()
		}
	}

}