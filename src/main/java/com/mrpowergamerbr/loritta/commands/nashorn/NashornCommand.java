package com.mrpowergamerbr.loritta.commands.nashorn;

import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// Comandos usando a Nashorn Engine
public class NashornCommand {
	String label;
	String javaScript;

	public NashornCommand(String label, String javaScript) {
		this.label = label;
		this.javaScript = javaScript;
	}

	@Deprecated public boolean handle(MessageReceivedEvent ev, String message, ServerConfig conf) {
		System.out.println("Executar comando...");
		if (message.startsWith(conf.commandPrefix + label)) {
			System.out.println("Vamos executar!");
			ev.getChannel().sendTyping().complete();
			Loritta.setExecutedCommands(Loritta.getExecutedCommands() + 1);
			String cmd = label;
			String onlyArgs = message.substring(message.indexOf(cmd) + cmd.length()); // wow, such workaround, very bad
			String[] args = Arrays.asList(onlyArgs.split(" ")).stream().filter((str) -> !str.isEmpty())
					.collect(Collectors.toList()).toArray(new String[0]);
			CommandContext context = new CommandContext(conf, ev, null, args);
			run(context, new NashornContext(context));
			return true;
		}
		return false;
	}

	public boolean handle(MessageReceivedEvent ev, ServerConfig conf) {
		String message = ev.getMessage().getContent();

		if (message.startsWith(conf.commandPrefix + label)) {
			ev.getChannel().sendTyping().complete();
			Loritta.setExecutedCommands(Loritta.getExecutedCommands() + 1);
			String cmd = label;
			String onlyArgs = message.substring(message.indexOf(cmd) + cmd.length()); // wow, such workaround, very bad
			String[] args = Arrays.asList(onlyArgs.split(" ")).stream().filter((str) -> !str.isEmpty())
					.collect(Collectors.toList()).toArray(new String[0]);
			CommandContext context = new CommandContext(conf, ev, null, args);
			run(context, new NashornContext(context));
			return true;
		}
		return false;
	}

	public void run(CommandContext ogContext, NashornContext context) {
		NashornScriptEngineFactory factory = new NashornScriptEngineFactory();

		ScriptEngine engine = factory.getScriptEngine(new NashornClassFilter());
		Invocable invocable = (Invocable) engine;
		// Funções que jamais poderão ser usadas em comandos
		String blacklisted = "var quit=function(){throw 'Operação não suportada: quit';};var exit=function(){throw 'Operação não suportada: exit';};var print=function(){throw 'Operação não suportada: print';};var echo=function(){throw 'Operação não suportada: echo';};var readLine=function(){throw 'Operação não suportada: readLine';};var readFully=function(){throw 'Operação não suportada: readFully';};var load=function(){throw 'Operação não suportada: load';};var loadWithNewGlobal=function(){throw 'Operação não suportada: loadWithNewGlobal';};";
		// Funções inline para facilitar a programação de comandos
		String inlineMethods = "var loritta=function(){ return utils.loritta(); };\n"
				+ "var pegarConteúdoDeUmaURL=function(url){ return utils.getURL(url); };";
		try {
			ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
			Future<Void> future = executor.submit(new NashornTask(engine, blacklisted + " function nashornCommand(contexto, utils) {" + inlineMethods + javaScript + "}", ogContext, context));
			future.get(3, TimeUnit.SECONDS);
			System.out.println("Feito!");
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
	}

	class NashornClassFilter implements ClassFilter {
		@Override public boolean exposeToScripts(String s) {
			return false;
		}
	}
}