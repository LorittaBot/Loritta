package net.perfectdreams.loritta.plugin.donatorsostentation.commands

import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.commands.vanilla.magic.LoriToolsCommand
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordCommandContext
import net.perfectdreams.loritta.platform.discord.legacy.entities.jda.JDAUser
import net.perfectdreams.loritta.plugin.donatorsostentation.NitroBoostUtils

object DisableBoostExecutor : LoriToolsCommand.LoriToolsExecutor {
	override val args = "donation boost disable <user>"

	override fun executes(): suspend CommandContext.() -> Boolean = task@{
		if (this.args.getOrNull(0) != "donation")
			return@task false
		if (this.args.getOrNull(1) != "boost")
			return@task false
		if (this.args.getOrNull(2) != "disable")
			return@task false

		val context = this.checkType<DiscordCommandContext>(this)

		val user = context.user(3) ?: run {
			context.sendMessage("Usuário inexistente!")
			return@task true
		}
		user as JDAUser

		val member = context.discordMessage.guild.getMember(user.handle) ?: run {
			context.sendMessage("Usuário não está na guild atual!")
			return@task true
		}

		NitroBoostUtils.onBoostDeactivate(member)

		context.reply(
				LorittaReply(
						"Vantagens de Booster Desativadas!"
				)
		)
		return@task true
	}
}