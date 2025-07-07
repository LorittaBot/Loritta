package net.perfectdreams.loritta.morenitta.interactions.vanilla.moderation

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.utils.Constants
import java.util.*

class UnlockCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Unlock
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.MODERATION, UUID.fromString("d39bbd64-fea7-4eb5-9b50-fcff7d83804b")) {
        this.enableLegacyMessageSupport = true
        this.defaultMemberPermissions = DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)
        this.botPermissions = setOf(Permission.MANAGE_CHANNEL)

        alternativeLegacyLabels.apply {
            add("destrancar")
            add("desfechar")
        }

        executor = UnlockExecutor(loritta)
    }

    class UnlockExecutor(private val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val channel = optionalChannel("channel", I18N_PREFIX.Options.Channel.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val channelToBeUnlocked = args[options.channel] ?: context.channel

            // We use StandardGuildMessageChannel instead of GuildMessageChannel because GuildMessageChannels aren't IPermissionContainers
            if (channelToBeUnlocked !is StandardGuildMessageChannel) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.ChannelCannotBeUnlocked),
                        Constants.ERROR
                    )
                }
                return
            }

            val publicRole = context.guild.publicRole
            val override = channelToBeUnlocked.getPermissionOverride(publicRole)

            context.deferChannelMessage(false)
            if (override != null) {
                if (Permission.MESSAGE_SEND in override.denied) {
                    override.manager
                        .grant(Permission.MESSAGE_SEND)
                        .queue()

                    context.reply(false) {
                        styled(
                            context.locale["commands.command.unlock.allowed", context.config.commandPrefix],
                            "\uD83C\uDF89"
                        )
                    }
                } else {
                    context.reply(false) {
                        styled(
                            context.locale["commands.command.unlock.channelAlreadyIsUnlocked", context.config.commandPrefix],
                            Emotes.LORI_CRYING
                        )
                    }
                }
            } else { // Bem, na verdade não seria totalmente necessário este else, mas vamos supor que o cara usou o "+unlock" com o chat destravado sem ter travado antes :rolling_eyes:
                channelToBeUnlocked.permissionContainer.upsertPermissionOverride(publicRole)
                    .grant(Permission.MESSAGE_SEND)
                    .queue()

                context.reply(false) {
                    styled(
                        context.locale["commands.command.unlock.allowed", context.config.commandPrefix],
                        "\uD83C\uDF89"
                    )
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            return mapOf(
                options.channel to LockCommand.getGuildMessageChannel(context, args.joinToString(" "))
            )
        }
    }
}
