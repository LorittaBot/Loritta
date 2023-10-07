package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.twitch

import androidx.compose.runtime.*
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.*
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.ResourceChecker
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.ConfigureGuildTwitchScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalSpicyInfo
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalUserIdentification
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Toast
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPath
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPathWithArguments
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.GuildViewModel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.TwitchViewModel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.viewModel
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.serializable.config.TwitchAccountTrackState
import net.perfectdreams.loritta.serializable.dashboard.requests.DashGuildScopedRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.DashGuildScopedResponse
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener

@Composable
fun GuildTwitch(
    m: LorittaDashboardFrontend,
    screen: ConfigureGuildTwitchScreen,
    i18nContext: I18nContext,
    guildViewModel: GuildViewModel
) {
    val spicyInfo = LocalSpicyInfo.current
    val userInfo = LocalUserIdentification.current
    val configViewModel = viewModel { TwitchViewModel(m, it, guildViewModel) }

    ResourceChecker(
        i18nContext,
        guildViewModel.guildInfoResource,
        configViewModel.configResource
    ) { guild, twitchResponse ->
        val trackedTwitchAccounts by remember { mutableStateOf(twitchResponse.twitchConfig.trackedTwitchAccounts.toMutableStateList()) }
        val premiumTrackTwitchAccounts by remember { mutableStateOf(twitchResponse.twitchConfig.premiumTrackTwitchAccounts.toMutableStateList()) }

        HeroBanner {
            HeroImage {
                // WelcomerWebAnimation()
            }

            HeroText {
                H1 {
                    Text("Notificações da Twitch")
                }

                P {
                    Text("Anuncie para seus membros quando você entra ao vivo na Twitch! Assim, seus fãs não irão perder as suas lives.")
                }
            }
        }

        Hr {}

        CardsWithHeader {
            CardHeader {
                CardHeaderInfo {
                    CardHeaderTitle {
                        Text("Canais que você está seguindo")
                    }

                    CardHeaderDescription {
                        Text(i18nContext.get(I18nKeysData.Website.Dashboard.Twitch.Channels(trackedTwitchAccounts.size)))
                    }
                }

                DiscordButton(
                    DiscordButtonType.PRIMARY,
                    {
                        if (trackedTwitchAccounts.size >= 100)
                            disabledWithSoundEffect(m)
                        else {
                            onClick {
                                var channelUserLogin by mutableStateOf<String>("")

                                m.globalState.openCloseOnlyModal(
                                    "Qual canal você deseja adicionar?",
                                    true
                                ) {
                                    VerticalList {
                                        DiscordButton(
                                            DiscordButtonType.PRIMARY,
                                            attrs = {
                                                onClick {
                                                    m.globalState.openCloseOnlyModal(
                                                        "Autorizar Conta na Twitch",
                                                        true,
                                                    ) { modal ->
                                                        // This is a hack!!! We use this to know when the modal has been closed
                                                        Span(attrs = {
                                                            window.open("https://id.twitch.tv/oauth2/authorize?client_id=${spicyInfo.twitchClientId}&redirect_uri=${spicyInfo.twitchRedirectUri}&response_type=code")

                                                            val listener = object : EventListener {
                                                                override fun handleEvent(event: Event) {
                                                                    val userId = event.asDynamic().data

                                                                    println("User ID: $userId")

                                                                    // Close the modal and switch!
                                                                    modal.close()
                                                                    m.routingManager.switchBasedOnPath(
                                                                        i18nContext,
                                                                        ScreenPathWithArguments(
                                                                            ScreenPath.AddNewGuildTwitchChannelPath,
                                                                            mapOf("guildId" to screen.guildId.toString()),
                                                                            mapOf("userId" to userId)
                                                                        ).build(),
                                                                        false
                                                                    )
                                                                }
                                                            }

                                                            window.addEventListener(
                                                                "message",
                                                                listener,
                                                                false
                                                            )

                                                            ref {
                                                                onDispose {
                                                                    window.removeEventListener("message", listener)
                                                                }
                                                            }
                                                        })

                                                        Text("Siga as instruções para autorizar a sua conta")
                                                    }
                                                }
                                            }
                                        ) {
                                            Text("Quero adicionar o meu canal")
                                        }

                                        DiscordButton(
                                            DiscordButtonType.PRIMARY,
                                            attrs = {
                                                onClick {
                                                    var isLoading by mutableStateOf(false)

                                                    m.globalState.openModalWithCloseButton(
                                                        "Adicionar canal de outra pessoa",
                                                        true,
                                                        {
                                                            TextInput(channelUserLogin) {
                                                                placeholder("lorittamorenitta")

                                                                onInput {
                                                                    channelUserLogin = it.value
                                                                }
                                                            }
                                                        },
                                                        { modal ->
                                                            if (channelUserLogin.isBlank() || isLoading) {
                                                                DiscordButton(
                                                                    DiscordButtonType.PRIMARY,
                                                                    {
                                                                        disabledWithSoundEffect(m)
                                                                    }
                                                                ) {
                                                                    Text("Continuar")
                                                                }
                                                            } else {
                                                                DiscordButton(
                                                                    DiscordButtonType.PRIMARY,
                                                                    {
                                                                        onClick {
                                                                            screen.launch {
                                                                                isLoading = true
                                                                                m.globalState.showToast(
                                                                                    Toast.Type.INFO,
                                                                                    "Pesquisando canal..."
                                                                                )

                                                                                m.makeGuildScopedRPCRequestWithGenericHandling<DashGuildScopedResponse.CheckExternalGuildTwitchChannelResponse>(
                                                                                    guild.id,
                                                                                    DashGuildScopedRequest.CheckExternalGuildTwitchChannelRequest(
                                                                                        channelUserLogin.removePrefix("https://")
                                                                                            .removePrefix("http://")
                                                                                            .removePrefix("www.")
                                                                                            .removePrefix("twitch.tv/")
                                                                                    ),
                                                                                    onSuccess = { checkResponse ->
                                                                                        isLoading = false

                                                                                        when (checkResponse) {
                                                                                            is DashGuildScopedResponse.CheckExternalGuildTwitchChannelResponse.Success -> {
                                                                                                m.globalState.showToast(
                                                                                                    Toast.Type.SUCCESS,
                                                                                                    "Canal encontrado!"
                                                                                                )
                                                                                                when (checkResponse.trackingState) {
                                                                                                    TwitchAccountTrackState.AUTHORIZED, TwitchAccountTrackState.ALWAYS_TRACK_USER, TwitchAccountTrackState.PREMIUM_TRACK_USER -> {
                                                                                                        modal.close()
                                                                                                        m.routingManager.switchBasedOnPath(
                                                                                                            i18nContext,
                                                                                                            ScreenPathWithArguments(
                                                                                                                ScreenPath.AddNewGuildTwitchChannelPath,
                                                                                                                mapOf("guildId" to screen.guildId.toString()),
                                                                                                                mapOf("userId" to checkResponse.twitchUser!!.id.toString())
                                                                                                            ).build(),
                                                                                                            false
                                                                                                        )
                                                                                                    }

                                                                                                    TwitchAccountTrackState.UNAUTHORIZED -> {
                                                                                                        val plan =
                                                                                                            ServerPremiumPlans.getPlanFromValue(
                                                                                                                twitchResponse.activatedPremiumKeysValue
                                                                                                            )

                                                                                                        if (plan.maxUnauthorizedTwitchChannels > premiumTrackTwitchAccounts.size) {
                                                                                                            m.globalState.openModalWithCloseButton(
                                                                                                                "Conta não autorizada, mas...",
                                                                                                                true,
                                                                                                                {
                                                                                                                    P {
                                                                                                                        Text(
                                                                                                                            "A conta que você deseja adicionar não está autorizada na Loritta, mas você tem plano premium!"
                                                                                                                        )
                                                                                                                    }

                                                                                                                    P {
                                                                                                                        Text(
                                                                                                                            "Você pode seguir até ${plan.maxUnauthorizedTwitchChannels} contas que não foram autorizadas. Ao autorizar uma conta, outras pessoas podem seguir a conta sem precisar de plano premium, até você remover a conta da sua lista de acompanhamentos premium."
                                                                                                                        )
                                                                                                                    }
                                                                                                                },
                                                                                                                { modal ->
                                                                                                                    DiscordButton(
                                                                                                                        DiscordButtonType.PRIMARY,
                                                                                                                        attrs = {
                                                                                                                            onClick {
                                                                                                                                modal.close()

                                                                                                                                m.routingManager.switchBasedOnPath(
                                                                                                                                    i18nContext,
                                                                                                                                    ScreenPathWithArguments(
                                                                                                                                        ScreenPath.AddNewGuildTwitchChannelPath,
                                                                                                                                        mapOf(
                                                                                                                                            "guildId" to screen.guildId.toString()
                                                                                                                                        ),
                                                                                                                                        mapOf(
                                                                                                                                            "userId" to checkResponse.twitchUser!!.id.toString(),
                                                                                                                                            "createPremiumTrack" to "true"
                                                                                                                                        )
                                                                                                                                    ).build(),
                                                                                                                                    false
                                                                                                                                )
                                                                                                                            }
                                                                                                                        }
                                                                                                                    ) {
                                                                                                                        Text(
                                                                                                                            "Acompanhar de Forma Premium"
                                                                                                                        )
                                                                                                                    }
                                                                                                                }
                                                                                                            )
                                                                                                        } else {
                                                                                                            m.globalState.openCloseOnlyModal(
                                                                                                                "Conta não autorizada",
                                                                                                                true
                                                                                                            ) { modal ->
                                                                                                                P {
                                                                                                                    Text(
                                                                                                                        "A conta que você deseja adicionar não está autorizada na Loritta!"
                                                                                                                    )
                                                                                                                }

                                                                                                                P {
                                                                                                                    Text(
                                                                                                                        "Devido a limitações da Twitch, cada solicitação de notificação de livestream custa pontos, exceto se o dono da conta autorizar. Se fosse possível adicionar qualquer conta sem autorização, nós iriamos chegar no limite de solicitações rapidinho, assim não seria possível adicionar novas contas no painel..."
                                                                                                                    )
                                                                                                                }
                                                                                                                P {
                                                                                                                    Text(
                                                                                                                        "Peça para o dono da conta autorizar a conta dela na Loritta, ou compre plano premium na Loritta para poder adicionar contas não autorizadas!"
                                                                                                                    )
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                            }

                                                                                            DashGuildScopedResponse.CheckExternalGuildTwitchChannelResponse.UserNotFound -> {
                                                                                                m.globalState.showToast(
                                                                                                    Toast.Type.WARN,
                                                                                                    "Canal não existe!"
                                                                                                )
                                                                                            }
                                                                                        }
                                                                                    },
                                                                                    onError = {
                                                                                        isLoading = false
                                                                                    }
                                                                                )
                                                                            }
                                                                            // modal.close()
                                                                        }
                                                                    }
                                                                ) {
                                                                    Text("Continuar")
                                                                }
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        ) {
                                            Text("Quero adicionar o canal de outra pessoa")
                                        }
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text("Adicionar Canal")
                }
            }

            if (trackedTwitchAccounts.isNotEmpty()) {
                Cards {
                    for (trackedTwitchAccount in trackedTwitchAccounts) {
                        Card(attrs = {
                            attr("style", "flex-direction: row; align-items: center; gap: 0.5em;")
                        }) {
                            Img(src = trackedTwitchAccount.twitchUser?.profileImageUrl ?: "") {
                                attr("style", "width: 64px; height: 64px; border-radius: 100%;")
                            }

                            Div(attrs = {
                                attr("style", "flex-grow: 1; display: flex; flex-direction: column; word-break: break-word;")
                            }) {
                                Div {
                                    Text("${trackedTwitchAccount.twitchUser?.displayName} (${trackedTwitchAccount.twitchUser?.login})")
                                }

                                Div(attrs = {
                                    attr("style", "font-size: 0.8em; display: flex; flex-direction: row; gap: 0.25em; align-items: center;")
                                }) {
                                    when (trackedTwitchAccount.trackingState) {
                                        TwitchAccountTrackState.AUTHORIZED -> {
                                            Div(attrs = {
                                                classes("state-tip", "success")
                                            }) {
                                                UIIcon(SVGIconManager.check)
                                            }

                                            Text("Notificações ativadas — Canal autorizado pelo dono")
                                        }
                                        TwitchAccountTrackState.ALWAYS_TRACK_USER -> {
                                            Div(attrs = {
                                                classes("state-tip", "success")
                                            }) {
                                                UIIcon(SVGIconManager.check)
                                            }


                                            Text("Notificações ativadas — Canal famoso")
                                        }
                                        TwitchAccountTrackState.PREMIUM_TRACK_USER -> {
                                            Div(attrs = {
                                                classes("state-tip", "success")
                                            }) {
                                                UIIcon(SVGIconManager.check)
                                            }

                                            Text("Notificações ativadas — Canal usando Acompanhamento Premium")
                                        }
                                        TwitchAccountTrackState.UNAUTHORIZED -> {
                                            Div(attrs = {
                                                classes("state-tip", "warn")
                                            }) {
                                                UIIcon(SVGIconManager.xmark)
                                            }

                                            Text("Notificações desativadas — Canal não autorizado")
                                        }
                                    }
                                }
                            }

                            Div(attrs = {
                                attr("style", "display: grid;grid-template-columns: 1fr 1fr;grid-column-gap: 0.5em;")
                            }) {
                                Div {
                                    DiscordButton(
                                        DiscordButtonType.DANGER,
                                        attrs = {
                                            onClick {
                                                m.globalState.openModalWithCloseButton(
                                                    "Você tem certeza?",
                                                    true,
                                                    {
                                                        Text("Você quer deletar meeeesmo?")
                                                    },
                                                    { modal ->
                                                        DiscordButton(
                                                            DiscordButtonType.DANGER,
                                                            attrs = {
                                                                onClick {
                                                                    GlobalScope.launch {
                                                                        m.globalState.showToast(
                                                                            Toast.Type.INFO,
                                                                            "Deletando canal..."
                                                                        )
                                                                        // val config = WelcomerViewModel.toDataConfig(mutableWelcomerConfig)
                                                                        m.makeGuildScopedRPCRequestWithGenericHandling<DashGuildScopedResponse.DeleteGuildTwitchChannelResponse>(
                                                                            guild.id,
                                                                            DashGuildScopedRequest.DeleteGuildTwitchChannelRequest(
                                                                                trackedTwitchAccount.trackedInfo.id
                                                                            ),
                                                                            onSuccess = {
                                                                                trackedTwitchAccounts.remove(
                                                                                    trackedTwitchAccount
                                                                                )

                                                                                modal.close()
                                                                                m.globalState.showToast(
                                                                                    Toast.Type.SUCCESS,
                                                                                    "Canal deletado!"
                                                                                )
                                                                                m.soundEffects.configSaved.play(1.0)
                                                                            },
                                                                            onError = {
                                                                                m.soundEffects.configError.play(1.0)
                                                                            }
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        ) {
                                                            Text("Excluir")
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    ) {
                                        Text("Excluir")
                                    }
                                }

                                Div {
                                    AScreen(
                                        m,
                                        screenPath = ScreenPathWithArguments(
                                            ScreenPath.EditGuildTwitchChannelPath,
                                            mapOf(
                                                "guildId" to screen.guildId.toString(),
                                                "trackedId" to trackedTwitchAccount.trackedInfo.id.toString()
                                            ),
                                            mapOf()
                                        ),
                                    ) {
                                        DiscordButton(DiscordButtonType.PRIMARY) {
                                            Text("Editar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                EmptySection(i18nContext)
            }
        }

        Hr {}

        Div {
            HeroBanner {
                HeroImage {
                    // WelcomerWebAnimation()
                }

                HeroText {
                    H2 {
                        Text("Acompanhamentos Premium")
                    }

                    P {
                        Text("Servidores premium podem seguir contas que não foram autorizadas na Loritta. Aqui, você encontrará todas as contas com o recurso de acompanhamento premium ativado!")
                    }
                }
            }

            Hr {}

            CardsWithHeader {
                CardHeader {
                    CardHeaderInfo {
                        CardHeaderTitle {
                            Text("Canais com Acompanhamento Premium")
                        }

                        CardHeaderDescription {
                            Text(i18nContext.get(I18nKeysData.Website.Dashboard.Twitch.Channels(premiumTrackTwitchAccounts.size)))
                        }
                    }
                }

                if (premiumTrackTwitchAccounts.isNotEmpty()) {
                    Cards {
                        for (premiumTrackTwitchAccount in premiumTrackTwitchAccounts) {
                            Card(attrs = {
                                attr("style", "flex-direction: row; align-items: center; gap: 0.5em;")
                            }) {
                                Img(src = premiumTrackTwitchAccount.twitchUser?.profileImageUrl ?: "") {
                                    attr("style", "width: 64px; height: 64px; border-radius: 100%;")
                                }

                                Div(attrs = {
                                    attr("style", "flex-grow: 1; word-break: break-word;")
                                }) {
                                    Text("${premiumTrackTwitchAccount.twitchUser?.displayName} (${premiumTrackTwitchAccount.twitchUser?.login})")
                                }

                                Div(attrs = {
                                    attr(
                                        "style",
                                        "display: grid;grid-template-columns: 1fr;grid-column-gap: 0.5em;"
                                    )
                                }) {
                                    Div {
                                        DiscordButton(
                                            DiscordButtonType.DANGER,
                                            attrs = {
                                                onClick {
                                                    m.globalState.openModalWithCloseButton(
                                                        "Você tem certeza?",
                                                        true,
                                                        {
                                                            Text("Você quer deletar meeeesmo?")
                                                        },
                                                        { modal ->
                                                            DiscordButton(
                                                                DiscordButtonType.DANGER,
                                                                attrs = {
                                                                    onClick {
                                                                        GlobalScope.launch {
                                                                            m.globalState.showToast(
                                                                                Toast.Type.INFO,
                                                                                "Deletando acompanhamento premium..."
                                                                            )
                                                                            // val config = WelcomerViewModel.toDataConfig(mutableWelcomerConfig)
                                                                            m.makeGuildScopedRPCRequestWithGenericHandling<DashGuildScopedResponse.DisablePremiumTrackForTwitchChannelResponse>(
                                                                                guild.id,
                                                                                DashGuildScopedRequest.DisablePremiumTrackForTwitchChannelRequest(
                                                                                    premiumTrackTwitchAccount.trackedInfo.id
                                                                                ),
                                                                                onSuccess = {
                                                                                    premiumTrackTwitchAccounts.remove(
                                                                                        premiumTrackTwitchAccount
                                                                                    )
                                                                                    modal.close()
                                                                                    m.globalState.showToast(
                                                                                        Toast.Type.SUCCESS,
                                                                                        "Acompanhamento premium deletado!"
                                                                                    )
                                                                                    m.soundEffects.configSaved.play(
                                                                                        1.0
                                                                                    )
                                                                                },
                                                                                onError = {
                                                                                    m.soundEffects.configError.play(
                                                                                        1.0
                                                                                    )
                                                                                }
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            ) {
                                                                Text("Excluir")
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        ) {
                                            Text("Excluir")
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    EmptySection(i18nContext)
                }
            }
        }
    }
}