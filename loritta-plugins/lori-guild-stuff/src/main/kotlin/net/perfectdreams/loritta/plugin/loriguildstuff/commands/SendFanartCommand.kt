package net.perfectdreams.loritta.plugin.loriguildstuff.commands

import com.mrpowergamerbr.loritta.utils.extensions.await
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.discordCommand

object SendFanartCommand {
    fun create(loritta: LorittaDiscord) = discordCommand(loritta, listOf("enviarfanart", "sendfanart"), CommandCategory.MISC) {
        this.hideInHelp = true
        this.commandCheckFilter { lorittaMessageEvent, _, _, _, _ ->
            lorittaMessageEvent.guild?.idLong == 297732013006389252L
        }

        executesDiscord {
            val role = guild.getRoleById(589159900379086868L)!!
            val firstFanartChannel = guild.getTextChannelById(589159642160955402L)!!
            val member = this.member!!

            if (member.roles.contains(role)) {
                if (discordMessage.textChannel == firstFanartChannel) {
                    if (discordMessage.attachments.isEmpty()) {
                        reply(
                                LorittaReply(
                                        "Apenas envie sua fanart, não precisa usar o comando novamente!",
                                        "<a:lori_pat:706263175892566097>"
                                )
                        )
                    }
                }
                if (discordMessage.textChannel != firstFanartChannel) {
                    if (discordMessage.attachments.isNotEmpty()) {
                        reply(
                                LorittaReply(
                                        "Você deve enviar sua fanart em <#589159642160955402>!",
                                        "<:lori_ameno:673868465433477126>"
                                )
                        )
                    } else {
                        guild.removeRoleFromMember(member, role).await()
                        reply(
                                LorittaReply(
                                        "Que pena, pensei que você queria enviar a sua fan art para mim!",
                                        "<a:bongo_lori_triste:524894216510373888>"
                                )
                        )
                    }
                }
            } else {
                guild.addRoleToMember(member, role).await()
                reply(
                        LorittaReply(
                                "Agora você pode enviar a sua fanart para mim no <#589159642160955402>! Não se esqueça de ler as <#557629480391409666> antes!",
                                "<a:super_lori_happy:524893994874961940>"
                        )
                )
            }
        }
    }
}