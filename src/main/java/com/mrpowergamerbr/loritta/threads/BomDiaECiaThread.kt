package com.mrpowergamerbr.loritta.threads

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.config.LorittaConfig
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.*
import java.io.File
import java.lang.management.ManagementFactory
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Thread para o Bom Dia & Cia
 */
class BomDiaECiaThread : Thread("Bom Dia & Cia") {
	val logger by logger()

	override fun run() {
		super.run()

		while (true) {
			try {
				handleAnnouncing()
			} catch (e: Exception) {
				e.printStackTrace()
			}
			Thread.sleep(1000)
		}
	}

	fun handleAnnouncing() {
		// Gerar um tempo aleatório entre 15 minutos e 30 minutos
		val wait = Loritta.RANDOM.nextLong(900_000, 2_700_000)
		val estimatedTime = wait + System.currentTimeMillis()
		logger.info("Iremos esperar ${wait} até o próximo Funk do Yudi ${estimatedTime.humanize(loritta.getLocaleById("default"))}")
		Thread.sleep(wait)
		loritta.bomDiaECia.handleBomDiaECia(false)
	}
}