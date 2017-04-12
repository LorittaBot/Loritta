package com.mrpowergamerbr.loritta.whistlers;

import com.mrpowergamerbr.loritta.userdata.ServerConfig;

import net.dv8tion.jda.core.entities.Message;

public interface IPrecondition {
	public boolean isValid(ServerConfig conf, Message message);
}
