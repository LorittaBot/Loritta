package net.perfectdreams.loritta.plugin.loriguildstuff.commands

import com.mrpowergamerbr.loritta.utils.extensions.await
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.discordCommand
import org.jetbrains.kotlin.utils.doNothing

object SendFanartCommand {
    fun create(loritta: LorittaDiscord) = discordCommand(loritta, listOf("enviarfanart", "sendfanart"), CommandCategory.MISC) {
        this.hideInHelp = true
        this.commandCheckFilter { lorittaMessageEvent, _, _, _, _ ->
            lorittaMessageEvent.guild?.idLong == 297732013006389252L
        }

        executesDiscord {
            val role = guild.getRoleById(589159900379086868L)!!
            val firstFanartChannel = guild.getGuildChannelById(589159642160955402L)!!
            val member = this.member!!

            if (member.roles.contains(role)) {
                if (message.channel == firstFanartChannel && discordMessage.attachments.isEmpty()) {
                    reply(
                            LorittaReply(
                                    "Apenas envie sua fanart, não precisa usar o comando novamente!",
                                    "<a:lori_pat:706263175892566097>"
                            )
                    )
                } else if (message.channel !== firstFanartChannel && discordMessage.attachments.isNotEmpty()) {
                    reply(
                            LorittaReply(
                                    "Você deve enviar sua fanart em <#589159642160955402>!",
                                    "<:lori_ameno:673868465433477126>"
                            )
                    )
                } else if (message.channel == firstFanartChannel && discordMessage.attachments.isNotEmpty()) {
                    doNothing()
                }
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