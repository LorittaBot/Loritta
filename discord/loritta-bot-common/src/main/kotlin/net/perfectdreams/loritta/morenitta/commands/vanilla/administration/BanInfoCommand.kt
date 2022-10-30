package net.perfectdreams.loritta.morenitta.commands.vanilla.administration

import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.isValidSnowflake
import net.perfectdreams.loritta.morenitta.utils.onReactionAddByAuthor
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks
import net.perfectdreams.loritta.deviousfun.EmbedBuilder
import dev.kord.common.entity.Permission
import dev.kord.rest.json.JsonErrorCode
import dev.kord.rest.request.KtorRequestException
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.common.utils.Emotes

class BanInfoCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(
    loritta,
    listOf("baninfo", "infoban", "checkban"),
    net.perfectdreams.loritta.common.commands.CommandCategory.MODERATION
) {
    override fun command() = create {
        localizedDescription("commands.command.baninfo.description")
        localizedExamples("commands.command.baninfo.examples")

        arguments {
            argument(ArgumentType.USER) {
                optional = false
            }
        }

        userRequiredPermissions = listOf(Permission.BanMembers)
        botRequiredPermissions = listOf(Permission.BanMembers)

        executesDiscord {
            val userId = args.getOrNull(0) ?: explainAndExit()

            if (!userId.isValidSnowflake())
                fail(locale["commands.userDoesNotExist", userId.stripCodeMarks()])

            try {
                val banInformation = userId.let { guild.retrieveBanById(it.toLong()) }
                val banReason = banInformation.reason ?: locale["commands.command.baninfo.noReasonSpecified"]
                val embed = EmbedBuilder()
                    .setTitle("${Emotes.LORI_COFFEE} ${locale["commands.command.baninfo.title"]}")
                    .setThumbnail(banInformation.user.avatarUrl)
                    .addField(
                        "${Emotes.LORI_TEMMIE} ${locale["commands.command.baninfo.user"]}",
                        "`${banInformation.user.asTag}`",
                        false
                    )
                    .addField(
                        "${Emotes.LORI_BAN_HAMMER} ${locale["commands.command.baninfo.reason"]}",
                        "`${banReason}`",
                        false
                    )
                    .setColor(Constants.DISCORD_BLURPLE)
                    .setFooter("Se você deseja desbanir este usuário, aperte no ⚒️!")
                discordMessage.channel.sendMessage(embed.build()).also {
                    runCatching { it.addReaction("⚒") }
                }.onReactionAddByAuthor(this) {
                    if (it.reactionEmote.name == "⚒") {
                        runCatching { guild.unban(userId) }
                        reply(
                            LorittaReply(
                                locale["commands.command.unban.successfullyUnbanned"],
                                Emotes.LORI_BAN_HAMMER
                            )
                        )
                    }
                    return@onReactionAddByAuthor
                }

            } catch (e: KtorRequestException) {
                if (e.error?.code == JsonErrorCode.UnknownBan)
                    fail(locale["commands.command.baninfo.banDoesNotExist"])
                throw e
            }
        }
    }
}
