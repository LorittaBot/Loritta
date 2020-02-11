package com.mrpowergamerbr.loritta.threads

/**
 * Thread que atualiza o status da Loritta a cada 1s segundos
 */
class BirthdayThread : Thread("Birthday Thread") {
	override fun run() {
		super.run()

		while (true) {
			try {
			} catch (e: Exception) {
				e.printStackTrace()
			}
			Thread.sleep(1000)
		}
	}

	fun handleBirthdays() {

	}
}