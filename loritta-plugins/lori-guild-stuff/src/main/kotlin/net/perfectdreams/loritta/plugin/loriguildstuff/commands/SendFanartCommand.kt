package net.perfectdreams.loritta.plugin.loriguildstuff.commands

import com.mrpowergamerbr.loritta.utils.extensions.await
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.discordCommand

object SendFanartCommand {
    fun create(loritta: LorittaDiscord) = discordCommand(loritta, listOf("enviarfanart", "sendfanart"), CommandCategory.MISC) {
        this.hideInHelp = true
        this.commandCheckFilter { lorittaMessageEvent, _, _, _, _ ->
            lorittaMessageEvent.guild?.idLong == 297732013006389252L
        }

        executesDiscord {
            val role = guild.getRoleById(589159900379086868L)!!
            val member = this.member!!

            if (member.roles.contains(role)) {
                reply(
                        LorittaReply(
                                "Que pena, pensei que você queria enviar a sua fan art para mim!",
                                "<a:bongo_lori_triste:524894216510373888>"
                        )
                )
            } else {
                guild.addRoleToMember(member, role).await()
                reply(
                        LorittaReply(
                                "Agora você pode enviar a sua fanart para mim no <#738918304063815751>! Não se esqueça de ler as <#738918304063815751> antes!",
                                "<a:super_lori_happy:524893994874961940>"
                        )
                )
            }
        }
    }
}