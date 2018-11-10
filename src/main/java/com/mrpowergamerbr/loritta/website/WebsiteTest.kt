package com.mrpowergamerbr.loritta.website

import com.mrpowergamerbr.loritta.utils.KtsObjectLoader
import kotlinx.html.HtmlBlockTag
import kotlinx.html.body
import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import java.io.File
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.functions

val cachedValue: Any by lazy {
	val file = File("C:\\Users\\Whistler\\Documents\\TavaresBot\\owo_whats_this\\hello.kts")
	KtsObjectLoader().load<Any>(file.readText())
}

fun main(args: Array<String>) {
	println(cachedValue)
	cachedValue::class.declaredFunctions.forEach {
		println(it.name)
	}
	cachedValue::class.functions.forEach {
		println(it.name)
	}
	val ktFunction = cachedValue::class.functions.first {
		it.name == "something"
	}
	println("ktFunction requires:")
	ktFunction.parameters.forEach {
		println(it)
	}
	val result = ktFunction.call(cachedValue, "source") as HtmlBlockTag.() -> Unit
	println(result)
	val builder = StringBuilder()
	val b = builder.appendHTML().html {
		body {
			result.invoke(this)
		}
	}

	println(b.toString())

}