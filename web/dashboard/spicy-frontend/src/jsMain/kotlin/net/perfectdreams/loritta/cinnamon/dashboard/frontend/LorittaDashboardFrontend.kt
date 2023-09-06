package net.perfectdreams.loritta.cinnamon.dashboard.frontend

import androidx.compose.runtime.*
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.dashboard.common.embeds.DiscordEmbed
import net.perfectdreams.loritta.cinnamon.dashboard.common.embeds.DiscordMessage
import net.perfectdreams.loritta.cinnamon.dashboard.common.requests.LorittaRequest
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.LorittaResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.*
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.FieldWrapper
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.FieldWrappers
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.GuildLeftSidebar
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.ResourceChecker
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.UserLeftSidebar
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.UserRightSidebar
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.gamersaferverify.GamerSaferVerify
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.guilds.ChooseAServerOverview
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.shipeffects.ShipEffectsOverview
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.sonhosshop.SonhosShopOverview
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.game.GameState
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.*
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.*
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.discordcdn.DiscordCdn
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.discordcdn.Image
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.GuildViewModel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.WelcomerViewModel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.viewModel
import net.perfectdreams.loritta.cinnamon.dashboard.utils.pixi.Application
import net.perfectdreams.loritta.common.utils.Placeholders
import net.perfectdreams.loritta.serializable.TextDiscordChannel
import net.perfectdreams.loritta.serializable.dashboard.requests.LorittaDashboardRPCRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextArea
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.*

class LorittaDashboardFrontend(private val app: Application) {
    companion object {
        private val logger = KotlinLogging.loggerClassName(LorittaDashboardFrontend::class)
    }

    val routingManager = RoutingManager(this)
    val globalState = GlobalState(this)

    val http = HttpClient(Js) {
        expectSuccess = false
    }
    val spaLoadingWrapper by lazy { document.getElementById("spa-loading-wrapper") as HTMLDivElement? }
    val gameState = GameState(app)

    val configSavedSfx: Audio by lazy { Audio("${window.location.origin}/assets/snd/config-saved.ogg") }
    val configErrorSfx: Audio by lazy { Audio("${window.location.origin}/assets/snd/config-error.ogg") }

    private fun appendGameOverlay() {
        app.view.addClass("loritta-game-canvas")
        document.body!!.appendChild(app.view)
    }

    fun start() {
        logger.info { "Howdy from Kotlin ${KotlinVersion.CURRENT}! :3" }

        NitroPayUtils.prepareNitroPayState()

        appendGameOverlay()

        globalState.launch {
            globalState.launch { globalState.updateSelfUserInfo() }
            globalState.launch { globalState.updateSpicyInfo() }

            // We need to get it in this way, because we want to get the i18nContext for the routing manager
            val i18nContext = globalState.retrieveI18nContext()
            globalState.i18nContext = Resource.Success(i18nContext)

            // Switch based on the path
            routingManager.switchBasedOnPath(i18nContext, "/${window.location.pathname.split("/").drop(2).joinToString("/")}", false)

            window.onpopstate = {
                // TODO: We need to get the current i18nContext state from the globalState
                routingManager.switchBasedOnPath(i18nContext, "/${(it.state as String).split("/").drop(2).joinToString("/")}", true)
            }

            runOnDOMLoaded {
                logger.info { "DOM has been loaded! Mounting Jetpack Compose Web..." }

                renderApp()
            }
        }
    }

    fun renderApp() {
        renderComposable(rootElementId = "root") {
            val userInfo = globalState.userInfo
            val spicyInfo = globalState.spicyInfo
            val i18nContext = globalState.i18nContext

            if (userInfo !is Resource.Success || i18nContext !is Resource.Success || spicyInfo !is Resource.Success) {
                Text("Loading...")
            } else {
                CompositionLocalProvider(LocalI18nContext provides i18nContext.value) {
                    CompositionLocalProvider(LocalUserIdentification provides userInfo.value) {
                        CompositionLocalProvider(LocalSpicyInfo provides spicyInfo.value) {
                            // Fade out the single page application loading wrapper...
                            spaLoadingWrapper?.addClass("loaded")

                            Div(attrs = { id("wrapper") }) {
                                // Wrapped in a div to only trigger a recomposition within this div when a modal is updated
                                Div {
                                    val activeModal = globalState.activeModal

                                    if (activeModal != null) {
                                        // Open modal if there is one present
                                        Div(attrs = {
                                            classes("modal-wrapper")

                                            onClick {
                                                // Close modal when clicking outside of the screen
                                                if (it.target == it.currentTarget)
                                                    globalState.activeModal = null
                                            }
                                        }) {
                                            Div(attrs = {
                                                classes("modal")
                                            }) {
                                                Div(attrs = { classes("content") }) {
                                                    Div(attrs = { classes("title") }) {
                                                        Text(activeModal.title)
                                                    }

                                                    activeModal.body.invoke()
                                                }

                                                Div(attrs = { classes("buttons-wrapper") }) {
                                                    activeModal.buttons.forEach {
                                                        it.invoke()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                val screen = routingManager.screenState

                                when (screen) {
                                    is UserScreen -> {
                                        UserLeftSidebar(this@LorittaDashboardFrontend)

                                        UserRightSidebar(this@LorittaDashboardFrontend) {
                                            when (screen) {
                                                is ChooseAServerScreen -> {
                                                    ChooseAServerOverview(
                                                        this@LorittaDashboardFrontend,
                                                        screen,
                                                        i18nContext.value
                                                    )
                                                }
                                                is ShipEffectsScreen -> {
                                                    ShipEffectsOverview(
                                                        this@LorittaDashboardFrontend,
                                                        screen,
                                                        i18nContext.value
                                                    )
                                                }
                                                is SonhosShopScreen -> {
                                                    SonhosShopOverview(
                                                        this@LorittaDashboardFrontend,
                                                        screen,
                                                        i18nContext.value
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    is GuildScreen -> {
                                        // Always recompose if it is a new guild ID, to force the guild data to be reloaded
                                        key(screen.guildId) {
                                            val vm = viewModel { GuildViewModel(this@LorittaDashboardFrontend, it, screen.guildId) }
                                            val guildInfoResource = vm.guildInfoResource

                                            val guild = (guildInfoResource as? Resource.Success)?.value

                                            GuildLeftSidebar(this@LorittaDashboardFrontend, screen, guild)

                                            UserRightSidebar(this@LorittaDashboardFrontend) {
                                                when (screen) {
                                                    is ConfigureGuildGamerSaferVerifyScreen -> {
                                                        GamerSaferVerify(
                                                            this@LorittaDashboardFrontend,
                                                            screen,
                                                            i18nContext.value,
                                                            vm
                                                        )
                                                    }

                                                    is ConfigureGuildWelcomerScreen -> {
                                                        val configViewModel = viewModel { WelcomerViewModel(this@LorittaDashboardFrontend, it, vm) }

                                                        ResourceChecker(i18nContext.value, vm.guildInfoResource, configViewModel.configResource) { guild, welcomerResponse ->
                                                            Text("hell yeah!!! ")

                                                            val welcomerConfig = welcomerResponse.welcomerConfig
                                                            if (welcomerConfig == null) {
                                                                Text("No welcomer config set")
                                                            } else {
                                                                Text("${welcomerConfig}")
                                                            }

                                                            if (welcomerConfig != null) {
                                                                val mutableWelcomerConfig by remember { mutableStateOf(WelcomerViewModel.toMutableConfig(welcomerConfig)) }

                                                                Hr {}

                                                                ToggleableSection(
                                                                    "tell-on-join-section",
                                                                    "Ativar as mensagens quando alguém entrar",
                                                                    "-",
                                                                    mutableWelcomerConfig._tellOnJoin,
                                                                ) {
                                                                    FieldWrappers {
                                                                        FieldWrapper {
                                                                            FieldLabel("Canal onde será enviado as mensagens")

                                                                            SelectMenu(
                                                                                guild.channels.filterIsInstance<TextDiscordChannel>()
                                                                                    .map {
                                                                                        SelectMenuEntry(
                                                                                            {
                                                                                                Text("#")
                                                                                                Text(it.name)
                                                                                            },
                                                                                            it.id == mutableWelcomerConfig.channelJoinId,
                                                                                            {
                                                                                                mutableWelcomerConfig.channelJoinId =
                                                                                                    it.id
                                                                                            },
                                                                                            {}
                                                                                        )
                                                                                    }
                                                                            )
                                                                        }

                                                                        FieldWrapper {
                                                                            FieldLabel("Mensagem quando alguém entrar")

                                                                            val templates = mapOf(
                                                                                "Padrão" to "\uD83D\uDC49 {@user} entrou no servidor!",
                                                                                "Padrão, só que melhor" to "<a:lori_happy:521721811298156558> | {@user} acabou de entrar na {guild}! Agora temos {guild-size} membros!",
                                                                                "Lista de Informações" to """{@user}, bem-vindo(a) ao {guild}! <a:lori_happy:521721811298156558>
• Leia as #regras *(psiu, troque o nome do canal aqui na mensagem!)* para você poder conviver em harmonia! <:blobheart:467447056374693889>
• Converse no canal de #bate-papo <:lori_pac:503600573741006863>
• E é claro, divirta-se! <a:emojo_feriado:393751205308006421>

Aliás, continue sendo incrível! (E eu sou muito fofa! :3)""",
                                                                                "Embed Simples" to """{
   "content":"{@user}",
   "embed":{
      "color":-9270822,
      "title":"👋Seja bem-vindo(a)!",
      "description":"Olá {@user}! Seja bem-vindo(a) ao {guild}!"
   }
}""",
                                                                                "Embed com Avatar" to """{
   "content":"{@user}",
   "embed":{
      "color":-9270822,
      "title":"👋Bem-vindo(a)!",
      "description":"Olá {@user}, espero que você se divirta no meu servidor! <:loritta:331179879582269451>",
      "author":{
         "name":"{user}#{user-discriminator}",
         "icon_url":"{user-avatar-url}"
      },
      "thumbnail":{
         "url":"{user-avatar-url}"
      },
    "footer": {
      "text": "ID do usuário: {user-id}"
    }
   }
}""",
                                                                                "Embed com Avatar e Imagem" to """{
   "content":"{@user}",
   "embed":{
      "color":-9270822,
      "title":"👋Bem-vindo(a)!",
      "description":"Olá {@user}, espero que você se divirta no meu servidor! <:loritta:331179879582269451>",
      "author":{
         "name":"{user}#{user-discriminator}",
         "icon_url":"{user-avatar-url}"
      },
      "thumbnail":{
         "url":"{user-avatar-url}"
      },
	  "image": {
	     "url": "https://media.giphy.com/media/GPQBFuG4ABACA/source.gif"
	  },
    "footer": {
      "text": "ID do usuário: {user-id}"
    }
   }
}""",
                                                                                "Embed com Informações" to """{
   "content":"{@user}",
   "embed":{
      "color":-14689638 ,
      "title":"{user}#{user-discriminator} | Bem-vindo(a)!",
      "thumbnail": {
         "url" : "{user-avatar-url}"
      },
      "description":"<:lori_hug:515328576611155968> Olá, seja bem-vindo(a) ao {guild}!",
      "fields": [
        {
            "name": "👋 Sabia que...",
            "value": "Você é o {guild-size}º membro aqui no servidor?",
            "inline": true
        },
        {
            "name": "🛡 Tag do Usuário",
            "value": "`{user}#{user-discriminator}` ({user-id})",
            "inline": true
        },
        {
            "name": "📛 Precisando de ajuda?",
            "value": "Caso você tenha alguma dúvida ou problema, chame a nossa equipe!",
            "inline": true
        },
        {
            "name": "👮 Evite punições!",
            "value": "Leia as nossas #regras para evitar ser punido no servidor!",
            "inline": true
        }
      ],
    "footer": {
      "text": "{guild} • © Todos os direitos reservados."
    }
   }
}""",
                                                                                "Kit Social Influencer™" to """{
   "content":"{@user}",
   "embed":{
      "color":-2342853,
      "title":"{user}#{user-discriminator} | Bem-vindo(a)!",
      "thumbnail": {
         "url" : "{user-avatar-url}"
      },
      "description":"Salve {@user}! Você acabou de entrar no servidor do {guild}, aqui você poderá se interagir com fãs do {guild}, conversar sobre suas coisas favoritas e muito mais!",
      "fields": [
        {
            "name": "📢 Fique atento!",
            "value": "Novos vídeos do {guild} serão anunciados no #vídeos-novos!",
            "inline": true
        },
        {
            "name": "📺 Inscreva-se no canal se você ainda não é inscrito! ",
            "value": "[Canal](https://www.youtube.com/channel/UC-eeXSRZ8cO-i2NZYrWGDnQ)",
            "inline": true
        },
        {
            "name": "🐦 Siga no Twitter! ",
            "value": "[@LorittaBot](https://twitter.com/LorittaBot)",
            "inline": true
        },
        {
            "name": "🖼 Siga no Instagram! ",
            "value": "[@lorittabot](https://instagram.com/lorittabot)",
            "inline": true
        }
      ],
    "footer": {
      "text": "Eu sou muito fofa e o {guild} também :3"
    }
   }
}"""
                                                                            )

                                                                            Div {
                                                                                DiscordButton(
                                                                                    DiscordButtonType.PRIMARY,
                                                                                    attrs = {
                                                                                        onClick {
                                                                                            globalState.openCloseOnlyModal(
                                                                                                "Templates de Mensagens"
                                                                                            ) {
                                                                                                Text("Sem criatividade? Então pegue um template!")

                                                                                                Div(attrs = {
                                                                                                    style {
                                                                                                        display(
                                                                                                            DisplayStyle.Flex
                                                                                                        )
                                                                                                        flexDirection(
                                                                                                            FlexDirection.Column
                                                                                                        )
                                                                                                        gap(0.5.em)
                                                                                                    }
                                                                                                }) {
                                                                                                    for (placeholder in templates) {
                                                                                                        DiscordButton(
                                                                                                            DiscordButtonType.PRIMARY,
                                                                                                            attrs = {
                                                                                                                onClick {
                                                                                                                    globalState.openModal(
                                                                                                                        "Você realmente quer substituir?",
                                                                                                                        {
                                                                                                                            Text(
                                                                                                                                "Ao aplicar o template, a sua mensagem atual será perdida! A não ser se você tenha copiado ela para outro lugar, aí vida que segue né."
                                                                                                                            )
                                                                                                                        },
                                                                                                                        {
                                                                                                                            CloseModalButton(
                                                                                                                                globalState
                                                                                                                            )
                                                                                                                        },
                                                                                                                        {
                                                                                                                            DiscordButton(
                                                                                                                                DiscordButtonType.PRIMARY,
                                                                                                                                attrs = {
                                                                                                                                    onClick {
                                                                                                                                        mutableWelcomerConfig.joinMessage =
                                                                                                                                            placeholder.value
                                                                                                                                        globalState.activeModal =
                                                                                                                                            null
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            ) {
                                                                                                                                Text(
                                                                                                                                    "Aplicar"
                                                                                                                                )
                                                                                                                            }
                                                                                                                        }
                                                                                                                    )
                                                                                                                }
                                                                                                            }
                                                                                                        ) {
                                                                                                            Text(
                                                                                                                placeholder.key
                                                                                                            )
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                ) {
                                                                                    Text("Template de Mensagens")
                                                                                }
                                                                            }

                                                                            val joinMessage = mutableWelcomerConfig.joinMessage

                                                                            Div {
                                                                                TextArea(joinMessage) {
                                                                                    onInput {
                                                                                        println("Typing... ${it.value}")
                                                                                        mutableWelcomerConfig.joinMessage = it.value
                                                                                    }
                                                                                }
                                                                            }

                                                                            val avatarId = userInfo.value.avatarId

                                                                            val avatarUrl = if (avatarId != null) {
                                                                                DiscordCdn.userAvatar(userInfo.value.id.value, avatarId)
                                                                                    .toUrl()
                                                                            } else {
                                                                                DiscordCdn.defaultAvatar(userInfo.value.id.value)
                                                                                    .toUrl {
                                                                                        format = Image.Format.PNG // For some weird reason, the default avatars aren't available in webp format (why?)
                                                                                    }
                                                                            }

                                                                            val placeholders = listOf(
                                                                                MessagePlaceholder(
                                                                                    Placeholders.USER_MENTION,
                                                                                    "@${userInfo.value.username}",
                                                                                    "", // locale["$placeholdersUserPrefix.mention"],
                                                                                    MessagePlaceholder.RenderType.MENTION,
                                                                                    false
                                                                                ),
                                                                                MessagePlaceholder(
                                                                                    Placeholders.USER_NAME_SHORT,
                                                                                    userInfo.value.username,
                                                                                    "", // locale["$placeholdersUserPrefix.name"],
                                                                                    MessagePlaceholder.RenderType.TEXT,
                                                                                    false
                                                                                ),
                                                                                MessagePlaceholder(
                                                                                    Placeholders.USER_NAME,
                                                                                    userInfo.value.username,
                                                                                    "", // locale["$placeholdersUserPrefix.name"],
                                                                                    MessagePlaceholder.RenderType.TEXT,
                                                                                    false
                                                                                ),
                                                                                MessagePlaceholder(
                                                                                    Placeholders.USER_DISCRIMINATOR,
                                                                                    userInfo.value.discriminator,
                                                                                    "", // locale["$placeholdersUserPrefix.discriminator"],
                                                                                    MessagePlaceholder.RenderType.TEXT,
                                                                                    false
                                                                                ),
                                                                                MessagePlaceholder(
                                                                                    Placeholders.USER_TAG,
                                                                                    "@${userInfo.value.username}",
                                                                                    "", // locale["$placeholdersUserPrefix.tag"],
                                                                                    MessagePlaceholder.RenderType.TEXT,
                                                                                    false
                                                                                ),
                                                                                MessagePlaceholder(
                                                                                    Placeholders.USER_ID,
                                                                                    userInfo.value.id.value.toString(),
                                                                                    "", // locale["$placeholdersUserPrefix.id"],
                                                                                    MessagePlaceholder.RenderType.TEXT,
                                                                                    false
                                                                                ),
                                                                                MessagePlaceholder(
                                                                                    Placeholders.USER_AVATAR_URL,
                                                                                    avatarUrl,
                                                                                    "", // locale["$placeholdersUserPrefix.avatarUrl"],
                                                                                    MessagePlaceholder.RenderType.TEXT,
                                                                                    false
                                                                                ),
                                                                                MessagePlaceholder(
                                                                                    Placeholders.USER_NICKNAME,
                                                                                    userInfo.value.username,
                                                                                    "", // locale["$placeholdersUserPrefix.nickname"],
                                                                                    MessagePlaceholder.RenderType.TEXT,
                                                                                    false
                                                                                ),

                                                                                // === [ DEPRECATED ] ===
                                                                                MessagePlaceholder(
                                                                                    Placeholders.Deprecated.USER_ID, // Deprecated
                                                                                    userInfo.value.id.value.toString(),
                                                                                    null,
                                                                                    MessagePlaceholder.RenderType.TEXT,
                                                                                    true
                                                                                ),
                                                                                MessagePlaceholder(
                                                                                    Placeholders.Deprecated.USER_DISCRIMINATOR, // Deprecated
                                                                                    userInfo.value.discriminator,
                                                                                    null,
                                                                                    MessagePlaceholder.RenderType.TEXT,
                                                                                    true
                                                                                ),
                                                                                MessagePlaceholder(
                                                                                    Placeholders.Deprecated.USER_NICKNAME, // Deprecated
                                                                                    userInfo.value.username,
                                                                                    null,
                                                                                    MessagePlaceholder.RenderType.TEXT,
                                                                                    true
                                                                                ),
                                                                                MessagePlaceholder(
                                                                                    Placeholders.Deprecated.USER_AVATAR_URL,
                                                                                    avatarUrl,
                                                                                    null,
                                                                                    MessagePlaceholder.RenderType.TEXT,
                                                                                    true
                                                                                )
                                                                            )

                                                                            // I don't know why we need to use the key here to force it to recompose...
                                                                            key(mutableWelcomerConfig.joinMessage) {
                                                                                if (joinMessage != null) {
                                                                                    val parsedMessage = try {
                                                                                        Json.decodeFromString<DiscordMessage>(
                                                                                            joinMessage
                                                                                        )
                                                                                    } catch (e: SerializationException) {
                                                                                        null
                                                                                    }

                                                                                    if (parsedMessage != null) {
                                                                                        Div {
                                                                                            DiscordMessageRenderer(
                                                                                                welcomerResponse.selfUser.copy(
                                                                                                    name = "Loritta Morenitta \uD83D\uDE18"
                                                                                                ),
                                                                                                parsedMessage,
                                                                                                placeholders
                                                                                            )
                                                                                        }
                                                                                    } else {
                                                                                        // Before we render the message as a normal message, we will check if the user *tried* to do a JSON message
                                                                                        if (joinMessage.startsWith("{")) {
                                                                                            Div {
                                                                                                Text("Você tentou fazer uma mensagem em JSON? Se sim, tem algo errado nela!")
                                                                                            }

                                                                                            // Now we will check that MAYBE this is a "carl.gg" embed? (as in: the embed without the message object)
                                                                                            val embed = try {
                                                                                                Json.decodeFromString<DiscordEmbed>(joinMessage)
                                                                                            } catch (e: SerializationException) { null }

                                                                                            if (embed != null) {
                                                                                                Div {
                                                                                                    Text("Isso parece ser uma embed do carl.gg (raw embed json) seu safado sem vergonha ")

                                                                                                    DiscordButton(
                                                                                                        DiscordButtonType.PRIMARY,
                                                                                                        attrs = {
                                                                                                            onClick {
                                                                                                                mutableWelcomerConfig.joinMessage =
                                                                                                                    Json.encodeToString(
                                                                                                                        DiscordMessage(
                                                                                                                            "",
                                                                                                                            embed
                                                                                                                        )
                                                                                                                    )
                                                                                                            }
                                                                                                        }
                                                                                                    ) {
                                                                                                        Text("Desculpe Lori só faça ela funcionar pfv")
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }

                                                                                        // If the message couldn't be parsed, render it as a normal message
                                                                                        Div {
                                                                                            DiscordMessageRenderer(
                                                                                                welcomerResponse.selfUser.copy(
                                                                                                    name = "Loritta Morenitta \uD83D\uDE18"
                                                                                                ),
                                                                                                DiscordMessage(
                                                                                                    content = joinMessage
                                                                                                ),
                                                                                                placeholders
                                                                                            )
                                                                                        }
                                                                                    }
                                                                                } else {
                                                                                    Text("Null join message!")
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                DiscordToggle(
                                                                    "tell-on-join",
                                                                    "Ativar as mensagens quando alguém entrar",
                                                                    stateValue = mutableWelcomerConfig._tellOnJoin
                                                                )

                                                                Hr {}

                                                                DiscordToggle(
                                                                    "tell-on-remove",
                                                                    "Ativar as mensagens quando alguém sair",
                                                                    stateValue = mutableWelcomerConfig._tellOnRemove
                                                                )

                                                                Hr {}

                                                                DiscordToggle(
                                                                    "tell-on-ban",
                                                                    "Mostrar mensagem diferenciada ao ser banido",
                                                                    stateValue = mutableWelcomerConfig._tellOnBan
                                                                )

                                                                Hr {}

                                                                DiscordToggle(
                                                                    "tell-on-dm",
                                                                    "Ativar as mensagens enviadas nas mensagens diretas do usuário quando alguém entrar",
                                                                    "Útil caso você queria mostrar informações básicas sobre o servidor para um usuário mas não quer que fique cheio de mensagens inúteis toda hora que alguém entra.",
                                                                    stateValue = mutableWelcomerConfig._tellOnPrivateJoin
                                                                )

                                                                Hr {}

                                                                Text(mutableWelcomerConfig.tellOnRemove.toString())
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    else -> {
                                        Text("I don't know how to handle screen $screen")
                                        if (screen != null)
                                            Text(" (${screen::class})")
                                        Text("!")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // https://stackoverflow.com/a/59220393/7271796
    private fun runOnDOMLoaded(block: () -> (Unit)) {
        logger.info { "Current document readyState is ${document.readyState}" }
        if (document.readyState == DocumentReadyState.INTERACTIVE || document.readyState == DocumentReadyState.COMPLETE) {
            // already fired, so run logic right away
            block.invoke()
        } else {
            // not fired yet, so let's listen for the event
            window.addEventListener("DOMContentLoaded", { block.invoke() })
        }
    }

    suspend fun makeApiRequest(method: HttpMethod, path: String): LorittaResponse {
        val body = http.request("${window.location.origin}$path") {
            this.method = method
        }.bodyAsText()
        return Json.decodeFromString(body)
    }

    suspend inline fun <reified T : LorittaDashboardRPCResponse> makeRPCRequest(request: LorittaDashboardRPCRequest): T {
        val body = http.post("${window.location.origin}/api/v1/rpc") {
            setBody(
                Json.encodeToString<LorittaDashboardRPCRequest>(
                    request
                )
            )
        }.bodyAsText()
        return Json.decodeFromString(body)
    }

    suspend inline fun <reified T : LorittaDashboardRPCResponse> makeRPCRequestAndUpdateState(resource: MutableState<Resource<T>>, request: LorittaDashboardRPCRequest) = makeRPCRequestAndUpdateStateCheckType<T, T>(
        resource,
        request
    )

    suspend inline fun <reified D : LorittaDashboardRPCResponse, reified T : LorittaDashboardRPCResponse> makeRPCRequestAndUpdateStateCheckType(resource: MutableState<Resource<T>>, request: LorittaDashboardRPCRequest) {
        resource.value = Resource.Loading()
        val response = try {
            makeRPCRequest<D>(request)
        } catch (e: Exception) {
            resource.value = Resource.Failure(e)
            return
        }

        if (response !is T) {
            resource.value = Resource.Failure(RuntimeException("Deserialized $response does not match the type!"))
        } else {
            resource.value = Resource.Success(response)
        }
    }

    suspend fun putLorittaRequest(path: String, request: LorittaRequest): LorittaResponse {
        val body = http.put("${window.location.origin}$path") {
            setJsonBody(request)
        }.bodyAsText()
        return Json.decodeFromString(body)
    }

    suspend fun postLorittaRequest(path: String, request: LorittaRequest): LorittaResponse {
        val body = http.post("${window.location.origin}$path") {
            setJsonBody(request)
        }.bodyAsText()
        return Json.decodeFromString(body)
    }

    suspend inline fun <reified T : LorittaResponse> makeApiRequestAndUpdateState(resource: MutableState<Resource<T>>, method: HttpMethod, path: String) {
        resource.value = Resource.Loading()
        val response = makeApiRequest(method, path)
        if (response is T)
            resource.value = Resource.Success(response)
        else
            resource.value = Resource.Failure(null)
    }
}