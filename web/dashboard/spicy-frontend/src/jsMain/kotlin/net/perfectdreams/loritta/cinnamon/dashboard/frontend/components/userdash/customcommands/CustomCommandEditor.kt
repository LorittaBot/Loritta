package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.customcommands

import androidx.compose.runtime.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetUserIdentificationResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.*
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.FieldWrapper
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.FieldWrappers
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.DiscordUtils
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Toast
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.EditCustomCommandViewModel
import net.perfectdreams.loritta.common.utils.embeds.DiscordMessage
import net.perfectdreams.loritta.common.utils.placeholders.JoinMessagePlaceholders
import net.perfectdreams.loritta.serializable.CustomCommandCodeType
import net.perfectdreams.loritta.serializable.DiscordGuild
import net.perfectdreams.loritta.serializable.DiscordUser
import net.perfectdreams.loritta.serializable.config.GuildCustomCommand
import net.perfectdreams.loritta.serializable.dashboard.requests.DashGuildScopedRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.DashGuildScopedResponse
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.TextInput

@Composable
fun CustomCommandEditor(
    m: LorittaDashboardFrontend,
    i18nContext: I18nContext,
    guild: DiscordGuild,
    userInfo: GetUserIdentificationResponse,
    selfUser: DiscordUser,
    customCommand: GuildCustomCommand,
    alwaysDirty: Boolean,
    postSave: (Boolean, Long) -> (Unit)
) {
    val isNewCommand = customCommand.id == -1L

    var mutableGuildCustomCommand by remember { mutableStateOf(EditCustomCommandViewModel.toMutableConfig(customCommand)) }

    // The initial config state
    var startConfigState by remember { mutableStateOf(EditCustomCommandViewModel.toDataConfig(mutableGuildCustomCommand)) }

    FieldWrappers {
        FieldWrapper {
            FieldLabel("Nome do Comando")

            TextInput(mutableGuildCustomCommand.label) {
                onInput {
                    // Remove spaces + make it lowercase
                    mutableGuildCustomCommand.label = it.value
                        .replace(" ", "")
                        .lowercase()
                }
            }
        }

        FieldWrapper {
            FieldLabel("Mensagem")

            DiscordMessageEditor(
                m,
                i18nContext,
                null,
                JoinMessagePlaceholders,
                {
                    when (it) {
                        JoinMessagePlaceholders.UserMentionPlaceholder -> "@${userInfo.globalName ?: userInfo.username}"
                        JoinMessagePlaceholders.UserNamePlaceholder -> userInfo.globalName ?: userInfo.username
                        JoinMessagePlaceholders.UserDiscriminatorPlaceholder -> userInfo.discriminator
                        JoinMessagePlaceholders.UserTagPlaceholder -> "@${userInfo.username}"
                        JoinMessagePlaceholders.UserIdPlaceholder -> userInfo.id.value.toString()
                        JoinMessagePlaceholders.UserAvatarUrlPlaceholder -> DiscordUtils.getUserAvatarUrl(userInfo.id.value.toLong(), userInfo.avatarId)
                        JoinMessagePlaceholders.GuildNamePlaceholder -> guild.name
                        JoinMessagePlaceholders.GuildSizePlaceholder -> "100" // TODO: Fix this!
                        JoinMessagePlaceholders.GuildIconUrlPlaceholder -> guild.getIconUrl(512) ?: "" // TODO: Fix this!
                    }
                },
                null,
                guild,
                TargetChannelResult.ChannelNotSelected,
                userInfo,
                selfUser,
                listOf(
                    DiscordMessageWithAuthor(
                        author = RenderableDiscordUser(
                            userInfo.globalName ?: userInfo.username,
                            DiscordUtils.getUserAvatarUrl(userInfo.id.value.toLong(), userInfo.avatarId),
                            false
                        ),
                        message = DiscordMessage(content = "+${mutableGuildCustomCommand.label}")
                    )
                ),
                listOf(),
                mutableGuildCustomCommand.code
            ) {
                mutableGuildCustomCommand.code = it
            }
        }
    }

    Hr {}

    var isSaving by remember { mutableStateOf(false) }

    SaveBar(
        m,
        i18nContext,
        alwaysDirty || startConfigState != EditCustomCommandViewModel.toDataConfig(mutableGuildCustomCommand),
        isSaving,
        onReset = {
            mutableGuildCustomCommand = EditCustomCommandViewModel.toMutableConfig(startConfigState)
        },
        onSave = {
            GlobalScope.launch {
                isSaving = true

                m.globalState.showToast(Toast.Type.INFO, "Salvando configuração...")
                // val config = WelcomerViewModel.toDataConfig(mutableWelcomerConfig)
                m.makeGuildScopedRPCRequestWithGenericHandling<DashGuildScopedResponse.UpsertGuildCustomCommandConfigResponse>(
                    guild.id,
                    DashGuildScopedRequest.UpsertGuildCustomCommandConfigRequest(
                        if (isNewCommand) null else mutableGuildCustomCommand.id,
                        mutableGuildCustomCommand.label,
                        CustomCommandCodeType.SIMPLE_TEXT,
                        mutableGuildCustomCommand.code
                    ),
                    onSuccess = {
                        m.globalState.showToast(Toast.Type.SUCCESS, "Configuração salva!")
                        m.soundEffects.configSaved.play(1.0)
                        isSaving = false
                        m.globalState.activeSaveBar = false

                        startConfigState = GuildCustomCommand(
                            mutableGuildCustomCommand.id,
                            mutableGuildCustomCommand.label,
                            CustomCommandCodeType.SIMPLE_TEXT,
                            mutableGuildCustomCommand.code
                        )

                        postSave.invoke(isNewCommand, it.commandId)
                    },
                    onError = {
                        m.soundEffects.configError.play(1.0)
                        isSaving = false
                        m.globalState.activeSaveBar = false
                    }
                )
            }
        }
    )
}