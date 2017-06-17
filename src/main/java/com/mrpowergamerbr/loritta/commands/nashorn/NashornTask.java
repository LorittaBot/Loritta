package com.mrpowergamerbr.loritta.commands.nashorn;

import com.mrpowergamerbr.loritta.commands.CommandContext;
import net.dv8tion.jda.core.EmbedBuilder;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import java.awt.*;
import java.util.concurrent.Callable;

class NashornTask implements Callable<Void> {
	ScriptEngine engine;
	String javaScript;
	CommandContext ogContext;
	NashornContext context;

	public NashornTask(ScriptEngine engine, String javaScript, CommandContext ogContext, NashornContext context) {
		this.engine = engine;
		this.javaScript = javaScript;
		this.ogContext = ogContext;
		this.context = context;
	}

	@Override
	public Void call() throws Exception {
		try {
			Invocable invocable = (Invocable) engine;
			engine.eval(javaScript);
			invocable.invokeFunction("nashornCommand", context, new NashornUtils());
		} catch (Exception e) {
			e.printStackTrace();
			EmbedBuilder builder = new EmbedBuilder();
			builder.setTitle("❌ Ih Serjão Sujou! 🤦", "https://youtu.be/G2u8QGY25eU");
			builder.setDescription("```" + (e.getCause() != null ?
					e.getCause().getMessage().trim() :
					ExceptionUtils.getStackTrace(e)
							.substring(0, Math.min(1000, ExceptionUtils.getStackTrace(e).length()))) + "```");
			builder.setFooter(
					"Aprender a programar seria bom antes de me forçar a executar códigos que não funcionam 😢", null);
			builder.setColor(Color.RED);
			ogContext.sendMessage(builder.build());
		}
		return null;
	}
}