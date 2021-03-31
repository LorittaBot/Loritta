package com.mrpowergamerbr.loritta.whistlers;

import com.mrpowergamerbr.loritta.userdata.ServerConfig;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.core.entities.Message;

@AllArgsConstructor
@NoArgsConstructor
public class ContainsPrecondition implements IPrecondition {
	String contains;
	boolean ignoreCase;
	
	@Override
	public boolean isValid(ServerConfig conf, Message message) {
		String msg = (ignoreCase ? message.getContentDisplay().toLowerCase() : message.getContentDisplay());
		return (msg.contains(ignoreCase ? contains.toLowerCase() : contains));
	}

}
