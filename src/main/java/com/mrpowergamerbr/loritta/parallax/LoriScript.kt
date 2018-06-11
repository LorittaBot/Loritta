package com.mrpowergamerbr.loritta.parallax

fun main(args: Array<String>) {
	val input = """
		enviar "Olá, mundo!"
		enviar "kk eae mens" em "297732013006389252"
	""".trimIndent()

	var output = ""

	val quotePattern = "\"(.*?(?<!\\\\))\""
	val sendMessageRegEx = Regex("(enviar|send) $quotePattern( (no canal|em|in channel|in) $quotePattern)?")

	// Variable index
	var varIdx = 0

	input.lines().forEach {
		val test = sendMessageRegEx.toPattern().matcher(it)

		test.find()

		println("found! ${test.group(2)}")

		val channel = test.group(5)

		if (channel != null) {
			output += "var channel$varIdx = event.guild.getTextChannelById(\"$channel\")\n"
		} else {
			output += "var channel$varIdx = event.channel\n"
		}

		output += "channel$varIdx.send(\"${test.group(2)}\")\n"

		varIdx++
	}

	println("JavaScript Version:\n")
	println(output)
}