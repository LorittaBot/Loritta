package com.mrpowergamerbr.loritta.commands.vanilla.minecraft;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.Charsets;

import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;

public class OfflineUUIDCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "offlineuuid";
	}

	public String getDescription() {
		return "Pega a UUID offline (ou seja, de servidores sem autenticação da Mojang) de um player";
	}
	
	public CommandCategory getCategory() {
		return CommandCategory.MINECRAFT;
	}
	
	public String getUsage() {
		return "nickname";
	}

	public List<String> getExample() {
		return Arrays.asList("Monerk");
	}
	
	@Override
	public void run(CommandContext context) {
		if (context.getArgs().length == 1) {			
			
			UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + context.getArgs()[0]).getBytes(Charsets.UTF_8));
			
			context.sendMessage(context.getAsMention(true) + "UUID offline (sem autenticação da Mojang) de " + context.getArgs()[0] + ": `" + uuid.toString() + "`");
		} else {
			context.explain();
		}
	}

}