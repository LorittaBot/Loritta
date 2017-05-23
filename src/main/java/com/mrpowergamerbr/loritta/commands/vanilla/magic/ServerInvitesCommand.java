package com.mrpowergamerbr.loritta.commands.vanilla.magic;

import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;

import net.dv8tion.jda.core.entities.Invite;

public class ServerInvitesCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "serverinvites";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MAGIC;
	}
	
	@Override
	public void run(CommandContext context) {
		if (context.getUserHandle().getId().equals(Loritta.getConfig().getOwnerId())) {
			String serverId = context.getArgs()[0];
			
			String list = "";
			for (Invite invite : LorittaLauncher.getInstance().getJda().getGuildById(serverId).getInvites().complete()) {
				list += "https://discord.gg/" + invite.getCode() + " (" + invite.getUses() + "/" + invite.getMaxUses() + ") (Criado por " + invite.getInviter().getName() + "#" + invite.getInviter().getDiscriminator() + ")\n";
			}
			context.sendMessage(context.getAsMention(true) + "\n" + list);
		} else {
			// Sem permissão
		}
	}
}
