package net.perfectdreams.loritta.morenitta.websitedashboard.routes.banappeals

import dev.minn.jda.ktx.interactions.components.Thumbnail
import dev.minn.jda.ktx.messages.MessageCreate
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.html.b
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.p
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.perfectdreams.etherealgambi.client.EtherealGambiClient
import net.perfectdreams.etherealgambi.data.api.UploadFileResponse
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.banappeals.BanAppealResult
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.BanAppeals
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.morenitta.banappeals.BanAppealsUtils
import net.perfectdreams.loritta.morenitta.rpc.LorittaRPC
import net.perfectdreams.loritta.morenitta.rpc.payloads.NotifyBanAppealRequest
import net.perfectdreams.loritta.morenitta.rpc.execute
import net.perfectdreams.loritta.morenitta.rpc.payloads.NotifyBanAppealResponse
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroText
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.simpleHeroImage
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissSoundEffect
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.configSaved
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.UserBannedState
import net.perfectdreams.loritta.serializable.UserId
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.time.OffsetDateTime
import java.util.*

class PostBanAppealsRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/form") {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    // TODO: Move somewhere else
    val etherealGambiClient = EtherealGambiClient(website.loritta.config.loritta.etherealGambiService.baseUrl)

    @Serializable
    data class BanAppealRequest(
        // This is for overridden ban appeals!
        val userId: Long,
        val whatDidYouDo: String,
        val whyDidYouBreakThem: String,
        val accountIds: List<Long> = listOf(),
        val whyShouldYouBeUnbanned: String,
        val additionalComments: String,
        val files: List<File>
    ) {
        @Serializable
        data class File(
            val data: String,
            val name: String,
        )
    }

    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        try {
            val request = Json.decodeFromString<BanAppealRequest>(call.receiveText())
            if (request.whatDidYouDo.length !in 1..1000 || request.whyDidYouBreakThem.length !in 1..1000 || request.whyShouldYouBeUnbanned.length !in 1..1000 || request.additionalComments.length !in 0..1000) {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Uma ou mais opções estão inválidas!"
                        )
                    )
                }
                return
            }

            if (request.files.size > 10) {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Você só pode enviar até 10 imagens!"
                        )
                    )
                }
                return
            }

            // We will "reserve" the files, but we will only upload if the appeal was successfully inserted
            val uploadedFiles = mutableMapOf<String, ByteArray>()

            for (file in request.files) {
                val imageBytes = Base64.getDecoder().decode(file.data)
                val path = "/ban-appeals/${session.userId}-${UUID.randomUUID()}.${file.name.substringAfterLast(".")}"

                uploadedFiles[path] = imageBytes
            }

            val result = website.loritta.transaction {
                val now = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)
                val activeAppealTime = now.minusSeconds(BanAppealsUtils.BAN_APPEAL_COOLDOWN.inWholeSeconds)

                // We check by the submitted by instead of whom we are acting on behalf of, to avoid malicious users making forms for other users that they can't act upon
                val activeBanAppeal = BanAppeals.selectAll()
                    .where {
                        BanAppeals.submittedBy eq session.userId and (BanAppeals.submittedAt greaterEq activeAppealTime)
                    }
                    .firstOrNull()

                if (activeBanAppeal != null)
                    return@transaction AppealCreationResult.OnCooldown(now, activeBanAppeal[BanAppeals.submittedAt].plusSeconds(BanAppealsUtils.BAN_APPEAL_COOLDOWN.inWholeSeconds))

                val banState = website.loritta.pudding.users.getUserBannedState(UserId(request.userId))
                if (banState == null)
                    return@transaction AppealCreationResult.UserIsNotBanned

                val banAppeal = BanAppeals.insert {
                    it[BanAppeals.submittedBy] = session.userId
                    it[BanAppeals.userId] = request.userId

                    it[BanAppeals.languageId] = website.loritta.languageManager.getIdByI18nContext(i18nContext)
                    it[BanAppeals.whatDidYouDo] = request.whatDidYouDo
                    it[BanAppeals.whyDidYouBreakThem] = request.whyDidYouBreakThem
                    it[BanAppeals.accountIds] = request.accountIds
                    it[BanAppeals.whyShouldYouBeUnbanned] = request.whyShouldYouBeUnbanned
                    it[BanAppeals.additionalComments] = request.additionalComments
                    it[BanAppeals.submittedAt] = now
                    it[BanAppeals.banEntry] = banState.id

                    it[BanAppeals.files] = uploadedFiles.keys.toList()
                    it[BanAppeals.appealResult] = BanAppealResult.PENDING
                    it[BanAppeals.reviewedAt] = null
                    it[BanAppeals.reviewedBy] = null
                    it[BanAppeals.reviewerNotes] = null
                }

                return@transaction AppealCreationResult.Success(banAppeal, banState)
            }

            when (result) {
                is AppealCreationResult.Success -> {
                    for ((filePath, imageBytes) in uploadedFiles) {
                        val result = etherealGambiClient.uploadFile(
                            website.loritta.config.loritta.etherealGambiService.token,
                            filePath,
                            true,
                            imageBytes
                        )

                        logger.info { "Uploaded file $filePath to EtherealGambi! Result: $result" }

                        when (result) {
                            is UploadFileResponse.Success -> {}
                            else -> {
                                logger.warn { "Something went wrong while trying to upload a file! Result: $result" }
                                call.respondHtmlFragment(status = HttpStatusCode.InternalServerError) {
                                    blissShowToast(
                                        createEmbeddedToast(
                                            EmbeddedToast.Type.WARN,
                                            "Falha ao enviar as imagens!"
                                        )
                                    )
                                }
                                return
                            }
                        }
                    }

                    val response = LorittaRPC.NotifyBanAppeal.execute(
                        website.loritta,
                        DiscordUtils.getLorittaClusterForGuildId(website.loritta, website.loritta.config.loritta.banAppeals.guildId),
                        NotifyBanAppealRequest(
                            result.appeal[BanAppeals.id].value,
                            website.loritta.config.loritta.banAppeals.guildId,
                            website.loritta.config.loritta.banAppeals.channelId,
                        )
                    )

                    when (response) {
                        NotifyBanAppealResponse.UserNotFound -> {
                            call.respondHtmlFragment(status = HttpStatusCode.InternalServerError) {
                                blissShowToast(
                                    createEmbeddedToast(
                                        EmbeddedToast.Type.WARN,
                                        "Algo deu errado ao enviar o seu formulário!"
                                    ) {
                                        text("Usuário não encontrado")
                                    }
                                )
                            }
                            return
                        }
                        NotifyBanAppealResponse.Success -> {}
                    }

                    // We should ALWAYS send the DMs to the user that created the report, even tho they may be sending on behalf of someone else
                    // This is to avoid spamming innocent users
                    val privateChannel = website.loritta.getOrRetrievePrivateChannelForUserOrNullIfUserDoesNotExist(session.userId)
                    var successfullySentDM = false

                    if (privateChannel != null) {
                        try {
                            privateChannel.sendMessage(
                                MessageCreate {
                                    this.useComponentsV2 = true

                                    container {
                                        this.accentColorRaw = LorittaColors.BanAppealPending.rgb

                                        section(Thumbnail("https://assets.perfectdreams.media/loritta/loritta-support.png")) {
                                            text(
                                                buildString {
                                                    appendLine("### Recebemos o seu apelo!")

                                                    appendLine("Recebemos o seu apelo de ban! Em breve você terá uma resposta dizendo se o seu apelo foi aprovado ou não.")
                                                    appendLine()
                                                    appendLine("**Boa sorte! ${Emotes.LoriLick}**")
                                                    appendLine()
                                                    appendLine("-# Apelo #${result.appeal[BanAppeals.id].value}")
                                                }
                                            )
                                        }
                                    }
                                }
                            ).await()

                            successfullySentDM = true
                        } catch (e: Exception) {
                            logger.warn(e) { "Something went wrong while trying to tell the user that the appeal has been successfully received!" }
                        }
                    }

                    call.respondHtmlFragment {
                        blissSoundEffect("configSaved")

                        blissShowToast(
                            createEmbeddedToast(
                                EmbeddedToast.Type.SUCCESS,
                                "Apelo enviado!"
                            )
                        )

                        heroWrapper {
                            simpleHeroImage("https://assets.perfectdreams.media/loritta/loritta-support.png")

                            heroText {
                                h1 {
                                    text("Apelo enviado!")
                                }

                                p {
                                    text("O seu apelo chegou até a nossa equipe e ele será analisado em breve.")
                                }

                                if (successfullySentDM) {
                                    p {
                                        text("Você recebeu uma mensagem direta confirmando o envio. As atualizações sobre o apelo serão enviadas via mensagem direta.")
                                    }
                                } else {
                                    p {
                                        text("Eu tentei te enviar uma mensagem direta confirmando o envio, mas as suas mensagens diretas estão fechadas! Se você quiser receber atualizações sobre o apelo, mantenha ele aberto!")
                                    }
                                }

                                p {
                                    b {
                                        text("Boa sorte!")
                                    }
                                }
                            }
                        }
                    }
                }

                is AppealCreationResult.OnCooldown -> {
                    call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                        blissShowToast(
                            createEmbeddedToast(
                                EmbeddedToast.Type.WARN,
                                "Você está em cooldown!"
                            ) {
                                text("Você deve esperar ${DateUtils.formatDateDiff(i18nContext, result.now.toInstant(), result.expiresAt.toInstant())} antes de poder enviar outro apelo!")
                            }
                        )
                    }
                }

                is AppealCreationResult.UserIsNotBanned -> {
                    call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                        blissShowToast(
                            createEmbeddedToast(
                                EmbeddedToast.Type.WARN,
                                "Usuário não está banido da Loritta!"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            logger.warn(e) { "Something went wrong while trying to create a ban appeal!" }
            call.respondHtmlFragment(status = HttpStatusCode.InternalServerError) {
                blissShowToast(
                    createEmbeddedToast(
                        EmbeddedToast.Type.WARN,
                        "Algo deu errado ao enviar o seu formulário!"
                    )
                )
            }
            return
        }
    }

    private sealed class AppealCreationResult {
        data class Success(val appeal: InsertStatement<Number>, val banState: UserBannedState) : AppealCreationResult()
        data class OnCooldown(
            val now: OffsetDateTime,
            val expiresAt: OffsetDateTime
        ) : AppealCreationResult()
        data object UserIsNotBanned : AppealCreationResult()
    }
}