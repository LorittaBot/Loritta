package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.gamersaferverify

import androidx.compose.runtime.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.*
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.FieldWrapper
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.FieldWrappers
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.ConfigureGuildGamerSaferVerifyScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.GamerSaferVerifyViewModel
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.common.utils.Color
import net.perfectdreams.loritta.serializable.DiscordGuild
import net.perfectdreams.loritta.serializable.GamerSaferVerificationUserAndRole
import net.perfectdreams.loritta.serializable.dashboard.requests.LorittaDashboardRPCRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.rgb
import org.jetbrains.compose.web.dom.*
import kotlin.time.Duration.Companion.days

@Composable
fun GamerSaferVerifyOverview(
    m: LorittaDashboardFrontend,
    screen: ConfigureGuildGamerSaferVerifyScreen,
    i18nContext: I18nContext,
    guild: DiscordGuild,
    config: GamerSaferVerifyViewModel.MutableGuildGamerSaferConfig
) {
    // TODO: Move this somewhere else
    fun unpackRGB(packedRGB: Int): Color {
        val red = (packedRGB shr 16) and 0xFF
        val green = (packedRGB shr 8) and 0xFF
        val blue = packedRGB and 0xFF

        return Color(red, green, blue)
    }

    Div {
        Div {
            Div {
                var premiumKey by remember { mutableStateOf<String?>(null) }
                var user by remember { mutableStateOf<CachedUserInfo?>(null) }
                var roleId by remember { mutableStateOf<Long?>(null) }
                var verifyEveryX by remember { mutableStateOf<String>(1.days.toIsoString()) }

                FieldWrappers {
                    /* FieldWrapper {
                        FieldLabel("Key Premium da GamerSafer")

                        Div {
                            Text("Para usar todas as funcionalidades deste plugin, você precisa comprar um plano premium da GamerSafer - ")
                            A(href = "https://checkout.gamersafer.com/loritta") {
                                Text("https://checkout.gamersafer.com/loritta")
                            }
                        }

                        TextInput(
                            attrs = {
                                value(premiumKey ?: "")

                                onInput {
                                    premiumKey = it.value
                                }
                            }
                        )
                    } */

                    FieldWrapper {
                        FieldLabel("Cargo de Verificação")

                        SelectMenu(
                            buildList {
                                add(
                                    SelectMenuEntry(
                                        {
                                            Text("Sem cargo de verificação")
                                        },
                                        config.verificationRoleId.value == null,
                                        {
                                            config.verificationRoleId.value = null
                                        },
                                        {}
                                    )
                                )

                                addAll(
                                    guild.roles
                                        .map { roleData ->
                                            SelectMenuEntry(
                                                {
                                                    Span(attrs = {
                                                        if (roleData.color != 0x1FFFFFFF) {
                                                            style {
                                                                val color = unpackRGB(roleData.color)
                                                                color(rgb(color.red, color.green, color.blue))
                                                            }
                                                        }
                                                    }) {
                                                        Text(roleData.name)
                                                    }
                                                },
                                                config.verificationRoleId.value == roleData.id,
                                                {
                                                    config.verificationRoleId.value = roleData.id
                                                },
                                                {}
                                            )
                                        }
                                )
                            }
                        )
                    }
                }

                Hr {}

                FieldWrappers {
                    FieldWrapper {
                        FieldLabel("Usuário")

                        DiscordUserInput(
                            m,
                            i18nContext,
                            screen,
                            {
                                id("effect-user")
                            }
                        ) {
                            user = it
                        }
                    }

                    FieldWrapper {
                        FieldLabel("Cargo")

                        val entries = guild.roles
                            .map { roleData ->
                                SelectMenuEntry(
                                    {
                                        Span(attrs = {
                                            if (roleData.color != 0x1FFFFFFF) {
                                                style {
                                                    val color = unpackRGB(roleData.color)
                                                    color(rgb(color.red, color.green, color.blue))
                                                }
                                            }
                                        }) {
                                            Text(roleData.name)
                                        }
                                    },
                                    roleId == roleData.id,
                                    {
                                        roleId = roleData.id
                                    },
                                    {}
                                )
                            }

                        SelectMenu(entries)
                    }

                    FieldWrapper {
                        FieldLabel("Período entre cada Verificação")

                        val entries2 = listOf(
                            SelectMenuEntry(
                                {
                                    Text("1 dia")
                                },
                                verifyEveryX == 1.days.toIsoString(),
                                {
                                    verifyEveryX = 1.days.toIsoString()
                                },
                                {}
                            ),
                            SelectMenuEntry(
                                {
                                    Text("3 dias")
                                },
                                verifyEveryX == 3.days.toIsoString(),
                                {
                                    verifyEveryX = 3.days.toIsoString()
                                },
                                {}
                            ),
                            SelectMenuEntry(
                                {
                                    Text("7 dias")
                                },
                                verifyEveryX == 7.days.toIsoString(),
                                {
                                    verifyEveryX = 7.days.toIsoString()
                                },
                                {}
                            ),
                            SelectMenuEntry(
                                {
                                    Text("30 dias")
                                },
                                verifyEveryX == 30.days.toIsoString(),
                                {
                                    verifyEveryX = 30.days.toIsoString()
                                },
                                {}
                            ),
                            SelectMenuEntry(
                                {
                                    Text("365 dias")
                                },
                                verifyEveryX == 365.days.toIsoString(),
                                {
                                    verifyEveryX = 365.days.toIsoString()
                                },
                                {}
                            )
                        )

                        SelectMenu(entries2)
                    }

                    FieldWrapper {
                        DiscordButton(
                            DiscordButtonType.SUCCESS,
                            attrs = {
                                classes("button-discord", "button-discord-info", "pure-button")

                                val retrievedUser = user

                                if (roleId == null || retrievedUser == null)
                                    disabled()
                                else {
                                    onClick {
                                        println("omg u clicked on the button!")
                                        println(roleId)
                                        println(verifyEveryX)

                                        val roleCount = config.verificationRoles.map {
                                            it.roleId
                                        }.distinct().size

                                        val staffCount = config.verificationRoles.size

                                        // TODO: Backend verifications
                                        if (roleCount >= 1) {
                                            m.globalState.openCloseOnlyModal("Função Premium") {
                                                Text("Atualmente você só pode colocar um cargo para verificação")
                                            }
                                            return@onClick
                                        }

                                        if (staffCount >= 5) {
                                            m.globalState.openCloseOnlyModal("Função Premium") {
                                                Text("Atualmente você só pode colocar cinco pessoas para verificação")
                                            }
                                            return@onClick
                                        }

                                        config.verificationRoles.add(
                                            GamerSaferVerificationUserAndRole(
                                                retrievedUser.id.value.toLong(),
                                                retrievedUser,
                                                roleId!!,
                                                verifyEveryX
                                            )
                                        )

                                        // TODO: Clean up, I don't know how can we clean up the "user" element tbh, maybe key it on the DiscordUserInput?
                                        // user = null
                                        // roleId = null

                                        console.log("Current role size: ${config.verificationRoles.size}")
                                    }
                                }
                            }
                        ) {
                            Text("Adicionar")
                        }
                    }
                }
            }
            Hr {}
            H2 {
                Text("Verificações Configuradas")
            }

            if (config.verificationRoles.isNotEmpty()) {
                Cards {
                    for (role in config.verificationRoles) {
                        val roleData = guild.roles.firstOrNull { role.roleId == it.id }

                        Card {
                            IconWithText(SVGIconManager.user) {
                                InlineNullableUserDisplay(UserId(role.userId), role.cachedUserInfo)
                            }

                            IconWithText(SVGIconManager.idCard) {
                                if (roleData != null) {
                                    val name = roleData.name
                                    val color = if (roleData.color != 0x1FFFFFFF) {
                                        unpackRGB(roleData.color)
                                    } else null

                                    InlineDiscordMention(name, color)
                                } else {
                                    InlineDiscordMention("Cargo Desconhecido (${role.roleId})")
                                }
                            }

                            IconWithText(SVGIconManager.clock) {
                                Text(role.time)
                            }

                            DiscordButton(
                                DiscordButtonType.DANGER,
                                attrs = {
                                    classes("button-discord", "button-discord-info", "pure-button")
                                    onClick {
                                        config.verificationRoles.remove(role)
                                    }
                                }
                            ) {
                                Text("Remover")
                            }
                        }
                    }
                }
            } else {
                Div {
                    EmptySection(i18nContext)
                }
            }

            Hr {}

            DiscordButton(
                DiscordButtonType.SUCCESS,
                attrs = {
                    classes("button-discord", "button-discord-success", "pure-button")

                    onClick {
                        screen.launch {
                            m.makeRPCRequest<LorittaDashboardRPCResponse>(
                                LorittaDashboardRPCRequest.UpdateGuildGamerSaferConfigRequest(
                                    screen.guildId,
                                    GamerSaferVerifyViewModel.MutableGuildGamerSaferConfig.to(config)
                                )
                            )
                            m.configSavedSfx.play()
                        }
                    }
                }
            ) {
                Text("Salvar")
            }
        }
    }
}