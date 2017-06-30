package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.dv8tion.jda.core.Permission
import java.util.*

class LimparCommand : CommandBase() {
	override fun getLabel(): String {
		return "limpar"
	}

	override fun getDescription(): String {
		return "Limpa o chat do canal de texto atual."
	}

	override fun getUsage(): String {
		return "QuantasMensagens"
	}

	override fun getExample(): List<String> {
		return Arrays.asList("10", "25")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.ADMIN
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MESSAGE_MANAGE)
	}

	override fun run(context: CommandContext) {
		val toClear = Integer.parseInt(context.args[0])
		context.event.textChannel.history.retrievePast(toClear).complete().forEach { msg -> msg.delete().complete() }

		context.sendMessage("Chat limpo por " + context.handle.asMention + "!")
	}
}