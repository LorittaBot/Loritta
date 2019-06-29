package net.perfectdreams.loritta.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.QuirkyStuff
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.commands.LorittaDiscordCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext

class LoriToolsQuirkyStuffCommand : LorittaDiscordCommand(arrayOf("loritoolsqs"), CommandCategory.MAGIC) {
	override val onlyOwner: Boolean
		get() = true

	@Subcommand(["enable_boost"])
	suspend fun enableBoost(context: DiscordCommandContext, args: Array<String>) {
		val user = context.getUserAt(1) ?: run {
			context.sendMessage("Usuário inexistente!")
			return
		}

		val member = context.discordGuild!!.getMember(user) ?: run {
			context.sendMessage("Usuário não está na guild atual!")
			return
		}

		QuirkyStuff.onBoostActivate(member)
	}

	@Subcommand(["disable_boost"])
	suspend fun disableBoost(context: DiscordCommandContext, args: Array<String>) {
		val user = context.getUserAt(1) ?: run {
			context.sendMessage("Usuário inexistente!")
			return
		}

		val member = context.discordGuild!!.getMember(user) ?: run {
			context.sendMessage("Usuário não está na guild atual!")
			return
		}

		QuirkyStuff.onBoostDeactivate(member)
	}
}