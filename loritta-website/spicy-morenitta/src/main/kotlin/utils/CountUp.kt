package utils

@JsName("CountUp")
external class CountUp(id: String, start: Double, end: Double, decimals: Int? = definedExternally, duration: Double? = definedExternally, options: CountUpOptions? = definedExternally) {
	fun start()

	fun start(callback: () -> Unit)

	fun pauseResume()

	fun reset()

	fun update(numValue: Double)
}