package net.perfectdreams.loritta.morenitta.listeners

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.extensions.await

class AddReactionFurryAminoPtListener(val loritta: LorittaBot) : ListenerAdapter() {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    val config = loritta.config.loritta.quirky

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (event.emoji.type != Emoji.Type.CUSTOM || event.emoji.asCustom().idLong != 593161404937404416L)
            return

        val securityRole = event.guild.getRoleById(FurryAmino.SECURITY_ROLE_ID)
        val adminRole = event.guild.getRoleById(FurryAmino.ADMINISTRATOR_ROLE_ID)

        val user = event.user ?: return // The user may not be cached
        val member = event.member ?: return

        if (member.roles.contains(securityRole) || member.roles.contains(adminRole)) {
            GlobalScope.launch {
                val message = event.channel.retrieveMessageById(event.messageIdLong).await()
                message.delete().queue()

                val registry = event.guild.getTextChannelById(FurryAmino.LOG_REGISTRY_ID)

                // Mandar no registro
                registry?.sendMessage("${Emotes.LORI_HEART} **Registro de ${message.author.asMention} (`${message.author.name}#${message.author.discriminator}`), aprovado por ${user.asMention}**\n\n${message.contentRaw}")
                    ?.queue()

                // Remover cargos e tals
                val furriesRole = event.guild.getRoleById(FurryAmino.FURRIES_ROLE_ID)!!

                event.guild.addRoleToMember(message.member!!, furriesRole).queue()

                try {
                    val channel = loritta.getOrRetrievePrivateChannelForUser(message.author)

                    val embed = EmbedBuilder()
                        .setColor(Constants.LORITTA_AQUA)
                        .setDescription("Você foi aprovado no *Furry Amino Português* e agora pode conversar em outros canais.\n\n${Emotes.LORI_TEMMIE} **Quer conversar?** Então acesse o <#646405223916765187> e converse com os outros membros do servidor!\n\n${Emotes.LORI_OWO} **Querendo conversar com outros artistas?** Então acesse o <#643831758563049476> e compartilhe seus work in progress e suas frustrações como artista lá!\n\n${Emotes.LORI_YAY} **Precisando de ajuda artística?** Então veja vários tutoriais em <#643831795506348042> para te ajudar a virar um artista melhor!\n\n${Emotes.LORI_RICH} **Procurando pessoas que fazem comissões ou quer anunciar as suas?** Então veja o canal de <#646392653641941003>!\n\nEspero que você se divirta no servidor! ${Emotes.LORI_PAT}")
                        .setImage("https://loritta.website/assets/img/lori_akira.png")

                    channel.sendMessageEmbeds(embed.build()).await()
                } catch (e: Exception) {}
            }
        }
    }
}