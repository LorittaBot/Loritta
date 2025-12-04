package net.perfectdreams.loritta.morenitta.modules

import dev.minn.jda.ktx.messages.MessageCreate
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.toJavaInstant
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.BanAppeals
import net.perfectdreams.loritta.cinnamon.pudding.tables.CrazyManagerSentDirectMessages
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.loritta.morenitta.utils.BomDiaECia
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.MiscellaneousConfig
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

class CrazyManagerModule(val loritta: LorittaBot) : MessageReceivedModule {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
        private val startedAt = LocalDateTime.of(2025, 12, 4, 15, 0, 0)
            .atZone(Constants.LORITTA_TIMEZONE)
    }

    val mutex = Mutex()

	override suspend fun matches(
        event: LorittaMessageEvent,
        lorittaUser: LorittaUser,
        lorittaProfile: Profile?,
        serverConfig: ServerConfig,
        locale: BaseLocale,
        i18nContext: I18nContext
    ): Boolean {
        val now = LocalDate.now(Constants.LORITTA_TIMEZONE)

        return now in LocalDate.of(2025, 12, 1)..LocalDate.of(2025, 12, 31)
	}

	override suspend fun handle(
		event: LorittaMessageEvent,
		lorittaUser: LorittaUser,
		lorittaProfile: Profile?,
		serverConfig: ServerConfig,
		locale: BaseLocale,
		i18nContext: I18nContext
	): Boolean {
        val banState = loritta.pudding.users.getUserBannedState(UserId(event.author.idLong))
        val now = Instant.now()

        if (banState != null && banState.valid && startedAt >= banState.bannedAt.toJavaInstant().atZone(Constants.LORITTA_TIMEZONE) && banState.expiresAt == null && Duration.between(banState.bannedAt.toJavaInstant(), now).toDays() >= 90) {
            mutex.withLock {
                val shouldSendDM = loritta.transaction {
                    val sentDM = CrazyManagerSentDirectMessages.selectAll()
                        .where {
                            CrazyManagerSentDirectMessages.userId eq event.author.idLong
                        }
                        .count()

                    return@transaction sentDM == 0L
                }

                if (shouldSendDM) {
                    logger.info { "Sending direct message about the crazy manager event for ${event.author.idLong}..." }
                    val privateChannel = loritta.getOrRetrievePrivateChannelForUser(event.author.idLong)

                    var success: Boolean

                    try {
                        privateChannel.sendMessage(
                            MessageCreate {
                                this.useComponentsV2 = true

                                container {
                                    this.accentColorRaw = LorittaColors.LorittaAqua.rgb
                                    this.text(
                                        buildString {
                                            appendLine("### ${Emotes.LoriDemon} O gerente ficou maluco!")
                                            appendLine("Como presente de final de ano, estamos sendo generosos e desbanindo pessoas que foram banidas da Loritta, até aquelas que receberam um ban permanente!")
                                            appendLine()
                                            appendLine("**Atualmente você está banido da Loritta por ${banState.reason}**")
                                            appendLine()
                                            appendLine("**${Emotes.LoriLurk} Condições**")
                                            appendLine("* Você precisa estar banido há mais de três meses")
                                            appendLine("* Você não pode ter mais de quatro evasões de ban (conta principal + quatro outras contas banidas)")
                                            appendLine("* Se você foi banido por chargeback, você precisa estar banido há mais de dois anos")
                                            appendLine()
                                            appendLine("**${Emotes.LoriFire} Motivos que não iremos desbanir**")
                                            appendLine("* Se você evadiu mais que quatro vezes")
                                            appendLine("* Se você causou muito transtorno para a equipe")
                                            appendLine("* Se você abusou de bugs relacionados com a economia da Loritta (sonhos) ou do SparklyPower (sonecas/pesadelos)")
                                            appendLine()
                                            appendLine("O gerente ficará maluco até <t:1767236400:F> (<t:1767236400:R>)!")
                                            appendLine()
                                            appendLine("**Boa sorte se você for preencher o apelo! ${Emotes.LoriLick}**")
                                        }
                                    )
                                }

                                actionRow(
                                    Button.of(
                                        ButtonStyle.LINK,
                                        GACampaigns.createUrlWithCampaign(
                                            loritta.config.loritta.banAppeals.url.removeSuffix("/") + "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/",
                                            "discord",
                                            "button",
                                            "unban-appeal",
                                            "user-banned-dm"
                                        ).toString(),
                                        i18nContext.get(I18nKeysData.Commands.UserIsLorittaBanned.SendABanAppeal)
                                    ).withEmoji(Emotes.LoriAngel.toJDA())
                                )
                            }
                        ).await()

                        success = true
                    } catch (e: Exception) {
                        logger.warn(e) { "Something went wrong while trying to notify the crazy manager event for ${event.author.idLong}!" }

                        success = false
                    }

                    loritta.transaction {
                        CrazyManagerSentDirectMessages.insert {
                            it[CrazyManagerSentDirectMessages.userId] = event.author.idLong
                            it[CrazyManagerSentDirectMessages.sentAt] = OffsetDateTime.now()
                            it[CrazyManagerSentDirectMessages.success] = success
                            it[CrazyManagerSentDirectMessages.banEntry] = banState.id
                        }
                    }
                }
            }
        }

		return false
	}
}