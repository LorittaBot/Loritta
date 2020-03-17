package net.perfectdreams.spicymorenitta.utils

import kotlin.js.Promise

@JsName("JSZip")
external class JSZip {
	fun loadAsync(data: dynamic): Promise<Zip>
}

external class Zip {
	fun file(str: String): ZipChain
}

external class ZipChain {
	fun async(type: String): Promise<dynamic>
}