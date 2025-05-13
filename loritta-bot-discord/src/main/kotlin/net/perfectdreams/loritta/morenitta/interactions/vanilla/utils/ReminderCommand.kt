package net.perfectdreams.loritta.morenitta.interactions.vanilla.utils

import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEditBuilder
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.components.button.Button
import net.dv8tion.jda.api.components.button.ButtonStyle
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.IntegrationType
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reminders
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Reminder
import net.perfectdreams.loritta.morenitta.interactions.InteractionMessage
import net.perfectdreams.loritta.morenitta.interactions.UnleashedButton
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import java.awt.Color
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*

class ReminderCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Reminder
        private const val LOCALE_PREFIX = "commands.command.remindme"
        private val alternativeLegacyLabels = listOf(
            "remindme",
            "lembre",
            "remind",
            "lembrar",
            "lembrete"
        )
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.UTILS, UUID.fromString("8fdffec4-23f9-4663-83d6-263eabae1de8")) {
        enableLegacyMessageSupport = true

        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL)

        examples = I18N_PREFIX.Examples

        subcommand(I18N_PREFIX.Create.Label, I18N_PREFIX.Create.Description, UUID.fromString("f0d51677-b6ae-47f5-944f-7ad880a03382")) {
            this.alternativeLegacyAbsoluteCommandPaths.apply {
                addAll(ReminderCommand.alternativeLegacyLabels)
            }

            executor = ReminderAddExecutor(loritta)
        }

        subcommand(I18N_PREFIX.List.Label, I18N_PREFIX.List.Description, UUID.fromString("2de05c27-5cdb-45ce-988f-76d018f973cf")) {
            this.alternativeLegacyAbsoluteCommandPaths.apply {
                for (label in ReminderCommand.alternativeLegacyLabels) {
                    add("$label list")
                    add("$label lista")
                }
            }

            executor = ReminderListExecutor(loritta)
        }
    }

    class ReminderAddExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        companion object {
            private val logger = KotlinLogging.logger {}
        }

        class Options : ApplicationCommandOptions() {
            val reason = string("reason", I18N_PREFIX.Create.Options.Reason.Text)
            val duration = string("duration", I18N_PREFIX.Create.Options.Duration.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val reason = args[options.reason]
            val duration = args[options.duration]

            if (context is LegacyMessageCommandContext) {
                // If this is a legacy message command, we'll do this the old-fashioned way, because supporting two arguments in a clean way would be painful in a message command
                val message = context.reply(false) {
                    styled(
                        context.locale["${LOCALE_PREFIX}.setHour"],
                        "⏰"
                    )
                } as InteractionMessage.FollowUpInteractionMessage // This is not a REAL interaction message

                val discordMessage = message.message

                discordMessage.onResponseByAuthor(context) {
                    loritta.messageInteractionCache.remove(discordMessage.idLong)
                    discordMessage.delete().queue()
                    createReminderAndSendMessage(context, reason, it.message.contentDisplay)
                }
                return
            }

            createReminderAndSendMessage(context, reason, duration)
        }

        /**
         * Prepares the [duration] for the reminder, creates the reminder and sends a message
         *
         * @param context  the context
         * @param reason   the reason of why the reminder is being created
         * @param duration when the reminder will expire
         */
        private suspend fun createReminderAndSendMessage(context: UnleashedContext, reason: String, duration: String) {
            val inMillis = TimeUtils.convertToMillisRelativeToNow(duration)
            val instant = Instant.ofEpochMilli(inMillis)
            val now = Instant.now()
            if (now > instant) {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Create.DateInThePast(DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(inMillis))),
                        Constants.ERROR
                    )
                }
                return
            }
            val localDateTime = ZonedDateTime.ofInstant(instant, Constants.LORITTA_TIMEZONE)

            val messageContent = reason.trim()
            logger.trace { "userId = ${context.user.idLong}" }
            logger.trace { "channelId = ${context.channel.idLong}" }
            logger.trace { "remindAt = $inMillis" }
            logger.trace { "content = $messageContent" }

            createReminder(context, reason, localDateTime)

            context.reply(false) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.Create.Success(DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(localDateTime.toInstant())))
                )
            }
        }

        private suspend fun createReminder(context: UnleashedContext, messageContent: String, zonedDateTime: ZonedDateTime) {
            loritta.newSuspendedTransaction {
                Reminder.new {
                    userId = context.user.idLong
                    guildId = context.guild.idLong
                    channelId = context.channel.idLong
                    remindAt = (zonedDateTime.toEpochSecond() * 1000)
                    content = messageContent
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (context.args.isEmpty()) {
                context.explain()
                return null
            }

            // This SUCKS but that's because we need to support the legacy way of creating reminders toIntOrNull
            return mapOf(
                options.reason to args.joinToString(" "),
                options.duration to "unused_legacy"
            )
        }
    }

    class ReminderListExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        companion object {
            // We use 5 reminders per page because that's the limit of buttons per action row
            // In the old reminders list version, we did allow 10 reminders per page
            const val REMINDERS_PER_PAGE = 5
        }

        class Options : ApplicationCommandOptions() {
            val page = optionalLong("page", I18N_PREFIX.Create.Options.Reason.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val page = ((args[options.page]?.toInt() ?: 1) - 1).coerceAtLeast(0)

            context.deferChannelMessage(false)

            val reminderListMessage = createReminderListMessage(context, page)
            context.reply(false) {
                reminderListMessage.invoke(this)
            }
        }

        /**
         * Creates the message used for the reminder list
         *
         * @param context the context
         * @param page the current page, zero indexed
         */
        private suspend fun createReminderListMessage(
            context: UnleashedContext,
            page: Int
        ): InlineMessage<*>.() -> (Unit) {
            val (totalReminders, reminders) = loritta.newSuspendedTransaction {
                val totalReminders = Reminder.find { Reminders.userId eq context.user.idLong }
                    .count()

                val reminders = Reminder
                    .find { Reminders.userId eq context.user.idLong }
                    .orderBy(Reminders.remindAt to SortOrder.ASC) // Sort older reminders -> new reminders
                    .limit(REMINDERS_PER_PAGE)
                    .offset((page * REMINDERS_PER_PAGE).toLong())
                    .toMutableList()

                return@newSuspendedTransaction Pair(totalReminders, reminders)
            }

            val embed = EmbedBuilder()
            embed.setTitle("<a:lori_notification:394165039227207710> ${context.locale["$LOCALE_PREFIX.yourReminders"]} (${totalReminders})")
            embed.setColor(Color(255, 179, 43))

            for ((idx, reminder) in reminders.withIndex()) {
                embed.appendDescription(Constants.INDEXES[idx] + " ${reminder.content.substringIfNeeded(0..100)}\n")
            }

            val reminderButtons = mutableListOf<Button>()

            for ((index, reminder) in reminders.withIndex()) {
                reminderButtons.add(
                    loritta.interactivityManager
                        .buttonForUser(
                            context.user,
                            context.alwaysEphemeral,
                            ButtonStyle.PRIMARY,
                            builder = {
                                emoji = Emoji.fromUnicode(Constants.INDEXES[index])
                            }
                        ) {
                            val textChannel = loritta.lorittaShards.getGuildMessageChannelById(reminder.channelId.toString())

                            val guild = textChannel?.guild

                            val embedBuilder = EmbedBuilder()
                            if (guild != null) {
                                embedBuilder.setThumbnail(guild.iconUrl)
                            }

                            embedBuilder.setTitle("<a:lori_notification:394165039227207710> ${reminder.content}".substringIfNeeded(0 until MessageEmbed.TITLE_MAX_LENGTH))
                            embedBuilder.appendDescription("**${context.locale["${LOCALE_PREFIX}.remindAt"]} ** ${DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(reminder.remindAt)}\n")
                            embedBuilder.appendDescription("**${context.locale["${LOCALE_PREFIX}.createdInGuild"]}** `${guild?.name ?: "Servidor não existe mais..."}`\n")
                            embedBuilder.appendDescription("**${context.locale["${LOCALE_PREFIX}.remindInTextChannel"]}** ${textChannel?.asMention ?: "Canal de texto não existe mais..."}")
                            embedBuilder.setColor(Color(255, 179, 43))

                            it.editMessage(true) {
                                embeds += embedBuilder.build()

                                actionRow(
                                    loritta.interactivityManager
                                        .buttonForUser(
                                            context.user,
                                            context.alwaysEphemeral,
                                            ButtonStyle.SECONDARY,
                                            builder = {
                                                loriEmoji = Emotes.ChevronLeft
                                            }
                                        ) {
                                            it.deferAndEditOriginal {
                                                createReminderListMessage(context, page).invoke(this)
                                            }
                                        },
                                    loritta.interactivityManager
                                        .buttonForUser(
                                            context.user,
                                            context.alwaysEphemeral,
                                            ButtonStyle.SECONDARY,
                                            builder = {
                                                emoji = Emoji.fromUnicode("\uD83D\uDDD1")
                                            }
                                        ) {
                                            val hook = it.updateMessageSetLoadingState()

                                            loritta.newSuspendedTransaction {
                                                Reminders.deleteWhere { Reminders.id eq reminder.id }
                                            }

                                            context.reply(true) {
                                                styled(
                                                    context.locale["${LOCALE_PREFIX}.reminderRemoved"]
                                                )
                                            }

                                            hook.editOriginal(
                                                MessageEditBuilder {
                                                    createReminderListMessage(context, page).invoke(this)
                                                }.build()
                                            ).await()
                                        }
                                )
                            }
                        }
                )
            }

            return {
                this.content = context.user.asMention
                this.embeds += embed.build()

                val leftButton = UnleashedButton.of(
                    ButtonStyle.SECONDARY,
                    emoji = Emotes.ChevronLeft
                )

                val rightButton = UnleashedButton.of(
                    ButtonStyle.SECONDARY,
                    emoji = Emotes.ChevronRight
                )

                if (reminderButtons.isNotEmpty()) {
                    actionRow(reminderButtons)
                }

                actionRow(
                    if (page != 0) {
                        loritta.interactivityManager.buttonForUser(
                            context.user,
                            context.alwaysEphemeral,
                            leftButton
                        ) {
                            it.deferAndEditOriginal {
                                createReminderListMessage(context, page - 1).invoke(this)
                            }
                        }
                    } else {
                        leftButton.asDisabled()
                    },

                    if (((page + 1) * REMINDERS_PER_PAGE) in 0..totalReminders) {
                        loritta.interactivityManager.buttonForUser(
                            context.user,
                            context.alwaysEphemeral,
                            rightButton
                        ) {
                            it.deferAndEditOriginal {
                                createReminderListMessage(context, page + 1).invoke(this)
                            }
                        }
                    } else {
                        rightButton.asDisabled()
                    }
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            val page = args.getOrNull(0)?.toLongOrNull() ?: 0

            return mapOf(options.page to page)
        }
    }
}