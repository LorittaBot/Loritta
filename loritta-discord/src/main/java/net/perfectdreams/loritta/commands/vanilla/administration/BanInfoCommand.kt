package net.perfectdreams.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.isValidSnowflake
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.ErrorResponse
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.discordCommand

object BanInfoCommand {
    fun create(loritta: LorittaDiscord) = discordCommand(loritta, listOf("baninfo", "infoban"), CommandCategory.ADMIN) {
        description { it["commands.moderation.baninfo.description"] }

        arguments {
            argument(ArgumentType.USER) {
                optional = false
            }
        }

        examples {
            listOf("159985870458322944")
        }

        userRequiredPermissions = listOf(Permission.BAN_MEMBERS)
        botRequiredPermissions = listOf(Permission.BAN_MEMBERS)

        executesDiscord {
            val userId = args.getOrNull(0) ?: explainAndExit()

            if (!userId.isValidSnowflake())
                fail(locale["commands.userDoesNotExist", userId])
            
            try {
                val banInformation = userId.let { guild.retrieveBanById(it.toLong()).await() }
                val embed = EmbedBuilder()
                        .setTitle("${Emotes.LORI_COFFEE} ${locale["commands.moderation.baninfo.title"]}")
                        .setThumbnail(banInformation.user.avatarUrl)
                        .addField("${Emotes.LORI_TEMMIE} ${locale["commands.moderation.baninfo.user"]}", "`${banInformation.user.asTag}`", false)
                        .addField("${Emotes.LORI_BAN_HAMMER} ${locale["commands.moderation.baninfo.reason"]}", "`${banInformation.reason}`", false)
                        .setColor(Constants.DISCORD_BLURPLE)
                        .setFooter("Se você deseja desbanir este usuário, aperte no ⚒️!")
                discordMessage.channel.sendMessage(embed.build()).await().also {
                    it.addReaction("⚒").queue()
                }.onReactionAddByAuthor(this) {
                    if (it.reactionEmote.name == "⚒") {
                        guild.unban(userId).queue()
                        reply(
                                LorittaReply(
                                        locale["commands.moderation.unban.successfullyUnbanned"],
                                        Emotes.LORI_BAN_HAMMER
                                )
                        )
                    }
                    return@onReactionAddByAuthor
                }

            } catch (e: ErrorResponseException) {
                if (e.errorResponse == ErrorResponse.UNKNOWN_BAN)
                    fail(locale["commands.moderation.baninfo.banDoesNotExist"])
                throw e
            }
        }
    }
}
