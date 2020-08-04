package net.perfectdreams.loritta.plugin.loriguildstuff.commands

import com.mrpowergamerbr.loritta.commands.vanilla.administration.AdminUtils.ModerationConfigSettings
import com.mrpowergamerbr.loritta.commands.vanilla.administration.BanCommand
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.discordCommand

object FastBanCommand {

    private val punishmentReasons = hashMapOf(
            "spam" to "Floodar/spammar (Enviar várias mensagens repetidas, enviar uma mensagem com caracteres aletórios, adicionar reações aleatórias, etc) nos canais de texto.",
            "div" to "Não é permitido divulgar conteúdos em canais de texto sem que a equipe permita.",
            "divdm" to "Enviar conteúdo (não solicitado!) via mensagem direta, fazer spam (ou seja, mandar conteúdo indesejado para outras pessoas) é contra as regras do servidor da Loritta e dos termos de uso do Discord e, caso continuar, você poderá ser suspenso do Discord e irá perder a sua conta!",
            "nsfw" to "É proibido compartilhar conteúdo NSFW (coisas obscenas como pornografia, gore e coisas relacionadas), conteúdo sugestivo, jumpscares, conteúdo de ódio, racismo, assédio, links com conteúdo ilegal e links falsos. Será punido até se passar via mensagem direta, até mesmo se a outra pessoa pedir.",
            "toxic" to "Ser tóxico (irritar e desrespeitar) com outros membros do servidor. Aprenda a respeitar e conviver com outras pessoas!",
            "owo" to "Apenas um teste ^-^"
    )

    fun create(loritta: LorittaDiscord) = discordCommand(loritta, listOf("b", "fastban"), CommandCategory.ADMIN) {
        this.hideInHelp = true
        this.commandCheckFilter {lorittaMessageEvent, _, _, _, _ ->
            lorittaMessageEvent.guild?.idLong == 297732013006389252L
        }

        executesDiscord {
            val role = guild.getRoleById(351473717194522647L)!!
            val author = this.member!!
            val userToBePunished = this.user(0)
            val reason = args.getOrNull(1)?.toLowerCase()

            if (!author.roles.contains(role)) {
                reply(
                        LorittaReply(
                                "Você não tem permissão para usa este comando!",
                                Constants.ERROR
                        )
                )
                return@executesDiscord
            }
            if (userToBePunished == null) {
                reply(
                        LorittaReply(
                                "Usuário inválido!",
                                Constants.ERROR
                        )
                )
                return@executesDiscord
            }
            if (reason == null) {
                reply(
                        LorittaReply(
                                "Você precisa inserir um motivo!",
                                "<a:lori_pat:706263175892566097>"
                        )
                )
                return@executesDiscord
            }

            var fancyReason = punishmentReasons[reason]

            if (fancyReason != null) {
                reply(
                        LorittaReply(
                                "O usuário `${userToBePunished.asMention}` está sendo punido por `$fancyReason`!",
                                "<a:lori_happy:521721811298156558>"
                        )
                )
            }

            val proof = args.getOrNull(2)

            if (proof != null) {
                fancyReason = "[$fancyReason]($proof)"
            }

            BanCommand.ban(
                    ModerationConfigSettings(sendPunishmentViaDm = true, sendPunishmentToPunishLog = true),
                    guild,
                    author.user,
                    LegacyBaseLocale(),
                    userToBePunished.handle,
                    fancyReason!!,
                    false,
                    0
            )
        }
    }
}