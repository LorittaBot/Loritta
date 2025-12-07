package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.drop

import dev.minn.jda.ktx.messages.MessageCreate
import kotlinx.datetime.toJavaInstant
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.utils.TimeFormat
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosPayExecutor
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.NumberUtils
import net.perfectdreams.loritta.morenitta.utils.TimeUtils
import net.perfectdreams.loritta.morenitta.utils.VacationModeUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import java.time.Duration
import java.util.*

class DropCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Drop

        val Licks = listOf(
            net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriLick,
            net.perfectdreams.loritta.cinnamon.emotes.Emotes.PantufaLick,
            net.perfectdreams.loritta.cinnamon.emotes.Emotes.GabrielaLick,
            net.perfectdreams.loritta.cinnamon.emotes.Emotes.PowerLick,
        )

        suspend fun checkChargeCreatorSonhos(context: UnleashedContext, isLorittaAdminOption: Boolean): Boolean? {
            if (!isLorittaAdminOption)
                return true

            if (context.guildId != 297732013006389252L && context.guildId != 268353819409252352L && context.guildId != 268353819409252353L && context.guildId != 1204104683380285520L) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.YouCannotStartAAdminDrop),
                        net.perfectdreams.loritta.cinnamon.emotes.Emotes.Error
                    )
                }
                return null
            }

            return false
        }

        suspend fun validateSonhos(context: UnleashedContext, sonhos: Long?, maxSonhosPerParticipant: Long): Long? {
            if (sonhos == null || sonhos == 0L) {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Chat.TryingToDropZeroSonhos),
                        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriHmpf
                    )
                }
                return null
            }

            if (0L > sonhos) {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Chat.TryingToDropLessThanZeroSonhos),
                        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriHmpf
                    )
                }
                return null
            }

            if (sonhos >= DropCall.MAX_SONHOS_PER_PARTICIPANT) {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.TooManySonhos(SonhosUtils.getSonhosEmojiOfQuantity(DropChat.MAX_SONHOS_PER_PARTICIPANT), DropChat.MAX_SONHOS_PER_PARTICIPANT)),
                        net.perfectdreams.loritta.cinnamon.emotes.Emotes.Error
                    )
                }
                return null
            }

            return sonhos
        }
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.ECONOMY, UUID.fromString("9ee3f2ea-0f46-41d4-9074-2d4fc8287a50")) {
        this.enableLegacyMessageSupport = true
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL)
        this.defaultMemberPermissions = DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE)

        subcommand(I18N_PREFIX.Chat.Label, I18N_PREFIX.Chat.Description, UUID.fromString("f19b2bdb-662f-4323-9ab7-abe7f0344b1e")) {
            executor = StartDropChatExecutor(loritta)
        }

        subcommand(I18N_PREFIX.Call.Label, I18N_PREFIX.Call.Description, UUID.fromString("5448e529-b350-4e2a-922c-e11a271a44fd")) {
            executor = StartDropCallExecutor(loritta)
        }
    }

    class StartDropChatExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val sonhos = string("sonhos", I18N_PREFIX.Chat.Options.Sonhos.Text)
            val maxParticipants = long("max_participants", I18N_PREFIX.Chat.Options.MaxParticipants.Text)
            val maxWinners = long("max_winners", I18N_PREFIX.Chat.Options.MaxWinners.Text)
            val channel = channel("channel", I18N_PREFIX.Chat.Options.Channel.Text)
            val duration = string("duration", I18N_PREFIX.Chat.Options.Duration.Text)
            val lorittaAdmin = optionalBoolean("loritta_admin", I18N_PREFIX.Chat.Options.LorittaAdmin.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val chargeCreatorSonhos = checkChargeCreatorSonhos(context, args[options.lorittaAdmin] ?: false) ?: return

            val selfUserProfile = context.lorittaUser.profile

            val channel = args[options.channel]
            val sonhosInput = args[options.sonhos]
            val lorittaAsMember = context.guild.selfMember

            val sonhos = validateSonhos(
                context,
                NumberUtils.convertShortenedNumberOrUserSonhosSpecificToLong(
                    sonhosInput,
                    selfUserProfile.money
                ),
                DropChat.MAX_SONHOS_PER_PARTICIPANT
            ) ?: return

            val duration = TimeUtils.convertToMillisDurationRelative(args[options.duration])

            if (duration > Duration.ofMinutes(5)) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.DropTooLong),
                        net.perfectdreams.loritta.cinnamon.emotes.Emotes.Error
                    )
                }
                return
            }

            if (Duration.ofSeconds(1) > duration) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Chat.DropTooShort),
                        net.perfectdreams.loritta.cinnamon.emotes.Emotes.Error
                    )
                }
                return
            }

            val participants = args[options.maxParticipants].toInt()
            val winners = args[options.maxWinners].toInt()

            if (channel !is GuildMessageChannel) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.ChannelIsNotMessageChannel(channel.asMention)),
                        Constants.ERROR
                    )
                }
                return
            }

            if (!channel.canTalk()) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.LorittaCantSpeakOnChannel(channel.asMention)),
                        Constants.ERROR
                    )
                }
                return
            }

            if (!channel.canTalk(context.member)) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.UserCantSpeakOnChannel(channel.asMention)),
                        Constants.ERROR
                    )
                }
                return
            }

            if (!lorittaAsMember.hasPermission(channel, Permission.MESSAGE_EMBED_LINKS)) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.LorittaDoesNotHavePermissionToSendEmbeds(channel.asMention)),
                        Constants.ERROR
                    )
                }
                return
            }

            if (!lorittaAsMember.hasPermission(channel, Permission.MESSAGE_HISTORY)) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.LorittaDoesNotHavePermissionToViewChannelHistory(channel.asMention)),
                        Constants.ERROR
                    )
                }
                return
            }

            if (participants !in 1..100) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Chat.InvalidParticipantsCount(1, 100)),
                        Constants.ERROR
                    )
                }
                return
            }

            if (winners !in 1..100) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Chat.InvalidWinnersCount(1, 100)),
                        Constants.ERROR
                    )
                }
                return
            }

            if (winners > participants) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Chat.YouMustHaveMoreParticipantsThanWinners),
                        Constants.ERROR
                    )
                }
                return
            }

            if (chargeCreatorSonhos && (winners * sonhos) > selfUserProfile.money) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(SonhosUtils.insufficientSonhos(selfUserProfile.money, winners * sonhos)),
                        Constants.ERROR
                    )
                }
                return
            }

            when (val result = SonhosPayExecutor.checkIfAccountIsOldEnoughToSendSonhos(context.user)) {
                SonhosPayExecutor.Companion.OtherAccountOldEnoughResult.Success -> {}
                is SonhosPayExecutor.Companion.OtherAccountOldEnoughResult.NotOldEnough -> {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(
                                I18N_PREFIX.SelfAccountIsTooNewToCreateADrop(
                                    TimeFormat.DATE_TIME_LONG.format(result.allowedAfterTimestamp.toJavaInstant()),
                                    TimeFormat.RELATIVE.format(result.allowedAfterTimestamp.toJavaInstant())
                                )
                            ),
                            Constants.ERROR
                        )
                    }
                    return
                }
            }

            when (SonhosPayExecutor.checkIfAccountGotDailyAtLeastOnce(loritta, context.member)) {
                SonhosPayExecutor.Companion.AccountGotDailyAtLeastOnceResult.Success -> {}
                SonhosPayExecutor.Companion.AccountGotDailyAtLeastOnceResult.HaventGotDailyOnce -> {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(
                                SonhosCommand.PAY_I18N_PREFIX.SelfAccountNeedsToGetDaily(loritta.commandMentions.daily)
                            ),
                            net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriSob
                        )
                    }
                    return
                }
            }

            if (VacationModeUtils.checkIfWeAreOnVacation(context, true))
                return

            context.deferChannelMessage(true)

            val drop = DropChat(
                loritta,
                context.guild,
                context.user,
                channel,
                sonhos,
                participants,
                winners,
                duration,
                context.i18nContext,
                chargeCreatorSonhos
            )

            val message = channel.sendMessage(
                MessageCreate {
                    with(drop) {
                        createDropMessage()
                    }
                }
            ).await()

            drop.originalDropMessage = message
            drop.startDropAutoFinishTask()

            context.reply(false) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.Chat.DropCreated(message.jumpUrl)),
                    net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriHappyJumping
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val sonhos = args.getOrNull(0)
            val maxParticipants = args.getOrNull(1)?.toLongOrNull()
            val maxWinners = args.getOrNull(2)?.toLongOrNull()
            val channelId = args.getOrNull(3)?.removePrefix("<#")?.removeSuffix(">")?.toLongOrNull()
            val duration = args.drop(4).joinToString(" ")

            if (sonhos == null || maxParticipants == null || maxWinners == null || channelId == null || duration.isBlank()) {
                context.explain()
                return null
            }

            val channel = context.guild.getGuildMessageChannelById(channelId)

            if (channel == null) {
                context.explain()
                return null
            }

            return mapOf(
                options.sonhos to sonhos,
                options.maxParticipants to maxParticipants,
                options.maxWinners to maxWinners,
                options.channel to channel,
                options.duration to duration
            )
        }
    }

    class StartDropCallExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val sonhos = string("sonhos", I18N_PREFIX.Call.Options.Sonhos.Text)
            val maxWinners = long("max_winners", I18N_PREFIX.Call.Options.MaxWinners.Text)
            val channel = channel("channel", I18N_PREFIX.Call.Options.Channel.Text)
            val voiceChannel = channel("voice_channel", I18N_PREFIX.Call.Options.VoiceChannel.Text)
            val duration = optionalString("duration", I18N_PREFIX.Call.Options.Duration.Text)
            val lorittaAdmin = optionalBoolean("loritta_admin", I18N_PREFIX.Call.Options.LorittaAdmin.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val chargeCreatorSonhos = checkChargeCreatorSonhos(context, args[options.lorittaAdmin] ?: false) ?: return

            val selfUserProfile = context.lorittaUser.profile

            val channel = args[options.channel]
            val voiceChannel = args[options.voiceChannel]
            val sonhosInput = args[options.sonhos]
            val lorittaAsMember = context.guild.selfMember

            val sonhos = validateSonhos(
                context,
                NumberUtils.convertShortenedNumberOrUserSonhosSpecificToLong(
                    sonhosInput,
                    selfUserProfile.money
                ),
                DropCall.MAX_SONHOS_PER_PARTICIPANT
            ) ?: return

            val durationInput = args[options.duration]
            val duration = if (durationInput != null) {
                val duration = TimeUtils.convertToMillisDurationRelative(durationInput)

                if (duration > Duration.ofMinutes(5)) {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.DropTooLong),
                            net.perfectdreams.loritta.cinnamon.emotes.Emotes.Error
                        )
                    }
                    return
                }

                if (Duration.ofSeconds(1) > duration) {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Call.DropTooShort),
                            net.perfectdreams.loritta.cinnamon.emotes.Emotes.Error
                        )
                    }
                    return
                }

                duration
            } else null

            val winners = args[options.maxWinners].toInt()

            if (channel !is GuildMessageChannel) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.ChannelIsNotMessageChannel(channel.asMention)),
                        Constants.ERROR
                    )
                }
                return
            }

            if (!channel.canTalk()) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.LorittaCantSpeakOnChannel(channel.asMention)),
                        Constants.ERROR
                    )
                }
                return
            }

            if (!channel.canTalk(context.member)) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.UserCantSpeakOnChannel(channel.asMention)),
                        Constants.ERROR
                    )
                }
                return
            }

            if (voiceChannel !is AudioChannel) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Call.ChannelIsNotVoiceChannel(channel.asMention)),
                        Constants.ERROR
                    )
                }
                return
            }

            if (!lorittaAsMember.hasPermission(channel, Permission.MESSAGE_EMBED_LINKS)) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.LorittaDoesNotHavePermissionToSendEmbeds(channel.asMention)),
                        Constants.ERROR
                    )
                }
                return
            }

            if (!lorittaAsMember.hasPermission(channel, Permission.MESSAGE_HISTORY)) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.LorittaDoesNotHavePermissionToViewChannelHistory(channel.asMention)),
                        Constants.ERROR
                    )
                }
                return
            }

            if (winners !in 1..100) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Chat.InvalidWinnersCount(1, 100)),
                        Constants.ERROR
                    )
                }
                return
            }

            if (chargeCreatorSonhos && (winners * sonhos) > selfUserProfile.money) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(SonhosUtils.insufficientSonhos(selfUserProfile.money, winners * sonhos)),
                        Constants.ERROR
                    )
                }
                return
            }
            
            when (val result = SonhosPayExecutor.checkIfAccountIsOldEnoughToSendSonhos(context.user)) {
                SonhosPayExecutor.Companion.OtherAccountOldEnoughResult.Success -> {}
                is SonhosPayExecutor.Companion.OtherAccountOldEnoughResult.NotOldEnough -> {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(
                                I18N_PREFIX.SelfAccountIsTooNewToCreateADrop(
                                    TimeFormat.DATE_TIME_LONG.format(result.allowedAfterTimestamp.toJavaInstant()),
                                    TimeFormat.RELATIVE.format(result.allowedAfterTimestamp.toJavaInstant())
                                )
                            ),
                            Constants.ERROR
                        )
                    }
                    return
                }
            }

            when (SonhosPayExecutor.checkIfAccountGotDailyAtLeastOnce(loritta, context.member)) {
                SonhosPayExecutor.Companion.AccountGotDailyAtLeastOnceResult.Success -> {}
                SonhosPayExecutor.Companion.AccountGotDailyAtLeastOnceResult.HaventGotDailyOnce -> {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(
                                SonhosCommand.PAY_I18N_PREFIX.SelfAccountNeedsToGetDaily(loritta.commandMentions.daily)
                            ),
                            net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriSob
                        )
                    }
                    return
                }
            }

            if (VacationModeUtils.checkIfWeAreOnVacation(context, true))
                return

            context.deferChannelMessage(true)

            val drop = DropCall(
                loritta,
                context.guild,
                context.user,
                channel,
                voiceChannel,
                sonhos,
                winners,
                duration,
                context.i18nContext,
                chargeCreatorSonhos
            )

            // This is a bit tricky, if the duration is null, then we don't need to create any of the drop messages!
            if (duration == null) {
                val message = drop.finishDrop()

                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Chat.DropCreated(message.jumpUrl)),
                        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriHappyJumping
                    )
                }
            } else {
                val message = channel.sendMessage(
                    MessageCreate {
                        with(drop) {
                            createDropMessage()
                        }
                    }
                ).await()

                drop.originalDropMessage = message
                drop.startDropAutoFinishTask()

                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Chat.DropCreated(message.jumpUrl)),
                        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriHappyJumping
                    )
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val sonhos = args.getOrNull(0)
            val maxWinners = args.getOrNull(1)?.toLongOrNull()
            val channelId = args.getOrNull(2)?.removePrefix("<#")?.removeSuffix(">")?.toLongOrNull()
            val voiceChannelId = args.getOrNull(3)?.removePrefix("<#")?.removeSuffix(">")?.toLongOrNull()
            val duration = args.drop(4).joinToString(" ").ifBlank { null }

            if (sonhos == null || maxWinners == null || channelId == null || voiceChannelId == null) {
                context.explain()
                return null
            }

            val channel = context.guild.getGuildMessageChannelById(channelId)

            if (channel == null) {
                context.explain()
                return null
            }

            val voiceChannel = context.guild.getGuildChannelById(voiceChannelId) as? AudioChannel

            if (voiceChannel == null) {
                context.explain()
                return null
            }

            return mapOf(
                options.sonhos to sonhos,
                options.maxWinners to maxWinners,
                options.channel to channel,
                options.voiceChannel to voiceChannel,
                options.duration to duration
            )
        }
    }
}