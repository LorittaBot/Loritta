package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.TEXTAREA
import kotlinx.html.textArea
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.embeds.DiscordMessage
import net.perfectdreams.loritta.dashboard.BlissHex
import net.perfectdreams.loritta.dashboard.discord.DiscordEmoji
import net.perfectdreams.loritta.dashboard.discord.DiscordGuild
import net.perfectdreams.loritta.dashboard.discord.DiscordRole
import net.perfectdreams.loritta.dashboard.discordmessages.RenderableDiscordUser
import net.perfectdreams.loritta.dashboard.messageeditor.LorittaMessageTemplate
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorMessagePlaceholderGroup
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorMessagePlaceholderGroup.RenderType
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons
import net.perfectdreams.loritta.placeholders.LorittaPlaceholder
import net.perfectdreams.loritta.placeholders.sections.SectionPlaceholder

fun FlowContent.discordMessageEditor(
    guild: Guild,
    target: MessageEditorBootstrap.TestMessageTarget,
    templates: List<LorittaMessageTemplate>,
    placeholders: List<MessageEditorMessagePlaceholderGroup>,
    message: String,
    block: TEXTAREA.() -> (Unit)
) {
    textArea {
        attributes["bliss-component"] = "discord-message-editor"
        attributes["discord-message-editor-bootstrap"] = BlissHex.encodeToHexString(
            Json.encodeToString(
                MessageEditorBootstrap(
                    RenderableDiscordUser(
                        guild.selfMember.effectiveName,
                        guild.selfMember.effectiveAvatarUrl,
                        guild.selfMember.user.isBot,
                        true
                    ),
                    templates,
                    placeholders,
                    DiscordGuild(
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
                        listOf(),
                        guild.emojis.map {
                            DiscordEmoji(
                                it.idLong,
                                it.name,
                                it.isAnimated
                            )
                        }
                    ),
                    target,
                    SVGIcons.CheckFat.html.toString(),
                    SVGIcons.EyeDropper.html.toString(),
                    SVGIcons.CaretDown.html.toString(),
                )
            )
        )

        block()

        text(message)
    }
}

fun createMessageTemplate(
    title: String,
    content: String
) = LorittaMessageTemplate(
    title,
    Json.encodeToString(DiscordMessage(content))
)

fun createPlaceholderGroup(
    placeholders: List<LorittaPlaceholder>,
    description: String?,
    replaceWithFrontend: String,
    replaceWithBackend: String,
    renderType: RenderType
) = MessageEditorMessagePlaceholderGroup(placeholders, description, replaceWithBackend, replaceWithFrontend, renderType)

fun createPlaceholderGroup(
    placeholders: List<LorittaPlaceholder>,
    description: String?,
    replace: String,
    renderType: RenderType
) = MessageEditorMessagePlaceholderGroup(placeholders, description, replace, replace, renderType)

fun createPlaceholderGroup(
    sectionPlaceholder: SectionPlaceholder,
    description: String?,
    replace: String,
    renderType: RenderType
) = MessageEditorMessagePlaceholderGroup(sectionPlaceholder.placeholders, description, replace, replace, renderType)

fun createUserMentionPlaceholderGroup(i18nContext: I18nContext, sectionPlaceholder: SectionPlaceholder, userId: Long, username: String, globalName: String?): MessageEditorMessagePlaceholderGroup {
    return createPlaceholderGroup(
        sectionPlaceholder.placeholders,
        null,
        "@${globalName ?: username}",
        "<@$userId>",
        RenderType.MENTION
    )
}

fun createUserNamePlaceholderGroup(i18nContext: I18nContext, sectionPlaceholder: SectionPlaceholder, username: String, globalName: String?): MessageEditorMessagePlaceholderGroup {
    return createPlaceholderGroup(
        sectionPlaceholder.placeholders,
        null,
        globalName ?: username,
        RenderType.TEXT
    )
}

fun createUserIdPlaceholderGroup(i18nContext: I18nContext, sectionPlaceholder: SectionPlaceholder, userId: Long): MessageEditorMessagePlaceholderGroup {
    return createPlaceholderGroup(
        sectionPlaceholder.placeholders,
        null,
        userId.toString(),
        RenderType.TEXT
    )
}

fun createUserAvatarUrlPlaceholderGroup(i18nContext: I18nContext, sectionPlaceholder: SectionPlaceholder, session: UserSession): MessageEditorMessagePlaceholderGroup {
    return createPlaceholderGroup(
        sectionPlaceholder.placeholders,
        null,
        session.getEffectiveAvatarUrl(),
        RenderType.TEXT
    )
}

fun createUserDiscriminatorPlaceholderGroup(i18nContext: I18nContext, sectionPlaceholder: SectionPlaceholder, discriminator: String): MessageEditorMessagePlaceholderGroup {
    return createPlaceholderGroup(
        sectionPlaceholder.placeholders,
        null,
        discriminator.padStart(4, '0'),
        RenderType.TEXT
    )
}

fun createUserTagPlaceholderGroup(i18nContext: I18nContext, sectionPlaceholder: SectionPlaceholder, username: String): MessageEditorMessagePlaceholderGroup {
    return createPlaceholderGroup(
        sectionPlaceholder.placeholders,
        null,
        "@${username}",
        RenderType.TEXT
    )
}

fun createGuildNamePlaceholderGroup(i18nContext: I18nContext, sectionPlaceholder: SectionPlaceholder, guild: Guild): MessageEditorMessagePlaceholderGroup {
    return createPlaceholderGroup(
        sectionPlaceholder,
        null,
        guild.name,
        RenderType.TEXT
    )
}

fun createGuildIconUrlPlaceholderGroup(i18nContext: I18nContext, sectionPlaceholder: SectionPlaceholder, guild: Guild): MessageEditorMessagePlaceholderGroup {
    return createPlaceholderGroup(
        sectionPlaceholder,
        null,
        guild.iconUrl ?: "???",
        RenderType.TEXT
    )
}

fun createGuildSizePlaceholderGroup(i18nContext: I18nContext, sectionPlaceholder: SectionPlaceholder, guild: Guild): MessageEditorMessagePlaceholderGroup {
    return createPlaceholderGroup(
        sectionPlaceholder,
        null,
        guild.memberCount.toString(),
        RenderType.TEXT
    )
}