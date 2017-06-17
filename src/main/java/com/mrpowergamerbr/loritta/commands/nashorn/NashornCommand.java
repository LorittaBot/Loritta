package com.mrpowergamerbr.loritta.commands.nashorn;

import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Comandos usando a Nashorn Engine
 */
@Getter
@Setter
public class NashornCommand {
	public String label;
	public String javaScript;
	public List<String> aliases = new ArrayList<>();

	public NashornCommand() {}

	public NashornCommand(String label, String javaScript) {
		this.label = label;
		this.javaScript = javaScript;
	}

	@Deprecated
	public boolean handle(MessageReceivedEvent ev, String message, ServerConfig conf) {
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
				+ "var pegarConteúdoDeUmaURL=function(url){ return utils.getURL(url); };\n"
				+ "var responder=function(mensagem){ contexto.responder(mensagem); };\n"
				+ "var enviarMensagem=function(mensagem){ contexto.enviarMensagem(mensagem); };";
		try {
			ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
			Future<Void> future = executor.submit(new NashornTask(engine, blacklisted + " function nashornCommand(contexto, utils) {" + inlineMethods + javaScript + "}", ogContext, context));
			future.get(3, TimeUnit.SECONDS);
		} catch (Exception e) {
			EmbedBuilder builder = new EmbedBuilder();
			builder.setTitle("❌ Ih Serjão Sujou! 🤦", "https://youtu.be/G2u8QGY25eU");
			String description = "Irineu, você não sabe e nem eu!";
			if (e instanceof ExecutionException) {
				description = "A thread que executava este comando agora está nos céus... *+angel*";
			} else {
				if (e != null && e.getCause() != null && e.getCause().getMessage() != null) {
					description = e.getCause().getMessage().trim();
				} else if (e != null) {
					description = ExceptionUtils.getStackTrace(e).substring(0, Math.min(1000, ExceptionUtils.getStackTrace(e).length()));
				}
			}
			builder.setDescription("```" + description + "```");
			builder.setFooter(
					"Aprender a programar seria bom antes de me forçar a executar códigos que não funcionam 😢", null);
			builder.setColor(Color.RED);
			ogContext.sendMessage(builder.build());
		}
	}

	class NashornClassFilter implements ClassFilter {
		@Override
		public boolean exposeToScripts(String s) {
			return false;
		}
	}
}