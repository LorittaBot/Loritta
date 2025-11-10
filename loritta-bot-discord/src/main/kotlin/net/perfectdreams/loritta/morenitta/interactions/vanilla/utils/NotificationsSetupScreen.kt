package net.perfectdreams.loritta.morenitta.interactions.vanilla.utils

import dev.minn.jda.ktx.interactions.components.*
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.components.textdisplay.TextDisplay
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserNotificationSettings
import net.perfectdreams.loritta.common.emojis.LorittaEmojis
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.NotificationType
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.components.ComponentContext
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import org.jetbrains.exposed.sql.*
import java.time.Instant

sealed class NotificationsSetupScreen(val m: LorittaBot) {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Notifications
        private val SETUP_I18N_PREFIX = I18N_PREFIX.Configure
    }

    abstract suspend fun render(context: UnleashedContext): InlineMessage<*>.() -> (Unit)

    fun isNotificationTypeEnabled(configuredNotifications: Map<NotificationType, Boolean>, type: NotificationType): Boolean {
        return configuredNotifications.getOrDefault(type, true)
    }

    fun createNotificationTypeToggleButton(
        context: UnleashedContext,
        enabledNotifications: Map<NotificationType, Boolean>,
        type: NotificationType,
    ): Button {
        val isEnabled = isNotificationTypeEnabled(enabledNotifications, type)

        return m.interactivityManager.buttonForUser(
            context.user,
            context.alwaysEphemeral,
            if (isEnabled)
                ButtonStyle.PRIMARY
            else
                ButtonStyle.SECONDARY,
            if (isEnabled)
                context.i18nContext.get(SETUP_I18N_PREFIX.Enabled)
            else
                context.i18nContext.get(SETUP_I18N_PREFIX.Disabled),
        ) { context ->
            val deferred = context.deferEdit()

            m.transaction {
                UserNotificationSettings.upsert(UserNotificationSettings.userId, UserNotificationSettings.type) {
                    it[UserNotificationSettings.userId] = context.user.idLong
                    it[UserNotificationSettings.type] = type
                    it[UserNotificationSettings.enabled] = isEnabled.not()
                    it[UserNotificationSettings.configuredAt] = Instant.now()
                }
            }

            deferred.editOriginal(MessageEdit { apply(render(context)) }).await()
        }
    }

    fun OptionExplanationCombo(title: String, description: String? = null, value: String? = null): TextDisplay {
        return TextDisplay(
            buildString {
                appendLine("**$title**")
                if (description != null) {
                    appendLine("-# $description")
                }
                if (value != null) {
                    appendLine(value)
                }
            }
        )
    }

    class Setup(m: LorittaBot) : NotificationsSetupScreen(m) {
        override suspend fun render(context: UnleashedContext): InlineMessage<*>.() -> Unit {
            val configuredNotifications = m.transaction {
                UserNotificationSettings.selectAll()
                    .where {
                        UserNotificationSettings.userId eq context.user.idLong
                    }
                    .associate { it[UserNotificationSettings.type] to it[UserNotificationSettings.enabled] }
            }

            val dailyReminderToggle = createNotificationTypeToggleButton(
                context,
                configuredNotifications,
                NotificationType.DAILY_REMINDER
            )

            val marriageExpirationReminderToggle = createNotificationTypeToggleButton(
                context,
                configuredNotifications,
                NotificationType.MARRIAGE_EXPIRATION_REMINDER
            )

            val marriageExpiredReminderToggle = createNotificationTypeToggleButton(
                context,
                configuredNotifications,
                NotificationType.MARRIAGE_EXPIRED
            )

            val marriageRenewedReminderToggle = createNotificationTypeToggleButton(
                context,
                configuredNotifications,
                NotificationType.MARRIAGE_RENEWED
            )

            val marriageLoveLetterToggle = createNotificationTypeToggleButton(
                context,
                configuredNotifications,
                NotificationType.MARRIAGE_LOVE_LETTER
            )

            val experienceLevelUpToggle = createNotificationTypeToggleButton(
                context,
                configuredNotifications,
                NotificationType.EXPERIENCE_LEVEL_UP
            )

            val giveawayEndedToggle = createNotificationTypeToggleButton(
                context,
                configuredNotifications,
                NotificationType.GIVEAWAY_ENDED
            )

            return {
                this.useComponentsV2 = true

                this.components += Container {
                    this.accentColorRaw = LorittaColors.LorittaAqua.rgb

                    this.components += TextDisplay(
                        buildString {
                            appendLine("### ${Emotes.LoriMegaphone} ${context.i18nContext.get(SETUP_I18N_PREFIX.Title)}")
                            appendLine(context.i18nContext.get(SETUP_I18N_PREFIX.ChooseWhichNotification))
                        })

                    this.components += Section(dailyReminderToggle) {
                        this.components += OptionExplanationCombo(
                            context.i18nContext.get(SETUP_I18N_PREFIX.Types.DailyReminder.Title),
                            context.i18nContext.get(SETUP_I18N_PREFIX.Types.DailyReminder.Description)
                        )
                    }

                    this.components += Section(marriageExpirationReminderToggle) {
                        this.components += OptionExplanationCombo(
                            context.i18nContext.get(SETUP_I18N_PREFIX.Types.MarriageExpirationReminder.Title),
                            context.i18nContext.get(SETUP_I18N_PREFIX.Types.MarriageExpirationReminder.Description)
                        )
                    }

                    this.components += Section(marriageExpiredReminderToggle) {
                        this.components += OptionExplanationCombo(
                            context.i18nContext.get(SETUP_I18N_PREFIX.Types.MarriageExpired.Title),
                            context.i18nContext.get(SETUP_I18N_PREFIX.Types.MarriageExpired.Description)
                        )
                    }

                    this.components += Section(marriageRenewedReminderToggle) {
                        this.components += OptionExplanationCombo(
                            context.i18nContext.get(SETUP_I18N_PREFIX.Types.MarriageRenewed.Title),
                            context.i18nContext.get(SETUP_I18N_PREFIX.Types.MarriageRenewed.Description)
                        )
                    }

                    this.components += Section(marriageLoveLetterToggle) {
                        this.components += OptionExplanationCombo(
                            context.i18nContext.get(SETUP_I18N_PREFIX.Types.MarriageLoveLetter.Title),
                            context.i18nContext.get(SETUP_I18N_PREFIX.Types.MarriageLoveLetter.Description)
                        )
                    }

                    this.components += Section(experienceLevelUpToggle) {
                        this.components += OptionExplanationCombo(
                            context.i18nContext.get(SETUP_I18N_PREFIX.Types.ExperienceLevelUp.Title),
                            context.i18nContext.get(SETUP_I18N_PREFIX.Types.ExperienceLevelUp.Description)
                        )
                    }

                    this.components += Section(giveawayEndedToggle) {
                        this.components += OptionExplanationCombo(
                            context.i18nContext.get(SETUP_I18N_PREFIX.Types.ManagedGiveawayEnded.Title),
                            context.i18nContext.get(SETUP_I18N_PREFIX.Types.ManagedGiveawayEnded.Description)
                        )
                    }
                }
            }
        }
    }
}