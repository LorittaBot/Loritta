package net.perfectdreams.loritta.commands.vanilla.`fun`

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import java.awt.Color

class GiveawayCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("giveaway", "sorteio"), CommandCategory.FUN) {
	companion object {
		private const val LOCALE_PREFIX = "commands.command"
	}

	override fun command() = create {
		userRequiredPermissions = listOf(Permission.MESSAGE_MANAGE)

		canUseInPrivateChannel = false

		localizedDescription("$LOCALE_PREFIX.giveawaymenu.description")

		executesDiscord {
			val context = this

			val embed = EmbedBuilder()
					.setTitle("\uD83C\uDF89 ${locale["commands.command.giveawaymenu.categoryTitle"]}")
					.setThumbnail("https://loritta.website/assets/img/loritta_confetti.png")
					.setDescription("*${locale["commands.command.giveawaymenu.categoryDescription"]}*\n\n")
					.setColor(Color(200, 20, 217))

			val commands = listOf(
					GiveawaySetupCommand(loritta),
					GiveawayEndCommand(loritta),
					GiveawayRerollCommand(loritta)
			)

			for (cmd in commands) {
				val toBeAdded = run {
					val usage = cmd.command().usage.build(context.locale)
					val usageWithinCodeBlocks = if (usage.isNotEmpty()) {
						"`$usage` "
					} else {
						""
					}

					"**${context.serverConfig.commandPrefix}${cmd.labels.firstOrNull()}** $usageWithinCodeBlocks» ${cmd.command().description(com.mrpowergamerbr.loritta.utils.loritta.localeManager.getLocaleById(context.serverConfig.localeId))}\n"
				}

				embed.appendDescription(toBeAdded)
			}

			context.sendMessage(context.getUserMention(true), embed.build())
		}
	}
}