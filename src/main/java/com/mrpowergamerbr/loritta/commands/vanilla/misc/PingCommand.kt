package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent

class PingCommand : CommandBase() {
    override fun getLabel(): String {
        return "ping"
    }

    override fun getDescription(): String {
        return "Um comando de teste para ver se eu estou funcionando, recomendo que você deixe isto ligado para testar!"
    }

    override fun run(context: CommandContext) {
        context.sendMessage(context.getAsMention(true) + "🏓 **Pong!** " + context.event.jda.ping + "ms (Shard ${context.event.jda.shardInfo.shardId})")
    }

    override fun onCommandReactionFeedback(context: CommandContext?, e: GenericMessageReactionEvent?, msg: Message) {
        msg.editMessage("kk eae men").complete();
    }
}