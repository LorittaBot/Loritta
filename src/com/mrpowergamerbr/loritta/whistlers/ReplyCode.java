package com.mrpowergamerbr.loritta.whistlers;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.core.entities.TextChannel;

@AllArgsConstructor
@NoArgsConstructor
public class ReplyCode implements ICode {
	String message;
	
	public void handle(TextChannel tc) {
		tc.sendMessage(message).queue();
	}
}
