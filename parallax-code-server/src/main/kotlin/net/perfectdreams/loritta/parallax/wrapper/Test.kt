package net.perfectdreams.loritta.parallax.wrapper

import org.graalvm.polyglot.Context

fun main() {
	val graalContext = Context.newBuilder()
			.allowHostAccess(true) // Permite usar coisas da JVM dentro do GraalJS
			.option("js.nashorn-compat", "true")
			.build()

	val value = graalContext.eval("js", "(function(customList) { \n" +
			"var MessageEmbed = Java.type('net.perfectdreams.loritta.parallax.wrapper.ParallaxEmbed'); var embed = new MessageEmbed().setDescription(\"test\"); console.log(embed); \n" +
			"})")

	value.execute("a")
	// value.execute(Test(mutableListOf(1L, 2L, 3L, 4L)))
}