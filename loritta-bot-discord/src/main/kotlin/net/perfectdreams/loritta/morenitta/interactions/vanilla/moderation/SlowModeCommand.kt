package net.perfectdreams.loritta.morenitta.interactions.vanilla.moderation

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.common.commands.CommandCategory
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

class SlowModeCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Slowmode

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

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.MODERATION, UUID.fromString("e8a7c1f4-3b92-4d6a-8f1e-5c9d2a7b4e3f")) {
        this.enableLegacyMessageSupport = true
        this.defaultMemberPermissions = DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)
        this.botPermissions = setOf(Permission.MANAGE_CHANNEL)

        alternativeLegacyLabels.add("modolento")

        executor = SlowModeExecutor(loritta)
    }

    class SlowModeExecutor(private val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val seconds = long("seconds", I18N_PREFIX.Options.Seconds.Text)
            val channel = optionalChannel("channel", I18N_PREFIX.Options.Channel.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val seconds = args[options.seconds].toInt()
            val targetChannel = args[options.channel] ?: context.channel

            if (targetChannel !is ISlowmodeChannel) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.ChannelDoesNotSupportSlowmode),
                        Constants.ERROR
                    )
                }
                return
            }

            if (seconds < 0 || seconds > 21600) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.InvalidSeconds),
                        Constants.ERROR
                    )
                }
                return
            }

            context.deferChannelMessage(false)

            targetChannel.manager.setSlowmode(seconds).await()

            if (seconds == 0) {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.DisabledInChannel(targetChannel.asMention)),
                        "\uD83C\uDFC3"
                    )
                }
            } else {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.EnabledInChannel(targetChannel.asMention, seconds)),
                        "\uD83D\uDC0C"
                    )
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty())
                return null

            val seconds = args.getOrNull(0)?.toLongOrNull() ?: return null
            val channel = if (args.size > 1) getGuildMessageChannel(context, args.drop(1).joinToString(" ")) else null

            return mapOf(
                options.seconds to seconds,
                options.channel to channel
            )
        }
    }
}
