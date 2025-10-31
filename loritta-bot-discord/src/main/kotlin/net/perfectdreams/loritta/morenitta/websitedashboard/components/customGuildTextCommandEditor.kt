package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.textInput
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.placeholders.sections.CustomTextCommandPlaceholders

fun FlowContent.customGuildCommandTextEditor(
    i18nContext: I18nContext,
    guild: Guild,
    session: UserSession,
    label: String,
    message: String?
) {
    val defaultMessage = createMessageTemplate(
        "Padrão",
        i18nContext.get(DashboardI18nKeysData.CustomCommands.TextCommand.DefaultMessage)
    )

    val customGuildCommandTextPlaceholders = CustomTextCommandPlaceholders.placeholders.map {
        when (it) {
            CustomTextCommandPlaceholders.GuildIconUrlPlaceholder -> createGuildIconUrlPlaceholderGroup(i18nContext, it, guild)
            CustomTextCommandPlaceholders.GuildNamePlaceholder -> createGuildNamePlaceholderGroup(i18nContext, it, guild)
            CustomTextCommandPlaceholders.UserAvatarUrlPlaceholder -> createUserAvatarUrlPlaceholderGroup(i18nContext, it, session)
            CustomTextCommandPlaceholders.UserIdPlaceholder -> createUserIdPlaceholderGroup(i18nContext, it, session.userId)
            CustomTextCommandPlaceholders.GuildSizePlaceholder -> createGuildSizePlaceholderGroup(i18nContext, it, guild)
            CustomTextCommandPlaceholders.UserDiscriminatorPlaceholder -> createUserDiscriminatorPlaceholderGroup(i18nContext, it, session.discriminator)
            CustomTextCommandPlaceholders.UserMentionPlaceholder -> createUserMentionPlaceholderGroup(i18nContext, it, session.userId, session.username, session.globalName)
            CustomTextCommandPlaceholders.UserNamePlaceholder -> createUserNamePlaceholderGroup(i18nContext, it, session.username, session.globalName)
            CustomTextCommandPlaceholders.UserTagPlaceholder -> createUserTagPlaceholderGroup(i18nContext, it, session.username)
        }
    }

    fieldWrappers {
        fieldWrapper {
            fieldInformationBlock {
                fieldTitle {
                    text("Nome do Comando")
                }
            }

            textInput {
                value = label
                name = "label"
                this.attributes["bliss-transform-text"] = "trim, no-spaces, lowercase"
                this.attributes["loritta-config"] = "label"
            }
        }

        discordMessageEditor(
            i18nContext,
            guild,
            { text("Mensagem") },
            null,
            MessageEditorBootstrap.TestMessageTarget.Unavailable,
            listOf(),
            customGuildCommandTextPlaceholders,
            message ?: defaultMessage.content,
            "message"
        ) {
            this.attributes["loritta-config"] = "message"
        }
    }
}