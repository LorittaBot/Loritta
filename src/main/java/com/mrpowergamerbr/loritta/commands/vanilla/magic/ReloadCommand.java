package com.mrpowergamerbr.loritta.commands.vanilla.magic;

import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.utils.config.LorittaConfig;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class ReloadCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "reload";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MAGIC;
	}
	
	@Override
	public void run(CommandContext context) {
		if (context.getUserHandle().getId().equals(Loritta.getConfig().getOwnerId())) {
		    try {
                String json = FileUtils.readFileToString(new File("./config.json"), "UTF-8");
                LorittaConfig config = Loritta.getGson().fromJson(json, LorittaConfig.class);
                LorittaLauncher.getInstance().loadFromConfig(config);
            } catch (Exception e) {}
			LorittaLauncher.getInstance().loadCommandManager();
			context.sendMessage("Loritta recarregada com sucesso!");
		} else {
			// Sem permissão
		}
	}
}
