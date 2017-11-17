package com.mrpowergamerbr.loritta.listeners.nashorn;

import com.sun.management.ThreadMXBean;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import java.lang.management.ManagementFactory;
import java.util.concurrent.Callable;

class NashornEventTask implements Callable<Void> {
	ScriptEngine engine;
	String javaScript;
	String call;
	Object[] objects;
	boolean running = true;
	int autoKill = 0;

	public NashornEventTask(ScriptEngine engine, String javaScript, String call, Object... objects) {
		this.engine = engine;
		this.javaScript = javaScript;
		this.call = call;
		this.objects = objects;
		running = true;
	}

	@Override
	public Void call() throws Exception {
		ThreadMXBean sunBean = (ThreadMXBean) ManagementFactory.getThreadMXBean();
		long id = Thread.currentThread().getId();
		Thread currentThread = Thread.currentThread();
		try {
			Thread t = new Thread() {
				public void run() {
					while (running) {
						autoKill++;
						if (sunBean.getThreadAllocatedBytes(id) > 227402240 || autoKill > 600) {
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
			invocable.invokeFunction(call, objects);
		} catch (Exception e) {
		}
		running = false;
		return null;
	}
}