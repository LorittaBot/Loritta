package net.perfectdreams.loritta.parallax.wrapper

import org.graalvm.polyglot.Context

fun main() {
	val graalContext = Context.newBuilder()
			.allowHostAccess(true) // Permite usar coisas da JVM dentro do GraalJS
			.allowExperimentalOptions(true)  // doesn't seem to be needed
			.option("js.ecmascript-version", "11") // EMCAScript 2020
			.option("js.nashorn-compat", "true")
			.option("js.experimental-foreign-object-prototype", "true") // Allow array extension methods for arrays
			.build()

	val testMap = mutableMapOf<String, Any?>()

	val value = graalContext.eval(
			"js",
			"(function(customList) { \n" +
					"customList[\"hello\"] = \"owo\";\n" +
			"})"
	)

	value.execute(ProxyTest())
}