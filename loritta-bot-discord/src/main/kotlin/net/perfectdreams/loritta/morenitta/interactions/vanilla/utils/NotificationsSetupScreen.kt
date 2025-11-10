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
import net.perfectdreams.loritta.morenitta.DirectMessageNotification
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

            return {
                this.useComponentsV2 = true

                this.components += Container {
                    this.accentColorRaw = LorittaColors.LorittaAqua.rgb

                    this.components += TextDisplay(
                        buildString {
                            appendLine("### ${Emotes.LoriMegaphone} ${context.i18nContext.get(SETUP_I18N_PREFIX.Title)}")
                            appendLine(context.i18nContext.get(SETUP_I18N_PREFIX.ChooseWhichNotification))
                        })

                    for (notification in DirectMessageNotification.all) {
                        val toggle = createNotificationTypeToggleButton(
                            context,
                            configuredNotifications,
                            notification.type
                        )

                        this.components += Section(toggle) {
                            this.components += OptionExplanationCombo(
                                context.i18nContext.get(notification.title),
                                context.i18nContext.get(notification.description)
                            )
                        }
                    }
                }
            }
        }
    }
}