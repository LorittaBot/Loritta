package net.perfectdreams.loritta.commands.administration

import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.api.commands.*
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase

class RenameChannelCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("renamechannel", "renomearcanal"), CommandCategory.ADMIN) {
    companion object {
        private const val LOCALE_PREFIX = "commands.moderation"
    }
    override fun command() = create {
        localizedDescription("$LOCALE_PREFIX.renamechannel.description")

        usage {
            arguments {
                argument(ArgumentType.TEXT) {}
                argument(ArgumentType.TEXT) {}
            }
        }

        examples {
            listOf(
                    "#lori-é-fofis lori é fofis",
                    "297732013006389252 bate-papo",
                    "lorota-pantufa lorota & pantufa",
                    "bate-papo \uD83D\uDE0E | bate-papo"
            )
        }

        canUseInPrivateChannel = false

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

            val textChannelManager = textChannel?.manager
            val voiceChannelManager = voiceChannel?.manager

            val newNameArguments = args.drop(1)
            val toRename = newNameArguments.joinToString(" ")
                    .trim()
                    .replace("(\\s\\|\\s|\\|)".toRegex(), "│")
                    .replace("(\\s&\\s|&)".toRegex(), "＆")
                    .replace("[\\s]".toRegex(), "-")
            try {
                if (textChannel != null && voiceChannel == null) {
                    textChannelManager?.setName(toRename)?.queue()

                    context.reply(
                            LorittaReply(
                                    context.locale["$LOCALE_PREFIX.renamechannel.successfullyRenamed"],
                                    "\uD83C\uDF89"
                            )
                    )
                } else if (voiceChannel != null && textChannel == null) {
                    voiceChannelManager?.setName(args.drop(1).joinToString(" ").trim())?.queue()

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
                                locale["commands.moderation.renamechannel.cantRename"],
                                Constants.ERROR
                        )
                )
            }
        }
    }
}
