package net.perfectdreams.loritta.morenitta.interactions.vanilla.moderation

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
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
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.utils.isValidSnowflake
import java.util.*

class LockCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Lock

        fun getGuildMessageChannel(context: LegacyMessageCommandContext, input: String?): GuildMessageChannel? {
            if (input.isNullOrBlank())
                return null

            val guild = context.guild

            val channels = guild.getTextChannelsByName(input, false)
            if (channels.isNotEmpty()) {
                return channels[0]
            }

            val id = input
                .replace("<", "")
                .replace("#", "")
                .replace(">", "")

            if (!id.isValidSnowflake())
                return null

            val channel = guild.getGuildMessageChannelById(id)
            if (channel != null) {
                return channel
            }

            return null
        }
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.MODERATION, UUID.fromString("b843d3ae-8768-4a5c-ad62-9b5c0c184e0f")) {
        this.enableLegacyMessageSupport = true
        this.defaultMemberPermissions = DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)
        this.botPermissions = setOf(Permission.MANAGE_CHANNEL)

        alternativeLegacyLabels.apply {
            add("trancar")
            add("fechar")
        }

        executor = LockExecutor(loritta)
    }

    class LockExecutor(private val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val channel = optionalChannel("channel", I18N_PREFIX.Options.Channel.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val channelToBeLocked = args[options.channel] ?: context.channel

            // We use StandardGuildMessageChannel instead of GuildMessageChannel because GuildMessageChannels aren't IPermissionContainers
            if (channelToBeLocked !is StandardGuildMessageChannel) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.ChannelCannotBeLocked),
                        Constants.ERROR
                    )
                }
                return
            }

            val publicRole = context.guild.publicRole
            val override = channelToBeLocked.getPermissionOverride(publicRole)

            context.deferChannelMessage(false)
            if (override != null) {
                if (Permission.MESSAGE_SEND !in override.denied) {
                    override.manager
                        .deny(Permission.MESSAGE_SEND)
                        .await()

                    val rolesThatWillStillBeAbleToTalkOnChannel = channelToBeLocked.rolePermissionOverrides
                        .filter { it.allowed.contains(Permission.MESSAGE_SEND) }

                    context.reply(false) {
                        styled(
                            context.locale["commands.command.lock.denied", context.config.commandPrefix],
                            "\uD83C\uDF89"
                        )

                        if (rolesThatWillStillBeAbleToTalkOnChannel.isNotEmpty()) {
                            styled(
                                // The role should NEVER be null here!
                                context.i18nContext.get(I18N_PREFIX.FollowingRolesCanStillTalkDueToPermissions(rolesThatWillStillBeAbleToTalkOnChannel.joinToString(", ") { it.role!!.asMention }))
                            )
                        }
                    }
                } else {
                    context.reply(false) {
                        styled(
                            context.locale["commands.command.lock.channelAlreadyIsLocked", context.config.commandPrefix],
                            Emotes.LORI_CRYING
                        )
                    }
                }
            } else {
                channelToBeLocked.permissionContainer.upsertPermissionOverride(publicRole)
                    .deny(Permission.MESSAGE_SEND)
                    .await()

                context.reply(false) {
                    styled(
                        context.locale["commands.command.lock.denied", context.config.commandPrefix],
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
                options.channel to getGuildMessageChannel(context, args.joinToString(" "))
            )
        }
    }
}
