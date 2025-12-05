package net.perfectdreams.loritta.morenitta.banappeals

import dev.minn.jda.ktx.interactions.components.MediaGalleryItem
import dev.minn.jda.ktx.interactions.components.Thumbnail
import dev.minn.jda.ktx.messages.InlineMessage
import kotlinx.datetime.toJavaInstant
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.components.separator.Separator
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.banappeals.BanAppealResult
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.BanAppeals
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.asUserNameCodeBlockPreviewTag
import net.perfectdreams.loritta.morenitta.utils.extensions.convertToUserNameCodeBlockPreviewTag
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import java.time.Instant
import kotlin.time.Duration.Companion.days

object BanAppealsUtils {
    val BAN_APPEAL_COOLDOWN = 30.days

    suspend fun getCachedUserInfoForAppeal(m: LorittaBot, appeal: BanAppeal): AppealCachedUserInfo {
        if (appeal.submittedBy == appeal.userId) {
            val userInfo = m.lorittaShards.retrieveUserInfoById(appeal.userId)

            return AppealCachedUserInfo(userInfo, userInfo)
        } else {
            return AppealCachedUserInfo(
                m.lorittaShards.retrieveUserInfoById(appeal.submittedBy),
                m.lorittaShards.retrieveUserInfoById(appeal.userId),
            )
        }
    }

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

    fun InlineMessage<*>.createAppealReceivedMessage(
        loritta: LorittaBot,
        appealId: Long
    ) {
        this.useComponentsV2 = true

        container {
            this.accentColorRaw = LorittaColors.BanAppealPending.rgb

            section(Thumbnail("https://assets.perfectdreams.media/loritta/loritta-support.png")) {
                text(
                    buildString {
                        appendLine("### Recebemos o seu apelo!")

                        appendLine("Recebemos o seu apelo de ban! Em breve você terá uma resposta dizendo se o seu apelo foi aprovado ou não.")
                        appendLine()
                        appendLine("Se você precisar de ajuda com o seu apelo, você pode falar com a nossa equipe no [Tribunal da Loritta](${loritta.config.loritta.banAppeals.supportInviteUrl}). Lembrando que o servidor serve você tirar dúvidas ou para dar mais informações para a equipe, e não para você ir pedir para verem o seu apelo mais rápido.")
                        appendLine()
                        appendLine("**Boa sorte! ${Emotes.LoriLick}**")
                        appendLine()
                        appendLine("-# Apelo #$appealId")
                    }
                )
            }
        }
    }

    fun InlineMessage<*>.createAppealAcceptedMessage(
        loritta: LorittaBot,
        appealId: Long,
        acceptedByUser: User
    ) {
        this.useComponentsV2 = true

        container {
            this.accentColorRaw = LorittaColors.BanAppealApproved.rgb

            section(Thumbnail("https://stuff.loritta.website/emotes/lori-angel.png")) {
                text(
                    buildString {
                        appendLine("### ${Emotes.LoriHappyJumping} Seu apelo foi aceito!")

                        appendLine("Em breve você poderá usar a Loritta novamente!")
                        appendLine()
                        appendLine("* Se você foi banido do SparklyPower (Servidor de Minecraft da Loritta), você terá que entrar no servidor do SparklyPower para pedir o unban da sua conta.")
                        appendLine("* Se você foi banido do servidor da Comunidade da Loritta no Discord ou o Servidor do SparklyPower no Discord, você terá que entrar no servidor do Tribunal da Loritta para pedir unban nos servidores.")
                        appendLine("* Se passar alguns minutos e você ainda não for desbanido da Loritta, talvez quem aprovou o seu apelo esqueceu de te desbanir... Neste caso, basta você entrar no servidor do Tribunal da Loritta e abrir um ticket de suporte, junto com o ID do seu apelo.")
                        appendLine()
                        appendLine("**Que bom ter você de volta, a Loritta sentiu saudades.** ${Emotes.LoriHeart}")
                        appendLine()
                        appendLine("O seu apelo foi aprovado por ${acceptedByUser.asUserNameCodeBlockPreviewTag(stripCodeMarksFromInput = false, stripLinksFromInput = false)}")
                        appendLine()
                        appendLine("-# Apelo #$appealId")
                    }
                )
            }
        }

        actionRow(
            Button.of(
                ButtonStyle.LINK,
                "https://discord.gg/loritta",
                "Servidor da Comunidade da Loritta",
            ).withEmoji(Emotes.LoriLick.toJDA()),
            Button.of(
                ButtonStyle.LINK,
                "https://discord.gg/sparklypower",
                "Servidor do SparklyPower",
            ).withEmoji(Emotes.PantufaLick.toJDA()),
            Button.of(
                ButtonStyle.LINK,
                loritta.config.loritta.banAppeals.supportInviteUrl,
                "Tribunal da Loritta",
            ).withEmoji(Emotes.LoriBanHammer.toJDA())
        )
    }

    fun InlineMessage<*>.createAppealDeniedMessage(
        loritta: LorittaBot,
        appealId: Long,
        reason: String,
        canSendAppealAfter: Instant
    ) {
        this.useComponentsV2 = true

        container {
            this.accentColorRaw = LorittaColors.BanAppealRejected.rgb

            section(Thumbnail("https://stuff.loritta.website/emotes/lori-sob.png")) {
                text(
                    buildString {
                        appendLine("### ${Emotes.LoriSob} Seu apelo foi rejeitado...")
                        appendLine("**Motivo:** $reason")
                        appendLine()
                        appendLine("Você poderá enviar outro apelo em ${DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(canSendAppealAfter)}.")
                        appendLine()
                        appendLine("Se você acha que a rejeição foi injusta, ou se você acha que faltou mais contexto sobre o seu apelo, você pode entrar no [Tribunal da Loritta](${loritta.config.loritta.banAppeals.supportInviteUrl}) e abrir um ticket de suporte para explicar mais sobre a situação.")
                        appendLine()
                        appendLine("-# Apelo #$appealId")
                    }
                )
            }
        }
    }

    data class AppealCachedUserInfo(
        val submittedBy: CachedUserInfo?,
        val appeal: CachedUserInfo?
    )
}