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
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.asUserNameCodeBlockPreviewTag
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
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

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
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

        val message = MessageUtils.generateMessage(
            request.message,
            guild,
            request.placeholders,
            true
        )

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