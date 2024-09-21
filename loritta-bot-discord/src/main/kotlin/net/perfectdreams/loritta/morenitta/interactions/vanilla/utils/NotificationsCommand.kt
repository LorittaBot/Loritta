package net.perfectdreams.loritta.morenitta.interactions.vanilla.utils

import dev.minn.jda.ktx.messages.InlineMessage
import kotlinx.datetime.toJavaInstant
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.NotificationUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.toKordColor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationBuilder
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.serializable.CorreiosPackageUpdateUserNotification
import net.perfectdreams.loritta.serializable.DailyTaxTaxedUserNotification
import net.perfectdreams.loritta.serializable.DailyTaxWarnUserNotification
import net.perfectdreams.loritta.serializable.UnknownUserNotification
import net.perfectdreams.loritta.serializable.UserId
import java.util.UUID

class NotificationsCommand : SlashCommandDeclarationWrapper {
    override fun command(): SlashCommandDeclarationBuilder =
        slashCommand(
            name = I18N_PREFIX.Label,
            description = I18N_PREFIX.Description,
            category = CommandCategory.UTILS,
            uniqueId = UUID.fromString("eda7d882-b0fb-4c77-951b-e19676b555c1")
        ) {
            enableLegacyMessageSupport = true

            alternativeLegacyAbsoluteCommandPaths.apply {
                add("notificações")
                add("notificacoes")
            }

            subcommand(
                name = ListExecutor.I18N_PREFIX.Label,
                description = ListExecutor.I18N_PREFIX.Description,
                uniqueId = UUID.fromString("05d0a678-1962-4e69-afca-81546e71711b")
            ) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("listar")
                    add("todas")
                    add("all")
                }

                executor = ListExecutor()
            }

            subcommand(
                name = ViewExecutor.I18N_PREFIX.Label,
                description = ViewExecutor.I18N_PREFIX.Description,
                uniqueId = UUID.fromString("f3b3b3b4-3b3b-4b3b-8b3b-3b3b3b3b3b3b")
            ) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("ver")
                    add("visualizar")
                }

                executor = ViewExecutor()
            }
        }

    class ListExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(ephemeral = true)

            val notifications = context.loritta.pudding.notifications.getUserNotifications(UserId(context.user.idLong), 10, 0)

            context.reply(ephemeral = true) {
                embed {
                    title = context.i18nContext.get(I18N_PREFIX.Title)

                    for (notification in notifications) {
                        field(
                            "[${notification.id}] ${
                                when (notification) {
                                    is DailyTaxTaxedUserNotification -> context.i18nContext.get(
                                        NotificationsCommand.I18N_PREFIX.DailyTaxTaxedUserNotification
                                    )
                                    is DailyTaxWarnUserNotification -> context.i18nContext.get(
                                        NotificationsCommand.I18N_PREFIX.DailyTaxWarnUserNotification
                                    )
                                    is CorreiosPackageUpdateUserNotification -> context.i18nContext.get(
                                        NotificationsCommand.I18N_PREFIX.CorreiosPackageUpdate
                                    )
                                    is UnknownUserNotification -> context.i18nContext.get(
                                        NotificationsCommand.I18N_PREFIX.UnknownNotification
                                    )
                                }}",
                            "<t:${notification.timestamp.epochSeconds}:d> <t:${notification.timestamp.epochSeconds}:t> | <t:${notification.timestamp.epochSeconds}:R>",
                            false
                        )
                    }

                    color = LorittaColors.LorittaAqua.rgb
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> = emptyMap()

        companion object {
            val I18N_PREFIX = NotificationsCommand.I18N_PREFIX.List
        }
    }

    class ViewExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val id = string("id", I18N_PREFIX.Options.Id.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(ephemeral = true)

            val notification = context.loritta.pudding.notifications.getUserNotification(UserId(context.user.idLong), args[options.id].toLong())
                ?: context.fail(
                    emote = "❌",
                    text = context.i18nContext.get(I18N_PREFIX.CouldntFindTheNotification),
                    ephemeral = true
                )

            context.reply(ephemeral = true) {
                NotificationUtils.buildUserNotificationMessage(
                    context.loritta,
                    context.i18nContext,
                    notification,
                    context.loritta.config.loritta.website.url
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val id = args.firstOrNull() ?: return null
            return mapOf(
                options.id to id
            )
        }

        companion object {
            val I18N_PREFIX = NotificationsCommand.I18N_PREFIX.View

            fun buildDailyTaxMessage(
                message: InlineMessage<MessageCreateData>,
                i18nContext: I18nContext,
                lorittaWebsiteUrl: String,
                userId: UserId,
                data: DailyTaxWarnUserNotification
            ) {
                message.embed {
                    title = i18nContext.get(I18nKeysData.InactiveDailyTax.TitleWarning)

                    description = i18nContext.get(
                        I18nKeysData.InactiveDailyTax.Warn(
                            user = "<@${userId.value}>",
                            sonhosTaxBracketThreshold = data.minimumSonhosForTrigger,
                            currentSonhos = data.currentSonhos,
                            daysWithoutGettingDaily = data.maxDayThreshold,
                            howMuchWillBeRemoved = data.howMuchWillBeRemoved,
                            taxPercentage = data.tax,
                            inactivityTaxTimeWillBeTriggeredAt = "<t:${data.inactivityTaxTimeWillBeTriggeredAt.epochSeconds}:f>",
                            timeWhenDailyTaxIsTriggered = "<t:${data.inactivityTaxTimeWillBeTriggeredAt.epochSeconds}:t>",
                            dailyLink = GACampaigns.dailyWebRewardDiscordCampaignUrl(
                                lorittaWebsiteUrl,
                                "daily-tax-message",
                                "user-warned-about-taxes"
                            ),
                            premiumLink = GACampaigns.premiumUpsellDiscordCampaignUrl(
                                lorittaWebsiteUrl,
                                "daily-tax-message",
                                "user-warned-about-taxes"
                            )
                        )
                    ).joinToString("\n")

                    color = LorittaColors.LorittaRed.rgb

                    timestamp = data.timestamp.toJavaInstant()

                    image = "https://stuff.loritta.website/loritta-sonhos-drool-cooki.png"
                }
            }
        }
    }

    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Notifications
    }
}