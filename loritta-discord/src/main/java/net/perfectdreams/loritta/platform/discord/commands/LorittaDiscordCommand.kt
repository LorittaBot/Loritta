package net.perfectdreams.loritta.platform.discord.commands

import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommand

open class LorittaDiscordCommand(labels: Array<String>, category: CommandCategory) : LorittaCommand(labels, category) {
	open val botPermissions = listOf<Permission>()
	open val discordPermissions = listOf<Permission>()
}