package com.mrpowergamerbr.loritta.commands.vanilla.magic;

import java.util.Arrays;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;

public class EvalCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "eval";
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("executar");
	}
	
	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MAGIC;
	}
	
	@Override
	public void run(CommandContext context) {
		if (context.getUserHandle().getId().equals(Loritta.getConfig().getOwnerId())) { // Somente o dono do bot pode usar eval! Eval é uma ferramenta muito poderosa que não deve ser usada por qualquer um!
			String javaScript = String.join(" ", context.getArgs());
			
			// Agora vamos mudar um pouquinho o nosso código
			javaScript = "function loritta(context) {" + javaScript + "}";
			
			ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn"); // Iniciar o nashorn
			try {
				engine.eval(javaScript);
				Invocable invocable = (Invocable) engine;
				Object returnedValue = invocable.invokeFunction("loritta", context); // Pegar o valor retornado pelo script
				
				context.sendMessage(String.valueOf(returnedValue)); // Value of, já que nós não sabemos qual tipo esse objeto é
			} catch (ScriptException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		} else {
			// Sem permissão
		}
	}
}
