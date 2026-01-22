package net.perfectdreams.loritta.morenitta.lorittapartners

import dev.minn.jda.ktx.interactions.components.Thumbnail
import dev.minn.jda.ktx.messages.InlineMessage
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.components.separator.Separator
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.asUserNameCodeBlockPreviewTag
import net.perfectdreams.loritta.morenitta.utils.extensions.convertToUserNameCodeBlockPreviewTag
import net.perfectdreams.loritta.partnerapplications.PartnerApplicationResult
import java.time.Instant
import kotlin.time.Duration.Companion.days

object PartnerApplicationsUtils {
    val APPLICATION_COOLDOWN = 30.days
    const val FIELD_CHARACTER_LIMIT = 500
    const val MINIMUM_GUILD_MEMBERS_COUNT = 5_000

    fun InlineMessage<*>.createStaffApplicationMessage(
        loritta: LorittaBot,
        applicationData: PartnerApplicationData,
        submittedByUser: CachedUserInfo,
        guildInfo: PartnerGuildInfo?
    ) {
        this.useComponentsV2 = true

        this.text("<@&${loritta.config.loritta.partnerApplications.roleId}>")

        container {
            this.accentColorRaw = when (applicationData.result) {
                PartnerApplicationResult.PENDING -> LorittaColors.LorittaAqua.rgb
                PartnerApplicationResult.APPROVED -> LorittaColors.BanAppealApproved.rgb
                PartnerApplicationResult.DENIED -> LorittaColors.BanAppealRejected.rgb
            }

            val guildIconUrl = guildInfo?.iconUrl

            val text = buildString {
                appendLine("### 🤝 Candidatura de Parceria")
                appendLine()
                appendLine("**Servidor:** ${guildInfo?.name ?: "Desconhecido"} (`${applicationData.guildId}`)")
                appendLine("**Membros:** ${guildInfo?.memberCount ?: "???"}")
                appendLine("**Convite:** https://discord.gg/${applicationData.inviteLink}")
                appendLine("**Dono do Servidor:** ${guildInfo?.ownerId ?: "???"}")
            }

            if (guildIconUrl != null) {
                section(Thumbnail(guildIconUrl)) {
                    text(text)
                }
            } else {
                text(text)
            }

            text(buildString {
                appendLine("**Qual é o propósito do seu servidor?**")
                appendLine(applicationData.serverPurpose)
                appendLine()
                appendLine("**Por que seu servidor deveria se tornar parceiro da Loritta?**")
                appendLine(applicationData.whyPartner)
            })

            this.separator(isDivider = true, spacing = Separator.Spacing.LARGE)

            this.section(Thumbnail(submittedByUser.effectiveAvatarUrl)) {
                text("### Enviado por ${convertToUserNameCodeBlockPreviewTag(submittedByUser.id, submittedByUser.name, submittedByUser.globalName, submittedByUser.discriminator, stripCodeMarksFromInput = false, stripLinksFromInput = false)} (${applicationData.submitterPermissionLevel})")
            }

            this.text("-# Candidatura #${applicationData.id}")
        }

        val alreadyReviewed = applicationData.result != PartnerApplicationResult.PENDING

        if (alreadyReviewed) {
            container {
                this.accentColorRaw = LorittaColors.LorittaAqua.rgb

                text(
                    buildString {
                        when (applicationData.result) {
                            PartnerApplicationResult.PENDING -> error("This should never happen!")
                            PartnerApplicationResult.APPROVED -> {
                                appendLine("**<@${applicationData.reviewedBy}> (`${applicationData.reviewedBy}`) aprovou esta candidatura em ${DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(applicationData.reviewedAt!!.toInstant())}**")
                            }
                            PartnerApplicationResult.DENIED -> {
                                appendLine("**<@${applicationData.reviewedBy}> (`${applicationData.reviewedBy}`) rejeitou esta candidatura em ${DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(applicationData.reviewedAt!!.toInstant())}**")
                                appendLine("**Motivo:** ${applicationData.reviewerNotes}")
                            }
                        }
                    }
                )
            }
        }

        actionRow(
            Button.of(ButtonStyle.SUCCESS, "partner_accept:${applicationData.id}", "Aceitar")
                .withDisabled(alreadyReviewed),
            Button.of(ButtonStyle.DANGER, "partner_reject:${applicationData.id}", "Rejeitar")
                .withDisabled(alreadyReviewed)
        )
    }

    fun InlineMessage<*>.createApplicationReceivedMessage(
        loritta: LorittaBot,
        applicationId: Long
    ) {
        this.useComponentsV2 = true

        container {
            this.accentColorRaw = LorittaColors.BanAppealPending.rgb

            section(Thumbnail("https://assets.perfectdreams.media/loritta/loritta-support.png")) {
                text(
                    buildString {
                        appendLine("### Recebemos a sua candidatura!")

                        appendLine("Recebemos a sua candidatura! Em breve você terá uma resposta dizendo se a sua candidatura foi aprovada ou não.")
                        appendLine()
                        appendLine("**Boa sorte! ${Emotes.LoriLick}**")
                        appendLine()
                        appendLine("-# Candidatura #$applicationId")
                    }
                )
            }
        }
    }

    fun InlineMessage<*>.createApplicationAcceptedMessage(
        loritta: LorittaBot,
        applicationId: Long,
        acceptedByUser: User
    ) {
        this.useComponentsV2 = true

        container {
            this.accentColorRaw = LorittaColors.BanAppealApproved.rgb

            section(Thumbnail("https://stuff.loritta.website/emotes/lori-angel.png")) {
                text(buildString {
                    appendLine("### ${Emotes.LoriHappyJumping} Sua candidatura foi aceita!")
                    appendLine("Parabéns! Seu servidor foi aprovado como parceiro da Loritta!")
                    appendLine()
                    appendLine("Para entrar no servidor de parceiros, entre na seção de \"Loritta Partners\" no painel da Loritta.")
                    appendLine()
                    appendLine("**Bem-vindo aos parceiros da Loritta!** ${Emotes.LoriHeart}")
                    appendLine()
                    appendLine("Aprovado por ${acceptedByUser.asUserNameCodeBlockPreviewTag(stripCodeMarksFromInput = false, stripLinksFromInput = false)}")
                    appendLine()
                    appendLine("-# Candidatura #$applicationId")
                })
            }
        }
    }

    fun InlineMessage<*>.createApplicationDeniedMessage(
        loritta: LorittaBot,
        applicationId: Long,
        reason: String,
        canApplyAgainAfter: Instant
    ) {
        this.useComponentsV2 = true

        container {
            this.accentColorRaw = LorittaColors.BanAppealRejected.rgb

            section(Thumbnail("https://stuff.loritta.website/emotes/lori-sob.png")) {
                text(buildString {
                    appendLine("### ${Emotes.LoriSob} Sua candidatura foi rejeitada...")
                    appendLine("**Motivo:** $reason")
                    appendLine()
                    appendLine("Você poderá enviar outra candidatura em ${DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(canApplyAgainAfter)}.")
                    appendLine()
                    appendLine("-# Candidatura #$applicationId")
                })
            }
        }
    }
}
