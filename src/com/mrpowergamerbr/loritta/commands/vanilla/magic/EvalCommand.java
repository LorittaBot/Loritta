package com.mrpowergamerbr.loritta.commands.vanilla.magic;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;

import net.dv8tion.jda.core.EmbedBuilder;

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
				EmbedBuilder builder = new EmbedBuilder();
				builder.setTitle("❌ Ih Serjão Sujou! 🤦", "https://youtu.be/G2u8QGY25eU");
				ExceptionUtils.getStackTrace(e);
				builder.setDescription("```" + e.getCause().getMessage().trim() + "```");
				builder.setFooter("Aprender a programar seria bom antes de me forçar a executar códigos que não funcionam 😢", null);
				builder.setColor(Color.RED);
				context.sendMessage(builder.build());
			}
		} else {
			// Sem permissão
		}
	}
}
