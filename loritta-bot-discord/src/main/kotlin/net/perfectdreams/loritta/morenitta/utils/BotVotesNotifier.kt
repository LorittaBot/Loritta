package net.perfectdreams.loritta.morenitta.utils

import dev.minn.jda.ktx.interactions.components.Container
import dev.minn.jda.ktx.interactions.components.MediaGallery
import dev.minn.jda.ktx.interactions.components.TextDisplay
import dev.minn.jda.ktx.messages.MessageCreate
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.discord.utils.RunnableCoroutine
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.BotVotesUserAvailableNotifications
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class BotVotesNotifier(val m: LorittaBot) : RunnableCoroutine {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    override suspend fun run() {
        val now = Instant.now()

        // We do this in two separate transactions because we don't want to ".await()" within a transaction because that blocks the transaction
        // and we don't want concurrent serializations to cause the entire transaction to be retried
        val usersToBeNotifiedData = m.transaction {
            // Get all users that needs to be notified
            BotVotesUserAvailableNotifications.selectAll().where {
                BotVotesUserAvailableNotifications.notified eq false and (BotVotesUserAvailableNotifications.notifyAt lessEq now)
            }.toList()
        }

        // Notify them!
        for (userToBeNotifiedData in usersToBeNotifiedData) {
            val userId = userToBeNotifiedData[BotVotesUserAvailableNotifications.userId]

            try {
                val privateChannel = m.getOrRetrievePrivateChannelForUserOrNullIfUserDoesNotExist(userId)
                if (privateChannel != null) {
                    logger.info { "Notifying user ${userId} about top.gg vote..." }
                    privateChannel.sendMessage(
                        MessageCreate {
                            this.useComponentsV2 = true

                            this.components += Container {
                                this.accentColor = LorittaColors.LorittaAqua.rgb

                                +TextDisplay(
                                    buildString {
                                        appendLine("### ${m.languageManager.defaultI18nContext.get(I18nKeysData.Commands.Command.Vote.Notification.Topgg.Title)} ${Emotes.LoriSmile}")
                                        appendLine()
                                        for (line in m.languageManager.defaultI18nContext.get(I18nKeysData.Commands.Command.Vote.Notification.Topgg.Description(Emotes.LoriLurk.toString(), Emotes.LoriHeart.toString()))) {
                                            appendLine(line)
                                        }
                                        appendLine()
                                        appendLine("https://top.gg/bot/${m.config.loritta.discord.applicationId}/vote")
                                    }
                                )

                                +MediaGallery {
                                    this.item("https://stuff.loritta.website/loritta-happy.gif")
                                }
                            }
                        }
                    )
                        .await()
                }
            } catch (e: Exception) {
                logger.warn(e) { "Something went wrong while attempting to notify $userId about vote ${userToBeNotifiedData[BotVotesUserAvailableNotifications.id]}!" }
            }
        }

        m.transaction {
            BotVotesUserAvailableNotifications.update({ BotVotesUserAvailableNotifications.id inList usersToBeNotifiedData.map { it[BotVotesUserAvailableNotifications.id] }}) {
                it[BotVotesUserAvailableNotifications.notified] = true
            }
        }
    }
}
