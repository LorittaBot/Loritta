package com.mrpowergamerbr.loritta.commands.nashorn;

import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.commands.nashorn.wrappers.NashornContext;
import com.sun.management.ThreadMXBean;
import net.dv8tion.jda.core.EmbedBuilder;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import java.awt.*;
import java.lang.management.ManagementFactory;
import java.util.concurrent.Callable;

class NashornTask implements Callable<Void> {
	ScriptEngine engine;
	String javaScript;
	CommandContext ogContext;
	NashornContext context;
	boolean running = true;
	int autoKill = 0;

	public NashornTask(ScriptEngine engine, String javaScript, CommandContext ogContext, NashornContext context) {
		this.engine = engine;
		this.javaScript = javaScript;
		this.ogContext = ogContext;
		this.context = context;
		running = true;
	}

	@Override
	public Void call() throws Exception {
		ThreadMXBean sunBean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
		long id = Thread.currentThread().getId();
		Thread currentThread = Thread.currentThread();
		try {
			Thread t = new Thread() {
				public void run() {
					while (running) {
						System.out.println("bytes: " + sunBean.getThreadAllocatedBytes(id));
						autoKill++;
						if (sunBean.getThreadAllocatedBytes(id) > 227402240 || autoKill > 600) {
							System.out.println("!!! Matando thread");
							running = false;
							currentThread.stop(); // stop now!
						}
						try {
							Thread.sleep(25);
						} catch (Exception e) {
						}
					}
					return;
				}
			};
			t.start();
			Invocable invocable = (Invocable) engine;
			engine.eval(javaScript);
			invocable.invokeFunction("nashornCommand", context);
		} catch (Exception e) {
			e.printStackTrace();
			EmbedBuilder builder = new EmbedBuilder();
			builder.setTitle("❌ Ih Serjão Sujou! 🤦", "https://youtu.be/G2u8QGY25eU");
			builder.setDescription("```" + (e.getCause() != null ?
					e.getCause().getMessage().trim() :
					ExceptionUtils.getStackTrace(e)
							.substring(0, Math.min(2000, ExceptionUtils.getStackTrace(e).length()))) + "```");
			builder.setFooter(
					"Aprender a programar seria bom antes de me forçar a executar códigos que não funcionam 😢", null);
			builder.setColor(Color.RED);
			ogContext.sendMessage(builder.build());
		}
		running = false;
		return null;
	}
}