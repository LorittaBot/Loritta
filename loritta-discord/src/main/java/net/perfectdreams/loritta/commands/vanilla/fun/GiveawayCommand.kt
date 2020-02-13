package net.perfectdreams.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.commands.LorittaDiscordCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import java.awt.Color

class GiveawayCommand : LorittaDiscordCommand(arrayOf("giveaway", "sorteio"), CommandCategory.MAGIC) {
	override val discordPermissions = listOf(
			Permission.MESSAGE_MANAGE
	)

	override val canUseInPrivateChannel = false

	override fun getDescription(locale: BaseLocale): String? {
		return locale["commands.fun.giveawaymenu.description"]
	}

	@Subcommand
	suspend fun root(context: DiscordCommandContext, locale: BaseLocale, args: Array<String>) {
		val embed = EmbedBuilder()
				.setTitle("\uD83C\uDF89 ${locale["commands.fun.giveawaymenu.categoryTitle"]}")
				.setThumbnail("https://loritta.website/assets/img/loritta_confetti.png")
				.setDescription("*${locale["commands.fun.giveawaymenu.categoryDescription"]}*\n\n")
				.setColor(Color(200, 20, 217))

		val commands = listOf(
				GiveawaySetupCommand(),
				GiveawayEndCommand(),
				GiveawayRerollCommand()
		)

		for (cmd in commands) {
			val toBeAdded = run {
				val usage = cmd.getUsage(com.mrpowergamerbr.loritta.utils.loritta.getLocaleById(context.config.localeId)).build(context.locale)
				val usageWithinCodeBlocks = if (usage.isNotEmpty()) {
					"`$usage` "
				} else {
					""
				}

				"**${context.config.commandPrefix}${cmd.labels.firstOrNull()}** $usageWithinCodeBlocks» ${cmd.getDescription(com.mrpowergamerbr.loritta.utils.loritta.getLocaleById(context.config.localeId))}\n"
			}

			embed.appendDescription(toBeAdded)
		}

		context.sendMessage(context.getAsMention(true), embed.build())
	}
}
