package net.perfectdreams.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.vanilla.administration.RoleIdCommand
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase

class RoleIdCommand(loritta: LorittaDiscord): DiscordAbstractCommandBase(loritta, listOf("roleid", "cargoid", "iddocargo"), CommandCategory.ADMIN) {

    override fun command(): Command<CommandContext> = create {
        localizedDescription("commands.moderation.roleId.description")
        localizedExamples("commands.moderation.roleId.examples")

        usage {
            argument(ArgumentType.TEXT) {
                optional = false
            }
        }

        executesDiscord {
            if (args.isEmpty()) return@executesDiscord explain()

            val joinedArguments = args.joinToString(" ")

            val replies = mutableListOf<LorittaReply>()
            val mentionedRoles = discordMessage.mentionedRoles

            if (mentionedRoles.isNotEmpty()) {
                replies.add(
                        LorittaReply(
                                message = locale["commands.moderation.roleId.identifiers", joinedArguments],
                                prefix = "\uD83D\uDCBC"
                        )
                )

                mentionedRoles.mapTo(replies) {
                    LorittaReply(
                            message = "*${it.name}* - `${it.id}`",
                            mentionUser = false
                    )
                }
            } else {
                val roles = guild.roles.filter { it.name.contains(joinedArguments, true) }

                replies.add(LorittaReply(
                        message = locale["${RoleIdCommand.LOCALE_PREFIX}.roleId.rolesThatContains", joinedArguments],
                        prefix = "\uD83D\uDCBC"
                ))

                if (roles.isEmpty()) {
                    replies.add(
                            LorittaReply(
                                    message = "*${locale["${RoleIdCommand.LOCALE_PREFIX}.roleId.emptyRoles"]}*",
                                    mentionUser = false,
                                    prefix = "\uD83D\uDE22"
                            )
                    )
                } else {
                    roles.mapTo(replies) {
                        LorittaReply(
                                message = "*${it.name}* - `${it.id}`",
                                mentionUser = false
                        )
                    }
                }
            }
            reply(replies)
        }
    }

}