package com.mrpowergamerbr.loritta.commands.custom;

import java.util.ArrayList;
import java.util.List;

import com.mrpowergamerbr.loritta.listeners.DiscordListener;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.loritta.whistlers.ICode;

import lombok.*;
import lombok.experimental.Accessors;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Usado para criar comandos personalizados
 * 
 * NÃO é usado para comandos padrões da Loritta, para isso, veja {@link com.mrpowergamerbr.loritta.commands.CommandBase}
 */
@Setter
@Getter
@NoArgsConstructor
@Accessors(fluent = true)
public class CustomCommand {
	// yay reaproveitamento de código ;)
	// Normalmente apenas contém um CodeBlock
	private List<ICode> codes = new ArrayList<ICode>();
	
	// Nome do comando
	private String commandName;
	
	public boolean handle(MessageReceivedEvent ev, ServerConfig conf) {
		String message = ev.getMessage().getContentDisplay();
		if (message.startsWith(conf.commandPrefix() + commandName)) {
			ev.getChannel().sendTyping().complete();
			DiscordListener.processCode(conf, ev.getMessage(), codes);
			return true;
		}
		return false;
	}
}
