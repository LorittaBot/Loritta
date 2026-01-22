package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds

import com.github.benmanes.caffeine.cache.Caffeine
import dev.minn.jda.ktx.coroutines.await
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.asUserNameCodeBlockPreviewTag
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissSoundEffect
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import java.util.concurrent.TimeUnit

class PostTestMessageGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/test-message") {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    // THE HACKIEST HACK YOU EVER HACKED
    private val messageSendCooldown = Caffeine.newBuilder().expireAfterAccess(30L, TimeUnit.SECONDS).maximumSize(100).build<Long, Long>().asMap()

    @Serializable
    data class TestMessageRequest(
        val channelId: Long?,
        val message: String,
        val placeholders: Map<String, String>
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val request = Json.decodeFromString<TestMessageRequest>(call.receiveText())
        val channelId = request.channelId
        val userId = member.user.idLong

        // Rate Limit
        val last = this.messageSendCooldown.getOrDefault(userId, 0L)

        val diff = System.currentTimeMillis() - last
        if (4000 >= diff) {
            call.respondHtmlFragment(status = HttpStatusCode.TooManyRequests) {
                blissShowToast(
                    createEmbeddedToast(
                        EmbeddedToast.Type.WARN,
                        "Algo deu errado ao enviar a mensagem!"
                    ) {
                        text("Você já enviou uma mensagem recentemente! Espere um pouco antes de tentar enviar uma nova mensagem.")
                    }
                )
            }
            return
        }

        this.messageSendCooldown[userId] = System.currentTimeMillis()

        val channel = if (channelId == null) {
            website.loritta.getOrRetrievePrivateChannelForUser(session.userId)
        } else {
            guild.getGuildMessageChannelById(channelId)
        }

        if (channel == null) {
            call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                blissShowToast(
                    createEmbeddedToast(
                        EmbeddedToast.Type.WARN,
                        "Algo deu errado ao enviar a mensagem!"
                    ) {
                        text("O canal que você selecionou não existe.")
                    }
                )
            }
            return
        }

        if (channel is IPermissionContainer && !channel.canTalk()) {
            call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                blissShowToast(
                    createEmbeddedToast(
                        EmbeddedToast.Type.WARN,
                        "Algo deu errado ao enviar a mensagem!"
                    ) {
                        text("A Loritta não tem permissão para enviar mensagens no canal selecionado.")
                    }
                )
            }
        }

        val message = try {
            MessageUtils.generateMessage(
                request.message,
                guild,
                request.placeholders,
                true
            )
        } catch (e: IllegalStateException) {
            if (e.message == "Cannot build message with no V2 components, or did you forget to disable them?") {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Mensagem inválida!"
                        ) {
                            text("Você precisa ter pelo ou menos um componente na mensagem!")
                        }
                    )
                }
                return
            }

            if (e.message == "Cannot have a section without an accessory!") {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Mensagem inválida!"
                        ) {
                            text("Uma seção precisa ter um acessório!")
                        }
                    )
                }
                return
            }

            if (e.message == "Cannot build an empty message. You need at least one of content, embeds, components, poll, or files") {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Mensagem inválida!"
                        ) {
                            text("Você não pode enviar uma mensagem vazia!")
                        }
                    )
                }
                return
            }
            throw e
        } catch (e: IllegalArgumentException) {
            if (e.message == "Row may not be empty") {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Mensagem inválida!"
                        ) {
                            text("Você não pode ter uma linha de botões vazia! Remova a linha ou adicione um botão nela.")
                        }
                    )
                }
                return
            }

            if (e.message == "URL may not be blank") {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Mensagem inválida!"
                        ) {
                            text("Você não pode ter uma URL vazia!")
                        }
                    )
                }
                return
            }

            if (e.message == "Content may not be blank") {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Mensagem inválida!"
                        ) {
                            text("Você não pode ter um componente de texto vazio!")
                        }
                    )
                }
                return
            }

            if (e.message == "Items may not be empty") {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Mensagem inválida!"
                        ) {
                            text("Você não pode ter uma galeria de mídia vazia!")
                        }
                    )
                }
                return
            }

            throw e
        }

        // This is a bit crappy, but we need to create a builder from the already generated message
        val patchedMessage = MessageCreateBuilder.from(message)
        if (5 > patchedMessage.components.size) { // Below the component limit
            patchedMessage.addComponents(
                ActionRow.of(
                    website.loritta.interactivityManager.button(
                        false,
                        ButtonStyle.SECONDARY,
                        i18nContext.get(I18nKeysData.Common.TestMessageWarning.ButtonLabel),
                        {
                            this.loriEmoji = Emotes.LoriCoffee
                        }
                    ) {
                        it.reply(true) {
                            styled(
                                i18nContext.get(I18nKeysData.Common.TestMessageWarning.MessageWasTestedByUser("${member.user.asMention} [${member.user.asUserNameCodeBlockPreviewTag(true)}]")),
                                Emotes.LoriCoffee
                            )

                            styled(
                                i18nContext.get(I18nKeysData.Common.TestMessageWarning.DontWorryTheMessageWillOnlyShowUpWhileTesting),
                                Emotes.LoriLurk
                            )
                        }
                    }
                )
            )
        }

        try {
            channel.sendMessage(patchedMessage.build()).await()
        } catch (e: Exception) {
            logger.warn(e) { "Something went wrong while trying to send a test message to the channel ${channel.id} in guild ${guild.id}! Message is ${request.message}" }
            call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                blissShowToast(
                    createEmbeddedToast(
                        EmbeddedToast.Type.WARN,
                        "Algo deu errado ao enviar a mensagem!"
                    ) {
                        text("Não foi possível enviar a mensagem.")
                    }
                )
            }
            return
        }

        call.respondHtmlFragment {
            blissSoundEffect("configSaved")
            blissShowToast(
                createEmbeddedToast(
                    EmbeddedToast.Type.SUCCESS,
                    "Mensagem enviada!"
                )
            )
        }
    }
}