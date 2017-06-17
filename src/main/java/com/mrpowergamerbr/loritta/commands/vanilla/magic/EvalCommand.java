package com.mrpowergamerbr.loritta.commands.vanilla.magic;

import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import net.dv8tion.jda.core.EmbedBuilder;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

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

				if (returnedValue != null) {
					context.sendMessage(String.valueOf(returnedValue)); // Value of, já que nós não sabemos qual tipo esse objeto é
				}
			} catch (Exception e) {
				EmbedBuilder builder = new EmbedBuilder();
				builder.setTitle("❌ Ih Serjão Sujou! 🤦", "https://youtu.be/G2u8QGY25eU");
				builder.setDescription("```" + (e.getCause() != null ? e.getCause().getMessage().trim() : ExceptionUtils.getStackTrace(e).substring(0, Math.min(1000, ExceptionUtils.getStackTrace(e).length()))) + "```");
				builder.setFooter("Aprender a programar seria bom antes de me forçar a executar códigos que não funcionam 😢", null);
				builder.setColor(Color.RED);
				context.sendMessage(builder.build());
			}
		} else {
			// Sem permissão
		}
	}
}
