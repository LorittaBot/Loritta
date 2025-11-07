import io.ktor.server.application.*
import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldInformationBlock
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.genericSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings

class CustomBrandingGuildDashboardRoute(website: LorittaDashboardWebServer): RequiresGuildAuthDashboardLocalizedRoute(website, "/custom-branding") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        call.respondHtml {
            dashboardBase(
                i18nContext,
                "Custom Branding",
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.CUSTOM_COMMANDS)
                },
                {
                    val lorittaMember = guild.selfMember

                    rightSidebarContentAndSaveBarWrapper(
                        website.shouldDisplayAds(call, userPremiumPlan, null),
                        {
                            if (call.request.headers["Loritta-Configuration-Reset"] == "true") {
                                blissEvent("resyncState", "[bliss-component='save-bar']")
                                blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Configuração redefinida!"))
                            }

                            div(classes = "hero-wrapper") {
                                div(classes = "hero-text") {
                                    // TODO: make a proper texting :3
                                    h1 {
                                        text("Custom branding")
                                    }

                                    p {
                                        text("Customize Loritta's branding like avatar, banner, bio and display name!")
                                    }
                                }
                            }

                            hr()

                            div {
                                id = "section-config"
                                div {
                                    style = "margin-top: 8px; position: relative; display: flex; justify-content: center; align-items: center; border-radius: 8px; aspect-ratio: 17/6; overflow: clip; width: 100%"

                                    label {
                                        htmlFor = "loritta-banner-input"
                                        style = "border-radius: 8px;"
                                        classes = setOf("edit-image-overlay")

                                        text("Edit")
                                    }
                                    // TODO: Get current banner
                                    img(src = lorittaMember.effectiveAvatarUrl, classes = "discord-message-avatar", block = { style = "display: flex; aspect-ratio: 17/6; width: 100%; object-fit: cover"; id = "loritta-banner-img" })
                                    fileInput {
                                        id = "loritta-banner-input"
                                        style = "appearance: none; display: none;"
                                        attributes["loritta-config"] = "banner"
                                        name = "banner"
                                        onChange = """
                                            const file = event.target?.files?.item(0);
                                            if (!file) {
                                                return;
                                            }
                                            
                                            const reader = new FileReader();
                                            reader.readAsDataURL(file);
                                            reader.onload = () => {
                                                const bannerInput = document.getElementById("loritta-banner-img")
                                                if (!bannerInput) {
                                                    return;
                                                }
                                                
                                                bannerInput.src = reader.result;
                                            };
                                        """.trimIndent()
                                    }
                                }

                                hr()

                                div {
                                    style = "display: flex; gap: 32px"
                                    div {
                                        style =
                                            "margin-top: 8px; position: relative; width: fit-content; display: flex; justify-content: center; align-items: center; border-radius: 99999px; aspect-ratio: 1/1; overflow: clip; width: 128px; height: 128px;"

                                        label {
                                            htmlFor = "loritta-icon-input"
                                            style = "border-radius: 99999px;"
                                            classes = setOf("edit-image-overlay")

                                            text("Edit")
                                        }
                                        img(src = lorittaMember.effectiveAvatarUrl, classes = "discord-message-avatar", block = { style = "display: flex; aspect-ratio: 1/1; width: 100%; object-fit: cover"; id = "loritta-avatar-img" })
                                        fileInput {
                                            id = "loritta-icon-input"
                                            style = "appearance: none; display: none;"
                                            attributes["loritta-config"] = "avatar"
                                            name = "avatar"
                                            onChange = """
                                            const file = event.target?.files?.item(0);
                                            if (!file) {
                                                return;
                                            }
                                            
                                            const reader = new FileReader();
                                            reader.readAsDataURL(file);
                                            reader.onload = () => {
                                                const avatarInput = document.getElementById("loritta-avatar-img")
                                                if (!avatarInput) {
                                                    return;
                                                }
                                                
                                                avatarInput.src = reader.result;
                                            };
                                        """.trimIndent()
                                        }
                                    }
                                    div {
                                        style = "width: 100%"
                                        fieldWrappers {
                                            fieldWrapper {
                                                fieldInformationBlock {
                                                    fieldTitle {
                                                        text("Display Name")
                                                    }
                                                }

                                                val nickname = if (lorittaMember.nickname != null) {
                                                    lorittaMember.nickname.toString()
                                                } else {
                                                    lorittaMember.user.name
                                                }
                                                textInput {
                                                    attributes["loritta-config"] = "displayName"
                                                    name = "displayName"
                                                    value = nickname
                                                }
                                            }

                                            fieldWrapper {
                                                fieldInformationBlock {
                                                    fieldTitle {
                                                        text("Bio")
                                                    }
                                                }

                                                textArea {
                                                    // TODO: Get current bio
                                                    attributes["loritta-config"] = "bio"
                                                    name = "bio"
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        {
                            genericSaveBar(
                                i18nContext,
                                false,
                                guild,
                                "/custom-branding"
                            )
                        }
                    )
                }
            )
        }
    }
}