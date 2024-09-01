package net.perfectdreams.loritta.morenitta.website.views.dashboard.guild

import kotlinx.html.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.*
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.common.utils.placeholders.BlueskyPostMessagePlaceholders
import net.perfectdreams.loritta.common.utils.placeholders.PlaceholderSectionType
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.components.DiscordChannelSelectMenu.discordChannelSelectMenu
import net.perfectdreams.loritta.morenitta.website.components.FancyDetails.fancyDetails
import net.perfectdreams.loritta.morenitta.website.components.LoadingSectionComponents
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.bluesky.BlueskyProfile
import net.perfectdreams.loritta.morenitta.website.views.htmxDiscordLikeLoadingButtonSetup
import net.perfectdreams.loritta.morenitta.website.views.htmxGetAsHref
import net.perfectdreams.loritta.serializable.*
import net.perfectdreams.loritta.serializable.messageeditor.LorittaDiscordMessageEditorSetupConfig
import net.perfectdreams.loritta.serializable.messageeditor.MessageEditorMessagePlaceholder
import net.perfectdreams.loritta.serializable.messageeditor.TestMessageTargetChannelQuery
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession

class GuildConfigureBlueskyProfileView(
    loritta: LorittaWebsite,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    legacyBaseLocale: LegacyBaseLocale,
    userIdentification: LorittaJsonWebSession.UserIdentification,
    userPremiumPlan: UserPremiumPlans,
    colorTheme: ColorTheme,
    guild: Guild,
    selectedType: String,
    private val trackId: Long?,
    private val blueskyProfile: BlueskyProfile,
    private val trackSettings: BlueskyTrackSettings,
) : GuildDashboardView(
    loritta,
    i18nContext,
    locale,
    path,
    legacyBaseLocale,
    userIdentification,
    userPremiumPlan,
    colorTheme,
    guild,
    selectedType
) {
    override fun DIV.generateRightSidebarContents() {
        val serializableGuild = DiscordGuild(
            guild.idLong,
            guild.name,
            guild.iconId,
            guild.roles.map {
                DiscordRole(
                    it.idLong,
                    it.name,
                    it.colorRaw
                )
            },
            guild.channels.map {
                when (it) {
                    is TextChannel -> {
                        TextDiscordChannel(
                            it.idLong,
                            it.name,
                            it.canTalk()
                        )
                    }
                    is VoiceChannel -> {
                        VoiceDiscordChannel(
                            it.idLong,
                            it.name
                        )
                    }

                    is Category -> {
                        CategoryDiscordChannel(
                            it.idLong,
                            it.name
                        )
                    }

                    is NewsChannel -> {
                        NewsDiscordChannel(
                            it.idLong,
                            it.name,
                            it.canTalk()
                        )
                    }

                    is StageChannel -> {
                        StageDiscordChannel(
                            it.idLong,
                            it.name
                        )
                    }

                    is ForumChannel -> {
                        ForumDiscordChannel(
                            it.idLong,
                            it.name
                        )
                    }

                    else -> UnknownDiscordChannel(
                        it.idLong,
                        it.name
                    )
                }
            },
            guild.emojis.map {
                DiscordEmoji(
                    it.idLong,
                    it.name,
                    it.isAnimated
                )
            }
        )

        val serializableSelfLorittaUser = DiscordUser(
            guild.selfMember.user.idLong,
            guild.selfMember.user.name,
            guild.selfMember.user.globalName,
            guild.selfMember.user.discriminator,
            guild.selfMember.user.avatarId
        )

        div {
            a(classes = "discord-button no-background-theme-dependent-dark-text", href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/bluesky") {
                htmxGetAsHref()
                attributes["hx-push-url"] = "true"
                attributes["hx-swap"] = "outerHTML show:top settle:0ms swap:0ms"
                attributes["hx-select"] = "#right-sidebar-contents"
                attributes["hx-target"] = "#right-sidebar-contents"

                htmxDiscordLikeLoadingButtonSetup(
                    i18nContext
                ) {
                    this.text("Voltar para a lista de contas do Bluesky")
                }
            }

            hr {}

            div(classes = "hero-wrapper") {
                div(classes = "hero-image") {
                    img(src = blueskyProfile.avatar) {
                        style = "border-radius: 100%; width: 300px; height: 300px;"
                    }
                }

                div(classes = "hero-text") {
                    h1 {
                        text(blueskyProfile.effectiveName)
                    }
                }
            }

            hr {}

            form {
                id = "module-config"
                // If the trackId is null, then the save bar should ALWAYS be dirty
                if (trackId != null) {
                    attributes["loritta-synchronize-with-save-bar"] = "#save-bar"
                }

                // TODO: We technically don't need this here if the trackId is != null
                hiddenInput {
                    name = "did"
                    value = blueskyProfile.did
                }

                div(classes = "field-wrappers") {
                    div(classes = "field-wrapper") {
                        div(classes = "field-title") {
                            label {
                                text("Canal onde será enviado as mensagens")
                            }
                        }

                        div {
                            style = "width: 100%;"

                            discordChannelSelectMenu(
                                lorittaWebsite,
                                i18nContext,
                                "channelId",
                                guild.channels.filterIsInstance<GuildMessageChannel>(),
                                trackSettings.channelId,
                                null
                            )
                        }
                    }

                    div {
                        // TODO: Refactor this to be reusable!
                        textArea {
                            attributes["loritta-discord-message-editor"] = "true"
                            attributes["loritta-discord-message-editor-config"] = Json.encodeToString(
                                LorittaDiscordMessageEditorSetupConfig(
                                    listOf(),
                                    PlaceholderSectionType.BLUESKY_POST_MESSAGE,
                                    BlueskyPostMessagePlaceholders.placeholders.flatMap { placeholder ->
                                        when (placeholder) {
                                            BlueskyPostMessagePlaceholders.GuildIconUrlPlaceholder -> placeholder.names.map { placeholderName ->
                                                MessageEditorMessagePlaceholder(
                                                    placeholderName.placeholder.name,
                                                    guild.iconUrl ?: "???", // TODO: Provide a proper fallback
                                                    placeholder.renderType
                                                )
                                            }
                                            BlueskyPostMessagePlaceholders.GuildNamePlaceholder -> placeholder.names.map { placeholderName ->
                                                MessageEditorMessagePlaceholder(
                                                    placeholderName.placeholder.name,
                                                    guild.name,
                                                    placeholder.renderType
                                                )
                                            }
                                            BlueskyPostMessagePlaceholders.GuildSizePlaceholder -> placeholder.names.map { placeholderName ->
                                                MessageEditorMessagePlaceholder(
                                                    placeholderName.placeholder.name,
                                                    guild.memberCount.toString(),
                                                    placeholder.renderType
                                                )
                                            }
                                            BlueskyPostMessagePlaceholders.PostUrlPlaceholder -> placeholder.names.map { placeholderName ->
                                                MessageEditorMessagePlaceholder(
                                                    placeholderName.placeholder.name,
                                                    "https://bsky.app/profile/loritta.website/post/3l34ux7btja24",
                                                    placeholder.renderType
                                                )
                                            }
                                        }
                                    },
                                    serializableGuild,
                                    serializableSelfLorittaUser,
                                    TestMessageTargetChannelQuery.QuerySelector("[name='channelId']"),
                                    "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/test-message"
                                )
                            )
                            name = "message"
                            text(trackSettings.message)
                        }
                    }

                    fancyDetails(
                        "Quais são as variáveis/placeholders que eu posso usar?"
                    ) {
                        table {
                            thead {
                                tr {
                                    th {
                                        text("Placeholder")
                                    }

                                    th {
                                        text("Significado")
                                    }
                                }
                            }

                            tbody {
                                for (placeholder in BlueskyPostMessagePlaceholders.placeholders) {
                                    tr {
                                        td {
                                            // TODO: Put it inside of a code block
                                            var isFirst = true
                                            for (name in placeholder.names) {
                                                if (!isFirst)
                                                    text(", ")
                                                isFirst = false
                                                code {
                                                    text(name.placeholder.asKey)
                                                }
                                            }
                                        }

                                        td {
                                            val i18nDescription = placeholder.description
                                            if (i18nDescription != null) {
                                                text(i18nContext.get(i18nDescription))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            hr {}

            // TODO: Refactor this to be reusable!
            // Maybe, with what little power you have... You can SAVE something else.
            div(classes = "save-bar-fill-screen-height") {}

            div(classes = "save-bar") {
                if (trackId == null) {
                    classes += "has-changes"
                } else {
                    classes += "initial-state"
                    classes += "no-changes"
                }

                id = "save-bar"
                attributes["loritta-save-bar"] = "true"

                div(classes = "save-bar-small-text") {
                    text("Deseja salvar?")
                }

                div(classes = "save-bar-large-text") {
                    text("Cuidado! Você tem alterações que não foram salvas")
                }

                div(classes = "save-bar-buttons") {
                    button(classes = "discord-button no-background-light-text") {
                        attributes["hx-get"] = ""
                        attributes["hx-select"] = "#module-config"
                        attributes["hx-target"] = "#module-config"
                        attributes["hx-indicator"] = "find .htmx-discord-like-loading-button"
                        attributes["hx-disabled-elt"] = "this"
                        // We don't want to swap nor settle because that causes a flicker due to our custom select menu
                        attributes["hx-swap"] = "innerHTML settle:0ms swap:0ms"
                        if (trackId != null) {
                            attributes["hx-on::after-request"] = """
                            if (event.detail.successful) {
                                document.querySelector("#save-bar").classList.add("no-changes")
                                document.querySelector("#save-bar").classList.remove("has-changes")
                                window['spicy-morenitta'].playSoundEffect("recycle-bin")
                            }
                            """.trimIndent()
                        } else {
                            // TODO: This causes the sound effect to be played twice, because of the HX-Trigger header
                            attributes["hx-on::after-request"] = """
                            if (event.detail.successful) {
                                window['spicy-morenitta'].playSoundEffect("recycle-bin")
                            }
                            """.trimIndent()
                        }

                        div(classes = "htmx-discord-like-loading-button") {
                            div {
                                text("Redefinir")
                            }

                            div(classes = "loading-text-wrapper") {
                                img(src = LoadingSectionComponents.list.random())

                                text(i18nContext.get(I18nKeysData.Website.Dashboard.Loading))
                            }
                        }
                    }

                    button(classes = "discord-button success") {
                        if (trackId != null) {
                            attributes["hx-patch"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/bluesky/tracks/$trackId"
                        } else {
                            attributes["hx-put"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/bluesky/tracks"
                        }
                        attributes["hx-swap"] = "innerHTML settle:0ms swap:0ms"
                        attributes["hx-select"] = "#module-config"
                        attributes["hx-target"] = "#module-config"
                        attributes["hx-include"] = "#module-config"
                        attributes["hx-indicator"] = "find .htmx-discord-like-loading-button"
                        attributes["hx-disabled-elt"] = "this"
                        // We don't want to swap nor settle because that causes a flicker due to our custom select menu
                        attributes["hx-swap"] = "innerHTML settle:0ms swap:0ms"
                        attributes["hx-on::after-request"] = """
                            if (event.detail.successful) {
                                document.querySelector("#save-bar").classList.add("no-changes")
                                document.querySelector("#save-bar").classList.remove("has-changes")
                            }
                            """.trimIndent()

                        div(classes = "htmx-discord-like-loading-button") {
                            div {
                                text("Salvar")
                            }

                            div(classes = "loading-text-wrapper") {
                                img(src = LoadingSectionComponents.list.random())

                                text(i18nContext.get(I18nKeysData.Website.Dashboard.Loading))
                            }
                        }
                    }
                }
            }
        }
    }

    data class BlueskyTrackSettings(
        val channelId: Long?,
        val message: String
    )
}