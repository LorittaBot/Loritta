package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.hr
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.style
import kotlinx.html.textArea
import kotlinx.html.textInput
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.lorittapartners.PartnerApplicationsUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData

fun FlowContent.partnerApplicationForm(
    i18nContext: I18nContext,
    guild: Guild,
    member: Member
) {
    fieldWrappers {
        fieldWrapper {
            // Guild header with icon and name
            div(classes = "simple-image-with-text-header") {
                val iconUrl = guild.iconUrl ?: "https://cdn.discordapp.com/embed/avatars/0.png"
                img(src = iconUrl, alt = i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.GuildIconAlt(guild.name)), classes = "round-corners")

                h1 {
                    text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.Title))
                }
            }

            h2 {
                text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.Rules.RequirementsAndRules))
            }

            div {
                style = "display: flex; gap: 24px; flex-direction: column;"

                div {
                    div {
                        style = "font-weight: bold;"
                        text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.Rules.UseLorittaFrequently.Title))
                    }

                    div {
                        text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.Rules.UseLorittaFrequently.Description))
                    }
                }

                div {
                    div {
                        style = "font-weight: bold;"
                        text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.Rules.ParticipateOnThePartnerServer.Title))
                    }

                    div {
                        text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.Rules.ParticipateOnThePartnerServer.Description))
                    }
                }

                div {
                    div {
                        style = "font-weight: bold;"
                        text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.Rules.BeHonest.Title))
                    }
                    div {
                        text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.Rules.BeHonest.Description))
                    }
                }

                div {
                    div {
                        style = "font-weight: bold;"
                        text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.Rules.BeRespectful.Title))
                    }
                    div {
                        text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.Rules.BeRespectful.Description))
                    }
                }

                div {
                    div {
                        style = "font-weight: bold;"
                        text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.Rules.BeAuthentic.Title))
                    }
                    text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.Rules.BeAuthentic.Description))
                }
            }

            div {
                style = "font-weight: bold; font-size: 24px;"
                text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.GoodLuck))
                text(" ")
                img(src = "https://stuff.loritta.website/emotes/lori-lick.gif", classes = "discord-inline-emoji") {}
            }
        }

        hr {}

        // Invite Link field
        fieldWrapper {
            fieldInformationBlock {
                fieldTitle {
                    text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.InviteLink.Title))
                }
                fieldDescription {
                    text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.InviteLink.Description))
                }
            }

            textInput {
                val vanityCode = guild.vanityCode

                attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/loritta-partners/invite"
                attributes["bliss-swap:200"] = "body (innerHTML) -> #invite-result (innerHTML)"
                attributes["bliss-include-json"] = "[name='inviteId']"
                attributes["bliss-trigger"] = "input, load"
                attributes["bliss-coerce-to-null-if-blank"] = "true"
                attributes["loritta-partner-app-attribute"] = "true"

                name = "inviteId"

                if (vanityCode != null) {
                    value = "https://discord.gg/$vanityCode"
                }

                placeholder = "https://discord.gg/loritta"
            }

            div {
                id = "invite-result"
                text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.Loading))
            }
        }

        // Server Purpose field (500 chars)
        fieldWrapper {
            fieldInformationBlock {
                fieldTitle {
                    text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.ServerPurpose.Title))
                }
                fieldDescription {
                    text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.ServerPurpose.Description))
                }
            }
            textArea {
                name = "serverPurpose"
                attributes["maxlength"] = PartnerApplicationsUtils.FIELD_CHARACTER_LIMIT.toString()
                attributes["loritta-partner-app-attribute"] = "true"
                attributes["rows"] = "5"
            }
            characterCounter("[name='serverPurpose']")
        }

        // Why Partner field (500 chars)
        fieldWrapper {
            fieldInformationBlock {
                fieldTitle {
                    text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.WhyPartner.Title))
                }
                fieldDescription {
                    text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.WhyPartner.Description))
                }
            }
            textArea {
                name = "whyPartner"
                attributes["maxlength"] = PartnerApplicationsUtils.FIELD_CHARACTER_LIMIT.toString()
                attributes["loritta-partner-app-attribute"] = "true"
                attributes["rows"] = "5"
            }
            characterCounter("[name='whyPartner']")
        }

        // Submit button
        fieldWrapper {
            discordButton(ButtonStyle.PRIMARY) {
                attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/loritta-partners/form"
                attributes["bliss-include-json"] = "[loritta-partner-app-attribute='true']"
                attributes["bliss-disable-when"] = "[name=inviteId] == blank || [name=serverPurpose] == blank || [name=whyPartner] == blank"
                attributes["bliss-swap:200"] = "body (innerHTML) -> #right-sidebar-contents (innerHTML)"

                text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.Submit))
            }
        }
    }
}