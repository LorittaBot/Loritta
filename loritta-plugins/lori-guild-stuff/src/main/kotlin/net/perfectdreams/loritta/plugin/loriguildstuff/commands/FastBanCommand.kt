package net.perfectdreams.loritta.plugin.loriguildstuff.commands

import com.mrpowergamerbr.loritta.commands.vanilla.administration.AdminUtils
import com.mrpowergamerbr.loritta.commands.vanilla.administration.BanCommand
import com.mrpowergamerbr.loritta.utils.extensions.await
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.discordCommand
import net.perfectdreams.loritta.utils.Emotes

object FastBanCommand {

    private val punishmentReasons = hashMapOf(
            "spam" to "Floodar/spammar (Enviar várias mensagens repetidas, enviar uma mensagem com caracteres aletórios, adicionar reações aleatórias, etc) nos canais de texto.",
            "div" to "Não é permitido divulgar conteúdos em canais de texto sem que a equipe permita.",
            "divdm" to "Enviar conteúdo (não solicitado!) via mensagem direta, fazer spam (ou seja, mandar conteúdo indesejado para outras pessoas) é contra as regras do servidor da Loritta e dos termos de uso do Discord e, caso continuar, você poderá ser suspenso do Discord e irá perder a sua conta!",
            "nsfw" to "É proibido compartilhar conteúdo NSFW (coisas obscenas como pornografia, gore e coisas relacionadas), conteúdo sugestivo, jumpscares, conteúdo de ódio, racismo, assédio, links com conteúdo ilegal e links falsos. Será punido até se passar via mensagem direta, até mesmo se a outra pessoa pedir.",
            "toxic" to "Ser tóxico (irritar e desrespeitar) com outros membros do servidor. Aprenda a respeitar e conviver com outras pessoas!",
            "under13" to "É proibido ter uma conta de Discord caso você tenha menos de 13 anos!",
            "owo" to "Apenas um teste uwu owo",
            "terms" to "Comercialização de produtos com valores monetários reais em troca de sonhos.",
            "bob" to "Imagine fazer spam dizendo que não é para fazer spam."
    )

    fun create(loritta: LorittaDiscord) = discordCommand(loritta, listOf("b", "fastban"), CommandCategory.MODERATION) {
        this.hideInHelp = true
        this.commandCheckFilter {lorittaMessageEvent, _, _, _, _ ->
            lorittaMessageEvent.guild?.idLong == 297732013006389252L
        }

        executesDiscord {
            val author = this.member!!
            val staffRole = guild.getRoleById(351473717194522647L)!!
            val supportRole = guild.getRoleById(399301696892829706L)!!
            if (!author.roles.contains(staffRole)) {
                fail("Você não pode usar o meu super comandinho de banir as pessoas com motivos bonitinhos ;w;")
            }

            val userToBePunished = this.user(0)?.handle ?: fail("Usuário inválido!")
            val reason = this.args.getOrNull(1)?.toLowerCase() ?: fail("Motivos disponíveis: `${punishmentReasons.keys.joinToString(", ")}`!")
            val proof = this.args.getOrNull(2)
            var fancyReason = punishmentReasons.getOrDefault(reason, null) ?: fail("Motivo inválido. Motivos disponíveis: `${punishmentReasons.keys.joinToString(", ")}`!")

            if (userToBePunished == this.user) {
                reply(
                    LorittaReply(
                        "Você quer mesmo se banir? Você não iria ser mais meu guarda-costas....",
                        Emotes.LORI_RAGE
                    )
                )
                return@executesDiscord
            }

            try {
                val member = guild.retrieveMember(userToBePunished).await()

                if (member.roles.contains(staffRole) || member.roles.contains(supportRole)) {
                    reply(
                        LorittaReply(
                            "Você não pode realizar um golpe de estado!",
                            Emotes.LORI_CRYING
                        )
                    )
                    return@executesDiscord
                }
            } catch(e: Exception) { }

            val settings = AdminUtils.retrieveModerationInfo(serverConfig)

            reply(
                    LorittaReply(
                            "Punindo `${userToBePunished.asTag}` por `$fancyReason`!",
                            "<a:lori_happy:521721811298156558>"
                    )
            )

            if (proof != null) fancyReason = "[$fancyReason]($proof)"

            BanCommand.ban(
                    settings,
                    this.guild,
                    author.user,
                    locale,
                    userToBePunished,
                    fancyReason,
                    false,
                    0
            )
        }
    }
}
