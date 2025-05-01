package net.perfectdreams.loritta.morenitta.commands.vanilla.`fun`

import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.servers.Giveaway
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.Giveaways
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.utils.isValidSnowflake
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks
import org.jetbrains.exposed.sql.and

class GiveawayRerollCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(loritta, listOf("giveaway reroll", "sorteio reroll"), net.perfectdreams.loritta.common.commands.CommandCategory.FUN) {
	companion object {
		private const val LOCALE_PREFIX = "commands.command"
	}

	override fun command() = create {
		userRequiredPermissions = listOf(Permission.MESSAGE_MANAGE)

		canUseInPrivateChannel = false

		localizedDescription("$LOCALE_PREFIX.giveawayreroll.description")

		executesDiscord {
			// This is an empty command because this command is still used on the "GiveawayCommand" command
			// This command is NOT registered on the command map because it has been replaced by the new interactions version
		}
	}
}