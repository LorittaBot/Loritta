package net.perfectdreams.loritta.parallax.wrapper

import org.graalvm.polyglot.Context

fun main() {
	val graalContext = Context.newBuilder()
			.hostClassFilter {
				it.startsWith("com.mrpowergamerbr.loritta.parallax.wrappers") || it.startsWith("com.mrpowergamerbr.loritta.commands.nashorn.NashornUtils")
			}
			.allowHostAccess(true) // Permite usar coisas da JVM dentro do GraalJS
			.option("js.nashorn-compat", "true")
			.build()

	val value = graalContext.eval("js", "(function(customList) { \n" +
			"console.log(customList); customList.forEach((e) => { console.log(e); }); customList[0] = 4; customList.forEach((e) => { console.log(e); });\n" +
			"})")
	// value.execute(Test(mutableListOf(1L, 2L, 3L, 4L)))
}