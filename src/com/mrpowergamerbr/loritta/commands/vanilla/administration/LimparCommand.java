package com.mrpowergamerbr.loritta.commands.vanilla.administration;

import java.util.Arrays;
import java.util.List;

import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;

import net.dv8tion.jda.core.Permission;

public class LimparCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "limpar";
	}

	@Override
	public String getDescription() {
		return "Limpa o chat do canal de texto atual.";
	}
	
	@Override
	public String getUsage() {
		return "QuantasMensagens";
	}
	
	@Override
	public List<String> getExample() {
		return Arrays.asList("10", "25");
	}
	
	@Override
	public CommandCategory getCategory() {
		return CommandCategory.ADMIN;
	}
	
	@Override
	public void run(CommandContext context) {
		if (context.getHandle().hasPermission(Permission.MANAGE_SERVER)) {
			int toClear = Integer.parseInt(context.getArgs()[0]);
			context.getEvent().getTextChannel().getHistory().retrievePast(toClear).complete().forEach((msg) -> msg.delete().complete());
			
			context.sendMessage("Chat limpo por " + context.getHandle().getAsMention() + "!");
		} else {
			// Sem permissão
		}
	}
}
