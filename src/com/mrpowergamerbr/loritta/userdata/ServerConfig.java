package com.mrpowergamerbr.loritta.userdata;

import java.util.ArrayList;
import java.util.HashMap;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexed;

import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandOptions;
import com.mrpowergamerbr.loritta.commands.custom.CustomCommand;
import com.mrpowergamerbr.loritta.whistlers.Whistler;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(fluent = true)
@Entity(value = "servers")
@ToString
public class ServerConfig {
	@Id
	@Indexed(options = @IndexOptions(unique = true))
	String guildId; // Guild ID
	String commandPrefix = "="; // Command Prefix (example: +help or .help or etc)
	ArrayList<String> modules = new ArrayList<String>();
	DebugOptions debugOptions = new DebugOptions();
	boolean deleteMessageAfterCommand; // Deletar mensagem do comando após executar ele?
	
	HashMap<String, CommandOptions> commandOptions = new HashMap<String, CommandOptions>(); // Command Options
	// Os command options são salvos assim:
	// CommandBase.getClass().getSimpleName() - CommandOptions

	// boolean warnOnFail; // Avisar ao usuário quando escrever o comando errado?
	boolean explainOnCommandRun = true; // Explicar quando rodar *comando*? (Ou quando usar *comando* :shrug:)
	boolean explainInPrivate = false; // Caso explainOnCommandRun estiver ativado, é para explicar APENAS no privado ou mandar no global?
	boolean commandOutputInPrivate = false; // É para mandar o output (ou seja, tudo do comando) no privado em vez de mandar no global?
	boolean mentionOnCommandOutput = true; // Caso esteja ativado, a Loritta irá marcar quem executou na mensagem resposta
	
	ArrayList<Whistler> whistlers = new ArrayList<Whistler>(); // Whistlers
	
	ArrayList<CustomCommand> customCommands = new ArrayList<CustomCommand>(); // Comandos customizados

	JoinLeaveConfig joinLeaveConfig = new JoinLeaveConfig();
	
	public CommandOptions getCommandOptionsFor(CommandBase cmd) {
		if (commandOptions.containsKey(cmd.getClass().getSimpleName())) {
			return commandOptions.get(cmd.getClass().getSimpleName());
		}
		try {
			return (CommandOptions) LorittaLauncher.getInstance().getCommandManager().getDefaultCmdOptions().get(cmd.getClass().getSimpleName()).newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Getter
	@Setter
	public static class DebugOptions extends CommandOptions {
		boolean enableAllModules; // Caso ativado, TODAS as modules estarão ativadas
	}
}
