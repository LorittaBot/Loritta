package net.perfectdreams.loritta.morenitta.interactions.vanilla.moderation

import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.future.asDeferred
import kotlinx.coroutines.future.await
import kotlinx.coroutines.joinAll
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils
import java.util.*
import java.util.concurrent.TimeUnit

class ClearCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Clear

        const val MAX_RANGE = 1000L
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.MODERATION, UUID.fromString("a5cb1636-81da-435f-bec8-a5be3f393edc")) {
        this.defaultMemberPermissions = DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY)
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL)
        this.enableLegacyMessageSupport = true

        this.alternativeLegacyLabels.apply {
            add("clean")
            add("clear")
        }

        this.executor = ClearExecutor(loritta)
    }

    class ClearExecutor(private val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val count = long("count", I18N_PREFIX.Options.Count.Text)

            // This could be refactored to be easier to use and understand, because atm this is a direct "legacy -> slash command" port
            val options = optionalString("options", I18N_PREFIX.Options.Options.Text)
        }

        private val unavailableGuilds = Collections.newSetFromMap(
            Caffeine.newBuilder()
                .expireAfterWrite(MAX_RANGE / 100, TimeUnit.SECONDS)
                .build<Long, Boolean>().asMap()
        )

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(true)

            val count = args[options.count].toInt()
            val rawOptions = args[options.options]
            val channel = context.channel as? GuildMessageChannel ?: return

            // The message count can't be null or be higher than 500 and lower than 2
            if (count !in 2..MAX_RANGE) {
                context.reply(true) {
                    styled(
                        context.locale["commands.command.clear.invalidClearRange"],
                        Constants.ERROR
                    )
                }
                return
            }

            // If the guild already have a clear operation queued, we'll prevent them from creating another one
            if (unavailableGuilds.contains(context.guild.idLong)) {
                context.reply(true) {
                    styled(
                        context.locale["commands.command.clear.operationQueued"],
                        Constants.ERROR
                    )
                }
                return
            }

            // The filter text and target user, null if not available
            val (targets, text, textInserted) = getOptions(context, rawOptions ?: "")

            if (targets.filterNotNull().isEmpty() && targets.isNotEmpty()) {
                context.reply(true) {
                    styled(
                        context.locale["commands.command.clear.invalidUserFilter"],
                        Constants.ERROR
                    )
                }
                return
            }

            if (text == null && textInserted) {
                context.reply(true) {
                    styled(
                        context.locale["commands.command.clear.invalidTextFilter"],
                        Constants.ERROR
                    )
                }
                return
            }

            val messagesToBeIgnored = mutableListOf<Message>()

            // Deleting the user's message (the command one, +clear)
            if (context is LegacyMessageCommandContext) {
                // We only need to delete the user's message if it is a legacy message command because, if it is the interaction, it is
                // an ephemeral message that doesn't need to be deleted
                val discordMessage = context.event.message

                runCatching {
                    discordMessage.delete()
                        .submit()
                        .await()
                }

                messagesToBeIgnored.add(discordMessage)
            }

            val messages = channel.iterableHistory.takeAsync(count).await()

            val allowedMessages = messages.applyAvailabilityFilterToCollection(text, targets.filterNotNull().toSet()).minus(messagesToBeIgnored)
            val disallowedMessages = messages.minus(allowedMessages)

            if (allowedMessages.isEmpty()) { // If there are no allowed messages, we'll cancel the execution
                context.reply(true) {
                    styled(
                        context.locale["commands.command.clear.couldNotFindMessages"],
                        Constants.ERROR
                    )
                }
                return
            }

            // Clear the messages after deleting the command's one
            clear(context, allowedMessages)

            context.reply(true) {
                styled(
                    context.locale["commands.command.clear.success", allowedMessages.size, context.user.asMention],
                    "\uD83C\uDF89"
                )

                if (disallowedMessages.isNotEmpty()) {
                    styled(
                        context.locale["commands.command.clear.successButIgnoredMessages", disallowedMessages.size],
                        "\uD83D\uDD37"
                    )
                }
            }
        }

        /**
         * Filter the messages that should be deleted or not,
         * the factors are the ones bellow, only those messages will be deleted.
         *
         * @see command
         *
         * @factor The message can't be older than 2 weeks
         * @factor The message can't be pinned
         * @factor If the target isn't null, the message must be from the target
         * @factor If the text isn't null, the message must contains the text
         */
        private fun List<Message>.applyAvailabilityFilterToCollection(text: String?, targets: Set<Long>) = filter {
            (((System.currentTimeMillis() / 1000) - it.timeCreated.toEpochSecond()) < 1209600) // The message can't be older than 2 weeks
                    && (it.isPinned.not()) // The message can't be pinned
                    && (if (targets.isNotEmpty()) targets.contains(it.author.idLong) else true) // If the target isn't null, the message must be from one of the targets
                    && (if (text != null) it.contentStripped.contains(text.trim(), ignoreCase = true) else true) // If the text isn't null, the message must contains the text
        }

        /**
         * This method will retrieve all the
         * command options to the user, including the contains and from one
         *
         * @return Command options
         */
        private suspend fun getOptions(context: UnleashedContext, rawOptions: String): CommandOptions {
            val optionName = context.locale["commands.command.clear.targetOption"]
            val options = rawOptions.split(" ").joinToString(" ").trim().split("$optionName:")

            var text: String? = options.firstOrNull()
            var textInserted = true

            if (text?.trim()?.startsWith("$optionName:") == true) {
                text = null
                textInserted = false
            }

            val targetArguments = options.let { if (text != null) it.drop(text.split(" ").size) else it }
            val targets = getUserIdsFromArguments(context.guild, targetArguments)

            return CommandOptions(targets, text, textInserted)
        }

        /**
         * This method will try to get the max number of users
         * possible, and if one of them is invalid, it will return an empty list
         * to provide better validating/debugging
         *
         * @param guild The message's guild
         * @param arguments The arguments that will be checked
         * @return Null if at least one target was inserted but not found
         */
        private suspend fun getUserIdsFromArguments(guild: Guild?, arguments: List<String>): Set<Long?> {
            val targets: MutableSet<Long?> = mutableSetOf()
            for (target in arguments) {
                targets.add(DiscordUtils.extractUserFromString(loritta, target.trim(), guild = guild)?.idLong)
            }
            return targets
        }

        /**
         * This will clear the provided [messages] using the "purge" function,
         * and will register the operation too.
         *
         * @param messages The messages that should be deleted
         */
        private suspend fun clear(context: UnleashedContext, messages: List<Message>) {
            this@ClearExecutor.unavailableGuilds.add(context.guild.idLong) // Adding the operation to the guild
            context.channel.purgeMessages(messages)
                .map { it.asDeferred() }.joinAll() // Purging the messages and awaiting

            this@ClearExecutor.unavailableGuilds.remove(context.guild.idLong)
        }

        data class CommandOptions(
            val targets: Set<Long?>,
            val text: String?,
            val textInserted: Boolean
        )

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val page = args.getOrNull(0)?.toLongOrNull()

            if (page == null) {
                context.explain()
                return null
            }

            return mapOf(
                // We won't implement all the other options because that's too much work
                options.count to page
            )
        }
    }
}
