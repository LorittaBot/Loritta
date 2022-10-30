package net.perfectdreams.loritta.morenitta.commands.vanilla.administration

import dev.kord.common.entity.Permission
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.utils.Constants

class RenameChannelCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(
    loritta,
    listOf("renamechannel", "renomearcanal"),
    net.perfectdreams.loritta.common.commands.CommandCategory.MODERATION
) {
    companion object {
        private const val LOCALE_PREFIX = "commands.command"
    }

    override fun command() = create {
        localizedDescription("$LOCALE_PREFIX.renamechannel.description")
        localizedExamples("$LOCALE_PREFIX.renamechannel.examples")

        usage {
            arguments {
                argument(ArgumentType.TEXT) {}
                argument(ArgumentType.TEXT) {}
            }
        }

        canUseInPrivateChannel = false

        botRequiredPermissions = listOf(Permission.ManageChannels)
        userRequiredPermissions = listOf(Permission.ManageChannels)

        executesDiscord {
            val context = this

            if (context.args.isEmpty()) explainAndExit()

            val textChannel = context.textChannel(0)
            val voiceChannel = context.voiceChannel(0)

            if (textChannel == null && voiceChannel == null) {
                context.reply(
                    LorittaReply(
                        context.locale["$LOCALE_PREFIX.renamechannel.channelNotFound"],
                        Constants.ERROR
                    )
                )
                return@executesDiscord
            }

            val newNameArguments = args.drop(1)
            val toRename = newNameArguments.joinToString(" ")
                .trim()
                .replace("(\\s\\|\\s|\\|)".toRegex(), "│")
                .replace("(\\s&\\s|&)".toRegex(), "＆")
                .replace("[\\s]".toRegex(), "-")
            try {
                if (textChannel != null) {
                    textChannel.modifyTextChannel {
                        name = toRename
                    }

                    context.reply(
                        LorittaReply(
                            context.locale["$LOCALE_PREFIX.renamechannel.successfullyRenamed"],
                            "\uD83C\uDF89"
                        )
                    )
                } else if (voiceChannel != null) {
                    voiceChannel.modifyVoiceChannel {
                        name = args.drop(1).joinToString(" ").trim()
                    }

                    context.reply(
                        LorittaReply(
                            context.locale["$LOCALE_PREFIX.renamechannel.successfullyRenamed"],
                            "\uD83C\uDF89"
                        )
                    )
                } else {
                    context.reply(
                        LorittaReply(
                            context.locale["$LOCALE_PREFIX.renamechannel.channelConflict"],
                            Constants.ERROR
                        )
                    )
                }
            } catch (e: Exception) {
                context.reply(
                    LorittaReply(
                        locale["commands.command.renamechannel.cantRename"],
                        Constants.ERROR
                    )
                )
            }
        }
    }
}