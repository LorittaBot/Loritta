package net.perfectdreams.loritta.morenitta.commands.vanilla.administration

import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.utils.Constants
import java.util.regex.Pattern

class RenameEmojiCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(loritta, listOf("renameemoji", "renomearemoji"), net.perfectdreams.loritta.common.commands.CommandCategory.MODERATION) {
    companion object {
        private const val LOCALE_PREFIX = "commands.command"
    }

    override fun command() = create {
        localizedDescription("$LOCALE_PREFIX.renameemoji.description")
        localizedExamples("$LOCALE_PREFIX.renameemoji.examples")

        usage {
            arguments {
                argument(ArgumentType.EMOTE) {}
                argument(ArgumentType.TEXT) {}
            }
        }

        canUseInPrivateChannel = false

        botRequiredPermissions = listOf(Permission.MANAGE_GUILD_EXPRESSIONS)
        userRequiredPermissions = listOf(Permission.MANAGE_GUILD_EXPRESSIONS)

        executesDiscord {
            val context = this

            if (args.isNotEmpty()) {
                // This will verify if have emotes in the message
                val emote = context.emote(0)

                if  (emote == null) {
                    context.reply(
                            LorittaReply(
                                    locale["$LOCALE_PREFIX.renameemoji.emoteNotFound"],
                                    Constants.ERROR
                            )
                    )
                    return@executesDiscord
                }
                val argumentChangeName = args[1]
                val regexPattern = Pattern.compile("[A-z0-9_]+")
                val regexMatch = regexPattern.matcher(argumentChangeName)
                val emoteName = if (argumentChangeName.length >= 32) {
                    context.reply(
                            LorittaReply(
                                    locale["$LOCALE_PREFIX.renameemoji.emoteNameLength32Error"],
                                    Constants.ERROR
                            )
                    )
                    return@executesDiscord
                } else if (2 >= argumentChangeName.length) {
                    context.reply(
                            LorittaReply(
                                    locale["$LOCALE_PREFIX.renameemoji.emoteNameLength2Error"],
                                    Constants.ERROR
                            )
                    )
                    return@executesDiscord
                } else if (!regexMatch.matches()) {
                    context.reply(
                            LorittaReply(
                                    locale["$LOCALE_PREFIX.renameemoji.emoteNameSpecialChar"],
                                    Constants.ERROR
                            )
                    )
                    return@executesDiscord
                } else {
                    argumentChangeName
                }

                if (emote.canInteract(context.guild.selfMember)) {
                    emote.manager.setName(emoteName).queue()
                    context.reply(
                            LorittaReply(
                                    locale["$LOCALE_PREFIX.renameemoji.renameSucess"],
                                    emote.asMention
                            )
                    )
                }
            } else {
                context.explain()
            }
        }
    }
}
