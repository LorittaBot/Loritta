package com.mrpowergamerbr.loritta.whistlers;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.core.entities.Message;

@AllArgsConstructor
@NoArgsConstructor
public class ReactionCode implements ICode {
	String reaction;
	boolean isCustom;

	public void handle(Message message) {
		// 🌍
		if (isCustom) {
			// Caso o emoji seja custom...
			message.addReaction(message.getGuild().getEmotesByName(reaction, true).get(0)).complete();
		} else {
			message.addReaction(reaction).complete();
		}
	}
}
