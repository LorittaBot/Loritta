package net.perfectdreams.loritta.morenitta.banappeals

import dev.minn.jda.ktx.interactions.components.MediaGalleryItem
import dev.minn.jda.ktx.interactions.components.Thumbnail
import dev.minn.jda.ktx.messages.InlineMessage
import kotlinx.datetime.toJavaInstant
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.components.separator.Separator
import net.perfectdreams.loritta.banappeals.BanAppealResult
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.convertToUserNameCodeBlockPreviewTag
import kotlin.time.Duration.Companion.days

object BanAppealsUtils {
    val BAN_APPEAL_COOLDOWN = 30.days

    // data = the result row of the BanAppeals table
    fun InlineMessage<*>.createStaffAppealMessage(
        loritta: LorittaBot,
        data: BanAppeal,
        submittedByUserInfo: CachedUserInfo,
        appealUserInfo: CachedUserInfo
    ) {
        this.useComponentsV2 = true

        val alreadyReviewed = data.appealResult != BanAppealResult.PENDING

        this.text("<@&${loritta.config.loritta.banAppeals.roleId}>")

        container {
            this.accentColorRaw = when (data.appealResult) {
                BanAppealResult.PENDING -> LorittaColors.LorittaAqua.rgb
                BanAppealResult.APPROVED -> LorittaColors.BanAppealApproved.rgb
                BanAppealResult.DENIED -> LorittaColors.BanAppealRejected.rgb
            }

            text(
                buildString {
                    appendLine("### \uD83D\uDE93 Apelo de Ban")

                    appendLine("**Qual foi o motivo do seu banimento?**")
                    appendLine(data.whatDidYouDo)

                    appendLine()
                    appendLine("**Por que você quebrou as regras da Loritta?**")
                    appendLine(data.whyDidYouBreakThem)

                    appendLine()
                    appendLine("**Quais são os IDs das suas contas do Discord?**")
                    if (data.accountIds.isNotEmpty()) {
                        appendLine("```")
                        for (accountId in data.accountIds) {
                            appendLine(accountId)
                        }
                        appendLine("```")
                    } else {
                        appendLine("*Nenhuma conta adicional*")
                    }

                    appendLine()
                    appendLine("**Por que você deveria ser desbanido?**")
                    appendLine(data.whyShouldYouBeUnbanned)

                    appendLine()
                    appendLine("**Deseja comentar mais alguma coisa sobre o seu ban?**")
                    appendLine(data.additionalComments)
                }
            )

            if (data.files.isNotEmpty()) {
                mediaGallery(data.files.map { MediaGalleryItem("https://stuff.loritta.website${it}") })
            }

            this.separator(isDivider = true, spacing = Separator.Spacing.LARGE)

            this.section(Thumbnail(appealUserInfo.effectiveAvatarUrl)) {
                text(
                    buildString {
                        appendLine("### Informações de ${convertToUserNameCodeBlockPreviewTag(appealUserInfo.id, appealUserInfo.name, appealUserInfo.globalName, appealUserInfo.discriminator, stripCodeMarksFromInput = false, stripLinksFromInput = false)}")
                        appendLine("**Banido por:** <@${data.banEntry.bannedBy?.value}> (`${data.banEntry.bannedBy?.value}`)")
                        appendLine("**Motivo:** ${data.banEntry.reason}")
                        appendLine("**Banido desde:** ${DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(data.banEntry.bannedAt.toJavaInstant())}")
                        if (data.banEntry.staffNotes != null) {
                            appendLine("**Notas da Equipe:** ${data.banEntry.staffNotes}")
                        }
                    }
                )
            }

            // Only show the "Appeal sent by" section if the user is NOT sent by the same user
            if (data.submittedBy != data.userId) {
                this.separator(isDivider = true, spacing = Separator.Spacing.LARGE)

                this.section(Thumbnail(submittedByUserInfo.effectiveAvatarUrl)) {
                    text("### Apelo enviado por ${convertToUserNameCodeBlockPreviewTag(submittedByUserInfo.id, submittedByUserInfo.name, submittedByUserInfo.globalName, submittedByUserInfo.discriminator, stripCodeMarksFromInput = false, stripLinksFromInput = false)}")
                }
            }

            this.text(
                buildString {
                    appendLine("-# Apelo #${data.id}")
                }
            )
        }

        if (alreadyReviewed) {
            container {
                this.accentColorRaw = LorittaColors.LorittaAqua.rgb

                text(
                    buildString {
                        when (data.appealResult) {
                            BanAppealResult.PENDING -> error("This should never happen!")
                            BanAppealResult.APPROVED -> {
                                appendLine("**<@${data.reviewedBy}> (`${data.reviewedBy}`) aprovou este apelo em ${DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(data.reviewedAt!!.toInstant())}**")
                            }
                            BanAppealResult.DENIED -> {
                                appendLine("**<@${data.reviewedBy}> (`${data.reviewedBy}`) rejeitou este apelo em ${DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(data.reviewedAt!!.toInstant())}**")
                                appendLine("**Motivo:** ${data.reviewerNotes}")
                            }
                        }
                    }
                )
            }
        }

        actionRow(
            Button.of(
                ButtonStyle.PRIMARY,
                "appeal_accept:${data.id}",
                "Aceitar"
            ).withDisabled(alreadyReviewed),
            Button.of(
                ButtonStyle.SECONDARY,
                "appeal_reject:${data.id}",
                "Rejeitar"
            ).withDisabled(alreadyReviewed)
        )
    }
}