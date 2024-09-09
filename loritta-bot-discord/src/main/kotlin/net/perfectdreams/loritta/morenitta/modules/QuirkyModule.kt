package net.perfectdreams.loritta.morenitta.modules

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.LorittaBot.Companion.RANDOM
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.MiscellaneousConfig
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`.SummonCommand
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.morenitta.utils.chance
import net.perfectdreams.loritta.morenitta.utils.extensions.addReaction

class QuirkyModule(val loritta: LorittaBot) : MessageReceivedModule {
    val config = loritta.config.loritta.quirky

    override suspend fun matches(
        event: LorittaMessageEvent,
        lorittaUser: LorittaUser,
        lorittaProfile: Profile?,
        serverConfig: ServerConfig,
        locale: BaseLocale,
        i18nContext: I18nContext
    ): Boolean {
        val miscellaneousConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<MiscellaneousConfig?>(loritta, ServerConfig::miscellaneousConfig)

        return miscellaneousConfig?.enableQuirky == true && event.guild?.selfMember?.hasPermission(event.channel as GuildChannel, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EXT_EMOJI, Permission.MESSAGE_HISTORY, Permission.MESSAGE_SEND) == true && event.message.type == MessageType.DEFAULT
    }

    override suspend fun handle(
        event: LorittaMessageEvent,
        lorittaUser: LorittaUser,
        lorittaProfile: Profile?,
        serverConfig: ServerConfig,
        locale: BaseLocale,
        i18nContext: I18nContext
    ): Boolean {
        // uwu u are sooo quirky
        val message = event.message

        if (config.randomReactions.enabled) {
            val reactionRandom = RANDOM.nextInt(0, config.randomReactions.maxBound)

            config.randomReactions.reactions.getOrNull(reactionRandom)?.let {
                // Let = "Vamos apenas pegar se NÃO for nulo", ou seja:
                // Se o valor na randomReactions.reactions.getOrNull(reactionRandom) NÃO for nulo, nós iremos adicionar a reação
                // Caso seja nulo, nada irá acontecer.
                message.addReaction(it).queue()
            }

            for (contextAware in config.randomReactions.contextAwareReactions) {
                if (chance(contextAware.chanceOf)) {
                    if (event.message.contentRaw.matches(Regex(contextAware.match))) {
                        message.addReaction(contextAware.reactions.random()).queue()
                        break
                    }
                }
            }
        }

        if (config.tioDoPave.enabled && chance(config.tioDoPave.chance))
            event.channel.sendMessage("${event.author.asMention} ${SummonCommand.TioDoPaveExecutor.PIADAS.random()} <:lori_ok_hand:426183783008698391>").queue()

        if ((event.message.contentRaw.contains("esta é uma mensagem do criador", true) && event.message.contentRaw.contains("se tornou muito lenta", true) && event.message.contentRaw.contains("que não enviarem essa mensagem dentro de duas semanas", true)) || (event.message.contentRaw.contains("deve fechar", true) && event.message.contentRaw.contains("Vamos enviar esta mensagem para ver se os membros", true) && event.message.contentRaw.contains("isto é de acordo com o criador", true)))
            event.channel.sendMessage("${event.author.asMention} agora me diga... porque você acha que o Discord ia avisar algo importante assim com uma CORRENTE? Isso daí é fake, se isso fosse verdade, o Discord iria colocar um aviso nas redes sociais e ao iniciar o Discord, apenas ignore tais mensagens... e por favor, pare de espalhar \uD83D\uDD17 correntes \uD83D\uDD17, não quero que aqui vire igual ao WhatsApp. <:smol_lori_putassa:395010059157110785>").queue()

        if (event.message.contentRaw.contains("DDoSed", true) && event.message.contentRaw.contains("pedidos de amizade para usuários aleatórios", true) && event.message.contentRaw.contains("tornando uma vítima também", true))
            event.channel.sendMessage("${event.author.asMention} você acha mesmo que se um usuário tivesse fazendo isto no Discord, ele já não teria sido suspenso em todo o Discord? Antes de compartilhar \uD83D\uDD17 correntes \uD83D\uDD17, pense um pouco sobre elas antes de mandar isto para vários usuários, não quero que aqui vire igual ao WhatsApp. <:smol_lori_putassa:395010059157110785>").queue()

        return false
    }
}