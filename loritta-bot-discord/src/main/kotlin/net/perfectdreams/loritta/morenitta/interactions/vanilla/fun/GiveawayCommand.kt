package net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.toJavaColor
import net.perfectdreams.loritta.cinnamon.emotes.Emotes as CinnamonEmotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GiveawayRoleExtraEntries
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.Giveaways
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.serializable.GiveawayRoles
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.servers.Giveaway
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.utils.isValidSnowflake
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

class GiveawayCommand(val m: LorittaBot) : SlashCommandDeclarationWrapper  {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Giveaway
        private const val LOCALE_PREFIX = "commands.command" // Legacy
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.FUN, UUID.fromString("e934638a-3297-4d93-8c7f-82941825e57c")) {
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL)
        this.defaultMemberPermissions = DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE)
        this.enableLegacyMessageSupport = true
        this.alternativeLegacyLabels.apply {
            this.add("sorteio")
        }

        subcommand(I18N_PREFIX.Setup.Label, I18N_PREFIX.Setup.Description, UUID.fromString("ad7ffd12-3817-44a1-930a-4dcd7351c034")) {
            this.executor = GiveawaySetupExecutor(m)
            this.alternativeLegacyLabels.apply {
                this.add("criar")
                this.add("create")
            }
        }

        subcommand(I18N_PREFIX.Reroll.Label, I18N_PREFIX.Reroll.Description, UUID.fromString("3e8f1a6d-4b7c-5e9f-0a1b-2c3d4e5f6a7b")) {
            this.executor = GiveawayRerollExecutor(m)
        }

        subcommand(I18N_PREFIX.End.Label, I18N_PREFIX.End.Description, UUID.fromString("9e5f4a7b-3c2d-6e8f-1a2b-3c4e5f6a7b8c")) {
            this.executor = GiveawayEndExecutor(m)
        }

        subcommand(I18N_PREFIX.Edit.Label, I18N_PREFIX.Edit.Description, UUID.fromString("d7b3e4f5-6a1c-8e9d-0f2b-3c4a5d6e7f8a")) {
            this.executor = GiveawayEditExecutor(m)
            this.alternativeLegacyLabels.apply {
                this.add("edit")
            }
        }
    }

    class GiveawaySetupExecutor(val m: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val builder = GiveawayBuilderScreen.GiveawayBuilder(
                context.i18nContext.get(I18N_PREFIX.Setup.GiveawayNamePlaceholder),
                context.i18nContext.get(I18N_PREFIX.Setup.GiveawayDescriptionPlaceholder)
            )

            val screen = GiveawayBuilderScreen.Appearance(m)

            context.reply(false, screen.render(context, builder))
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            return emptyMap()
        }
    }

    class GiveawayRerollExecutor(val m: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val messageUrl = string("giveaway_message_url", I18N_PREFIX.Reroll.Options.MessageUrl.Text)
            val rerollCount = optionalLong("reroll_count", I18N_PREFIX.Reroll.Options.RerollCount.Text)
        }
        override val options = Options()
        
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val link = args[options.messageUrl]
            val split = link.split("/")

            var messageId: Long? = null
            var channelId: Long? = null

            if (split.size == 1 && split[0].isValidSnowflake()) {
                messageId = split[0].toLong()
            } else {
                messageId = split.getOrNull(split.size - 1)?.toLongOrNull()
                channelId = split.getOrNull(split.size - 2)?.toLongOrNull()
            }

            if (messageId == null) {
                context.reply(false) {
                    styled(
                        context.locale["$LOCALE_PREFIX.giveawayend.giveawayInvalidArguments", "`https://canary.discordapp.com/channels/297732013006389252/297732013006389252/594270558238146603`"],
                        Constants.ERROR
                    )
                }
                return
            }

            val giveaway = m.newSuspendedTransaction {
                if (channelId != null) {
                    Giveaway.find {
                        (Giveaways.guildId eq context.guild.idLong) and (Giveaways.messageId eq messageId) and (Giveaways.textChannelId eq channelId)
                    }.firstOrNull()
                } else {
                    Giveaway.find {
                        (Giveaways.guildId eq context.guild.idLong) and (Giveaways.messageId eq messageId)
                    }.firstOrNull()
                }
            }

            if (giveaway == null) {
                context.reply(false) {
                    styled(
                        context.locale["$LOCALE_PREFIX.giveawayend.giveawayDoesNotExist"],
                        Emotes.LORI_HM
                    )
                }
                return
            }

            if (!giveaway.finished) {
                context.reply(false) {
                    styled(
                        context.locale[
                            "$LOCALE_PREFIX.giveawayreroll.giveawayStillRunning",
                            "`${context.locale["$LOCALE_PREFIX.giveawayreroll.giveawayHowToEnd", context.config.commandPrefix, link.stripCodeMarks()]}`"
                        ],
                        Constants.ERROR
                    )
                }
                return
            }

            val textChannel = context.guild.getGuildMessageChannelById(giveaway.textChannelId)

            if (textChannel == null) {
                context.reply(false) {
                    styled(
                        context.locale["$LOCALE_PREFIX.giveawayend.channelDoesNotExist"],
                        Constants.ERROR
                    )
                }
                return
            }
            val message = textChannel.retrieveMessageById(messageId).await()

            if (message == null) {
                context.reply(false) {
                    styled(
                        context.locale["$LOCALE_PREFIX.giveawayend.messageDoesNotExist"],
                        Constants.ERROR
                    )
                }
                return
            }

            val rerollCount = (args[options.rerollCount]?.toInt() ?: giveaway.numberOfWinners).coerceIn(1..100)

            m.giveawayManager.rollWinners(message, giveaway, numberOfWinnersOverride = rerollCount)

            context.reply(false) {
                styled(
                    context.locale["$LOCALE_PREFIX.giveawayreroll.rerolledGiveaway"],
                    Emotes.LORI_HAPPY
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val messageUrl = args.getOrNull(0)
            if (messageUrl == null) {
                context.explain()
                return null
            }

            val rerollCount = args.getOrNull(1)?.toLongOrNull()

            return mapOf(
                options.messageUrl to messageUrl,
                options.rerollCount to rerollCount
            )
        }
    }

    class GiveawayEndExecutor(val m: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val messageUrl = string("giveaway_message_url", I18N_PREFIX.End.Options.MessageUrl.Text)
        }
        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val link = args[options.messageUrl]

            val split = link.split("/")

            var messageId: Long? = null
            var channelId: Long? = null

            if (split.size == 1 && split[0].isValidSnowflake()) {
                messageId = split[0].toLong()
            } else {
                messageId = split.getOrNull(split.size - 1)?.toLongOrNull()
                channelId = split.getOrNull(split.size - 2)?.toLongOrNull()
            }

            if (messageId == null) {
                context.reply(false) {
                    styled(
                        context.locale["$LOCALE_PREFIX.giveawayend.giveawayInvalidArguments", "`https://canary.discordapp.com/channels/297732013006389252/297732013006389252/594270558238146603`"],
                        Constants.ERROR
                    )
                }
                return
            }

            val giveaway = m.newSuspendedTransaction {
                if (channelId != null) {
                    Giveaway.find {
                        (Giveaways.guildId eq context.guild.idLong) and (Giveaways.messageId eq messageId) and (Giveaways.textChannelId eq channelId)
                    }.firstOrNull()
                } else {
                    Giveaway.find {
                        (Giveaways.guildId eq context.guild.idLong) and (Giveaways.messageId eq messageId)
                    }.firstOrNull()
                }
            }

            if (giveaway == null) {
                context.reply(false) {
                    styled(
                        context.locale["$LOCALE_PREFIX.giveawayend.giveawayDoesNotExist"],
                        Emotes.LORI_HM
                    )
                }
                return
            }

            if (giveaway.finished) {
                context.reply(false) {
                    styled(
                        context.locale[
                            "$LOCALE_PREFIX.giveawayend.giveawayAlreadyEnded",
                            "`${context.locale["$LOCALE_PREFIX.giveawayend.giveawayHowToReroll", context.config.commandPrefix, link.stripCodeMarks()]}`"
                        ],
                        Constants.ERROR
                    )
                }
                return
            }

            val textChannel = context.guild.getGuildMessageChannelById(giveaway.textChannelId)

            if (textChannel == null) {
                context.reply(false) {
                    styled(
                        context.locale["$LOCALE_PREFIX.giveawayend.channelDoesNotExist"],
                        Constants.ERROR
                    )
                }
                return
            }

            val message = textChannel.retrieveMessageById(messageId).await()

            if (message == null) {
                context.reply(false) {
                    styled(
                        context.locale["$LOCALE_PREFIX.giveawayend.messageDoesNotExist"],
                        Constants.ERROR
                    )
                }
                return
            }

            m.giveawayManager.finishGiveaway(message, giveaway)

            context.reply(false) {
                styled(
                    context.locale["$LOCALE_PREFIX.giveawayend.finishedGiveaway"],
                    Emotes.LORI_HAPPY
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val messageUrl = args.getOrNull(0)
            if (messageUrl == null) {
                context.explain()
                return null
            }

            return mapOf(
                options.messageUrl to messageUrl
            )
        }
    }

    class GiveawayEditExecutor(val m: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val messageUrl = string("giveaway_message_url", I18N_PREFIX.Edit.Options.MessageUrl.Text)
        }
        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val link = args[options.messageUrl]
            val split = link.split("/")

            var messageId: Long? = null
            var channelId: Long? = null

            if (split.size == 1 && split[0].isValidSnowflake()) {
                messageId = split[0].toLong()
            } else {
                messageId = split.getOrNull(split.size - 1)?.toLongOrNull()
                channelId = split.getOrNull(split.size - 2)?.toLongOrNull()
            }

            if (messageId == null) {
                context.reply(false) {
                    styled(
                        context.locale["$LOCALE_PREFIX.giveawayend.giveawayInvalidArguments", "`https://canary.discordapp.com/channels/297732013006389252/297732013006389252/594270558238146603`"],
                        Constants.ERROR
                    )
                }
                return
            }

            val (giveaway, extraEntries) = m.newSuspendedTransaction {
                val giveaway = if (channelId != null) {
                    Giveaway.find {
                        (Giveaways.guildId eq context.guild.idLong) and (Giveaways.messageId eq messageId) and (Giveaways.textChannelId eq channelId)
                    }.firstOrNull()
                } else {
                    Giveaway.find {
                        (Giveaways.guildId eq context.guild.idLong) and (Giveaways.messageId eq messageId)
                    }.firstOrNull()
                }

                val extraEntries = if (giveaway != null) {
                    GiveawayRoleExtraEntries.select(GiveawayRoleExtraEntries.roleId, GiveawayRoleExtraEntries.weight)
                        .where { GiveawayRoleExtraEntries.giveawayId eq giveaway.id }
                        .map {
                            GiveawayBuilderScreen.ExtraEntry(
                                it[GiveawayRoleExtraEntries.roleId],
                                it[GiveawayRoleExtraEntries.weight]
                            )
                        }
                } else {
                    emptyList()
                }

                Pair(giveaway, extraEntries)
            }

            if (giveaway == null) {
                context.reply(false) {
                    styled(
                        context.locale["$LOCALE_PREFIX.giveawayend.giveawayDoesNotExist"],
                        Emotes.LORI_HM
                    )
                }
                return
            }

            if (giveaway.finished) {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Edit.GiveawayAlreadyFinished),
                        CinnamonEmotes.Error
                    )
                }
                return
            }

            // Parse allowed/denied roles
            val allowedRoles = giveaway.allowedRoles?.let { Json.decodeFromString<GiveawayRoles>(it) }
            val deniedRoles = giveaway.deniedRoles?.let { Json.decodeFromString<GiveawayRoles>(it) }

            // Build the giveaway builder with existing data
            val builder = GiveawayBuilderScreen.GiveawayBuilder(
                giveaway.reason,
                giveaway.description
            ).apply {
                this.imageUrl = giveaway.imageUrl
                this.thumbnailUrl = giveaway.thumbnailUrl
                this.color = giveaway.color?.let { java.awt.Color.decode(it) } ?: LorittaColors.LorittaAqua.toJavaColor()

                // Use absolute date format for the duration
                val finishInstant = Instant.ofEpochMilli(giveaway.finishAt)
                val zonedDateTime = finishInstant.atZone(Constants.LORITTA_TIMEZONE)
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                this.duration = zonedDateTime.format(formatter)

                this.reaction = giveaway.reaction
                this.channel = context.guild.getGuildMessageChannelById(giveaway.textChannelId)
                this.numberOfWinners = giveaway.numberOfWinners
                this.roleIds = giveaway.roleIds?.map { it.toLong() }

                this.allowedRolesIds = allowedRoles?.roleIds
                this.allowedRolesIsAndCondition = allowedRoles?.isAndCondition ?: true

                this.deniedRolesIds = deniedRoles?.roleIds
                this.deniedRolesIsAndCondition = deniedRoles?.isAndCondition ?: false

                this.needsToGetDailyBeforeParticipating = giveaway.needsToGetDailyBeforeParticipating ?: false
                this.extraEntriesShouldStack = giveaway.extraEntriesShouldStack

                this.extraEntries = extraEntries.toMutableList()

                // Set edit mode fields
                this.editModeAttributes = GiveawayBuilderScreen.GiveawayBuilder.EditModeAttributes(
                    giveaway.id.value,
                    giveaway.messageId,
                    giveaway.textChannelId
                )
            }

            val screen = GiveawayBuilderScreen.Appearance(m)

            context.reply(false, screen.render(context, builder))
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val messageUrl = args.getOrNull(0)
            if (messageUrl == null) {
                context.explain()
                return null
            }

            return mapOf(
                options.messageUrl to messageUrl
            )
        }
    }
}