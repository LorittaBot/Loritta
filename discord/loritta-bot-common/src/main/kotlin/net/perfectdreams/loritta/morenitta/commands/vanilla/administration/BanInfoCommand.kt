package net.perfectdreams.loritta.morenitta.commands.vanilla.administration

import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.MessageEdit
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.isValidSnowflake
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.requests.ErrorResponse
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.interactions.CommandContextCompat
import net.perfectdreams.loritta.morenitta.interactions.vanilla.moderation.BanInfoCommand

class BanInfoCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(loritta, listOf("baninfo", "infoban", "checkban"), net.perfectdreams.loritta.common.commands.CommandCategory.MODERATION) {
    override fun command() = create {
        localizedDescription("commands.command.baninfo.description")
        localizedExamples("commands.command.baninfo.examples")

        arguments {
            argument(ArgumentType.USER) {
                optional = false
            }
        }

        userRequiredPermissions = listOf(Permission.BAN_MEMBERS)
        botRequiredPermissions = listOf(Permission.BAN_MEMBERS)

        executesDiscord {
            val userId = args.getOrNull(0) ?: explainAndExit()

            val contextCompat: CommandContextCompat = CommandContextCompat.LegacyDiscordCommandContextCompat(this)

            BanInfoCommand.executeCompat(
                contextCompat,
                userId
            )
        }
    }
}
