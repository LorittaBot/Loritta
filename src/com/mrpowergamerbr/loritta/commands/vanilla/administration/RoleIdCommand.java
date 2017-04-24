package com.mrpowergamerbr.loritta.commands.vanilla.administration;

import java.util.Arrays;
import java.util.List;

import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;

public class RoleIdCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "roleid";
	}

	@Override
	public String getDescription() {
		return "Pega o ID de um cargo do Discord";
	}
	
	@Override
	public String getUsage() {
		return "CargoMencionado";
	}
	
	@Override
	public List<String> getExample() {
		return Arrays.asList("@Moderadores");
	}
	
	@Override
	public void run(CommandContext context) {
		if (context.getHandle().hasPermission(Permission.MANAGE_SERVER)) {
			for (Role r : context.getMessage().getMentionedRoles()) {
				context.sendMessage(context.getAsMention(true) + r.getId());
			}
		} else {
			// Sem permissão
		}
	}
}
