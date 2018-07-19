package com.mrpowergamerbr.loritta.threads

import com.google.common.flogger.FluentLogger
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.loritta

class FetchFacebookPostsThread : Thread("Fetch Facebook Posts Thread") {
	companion object {
		private val logger = FluentLogger.forEnclosingClass()
	}

	override fun run() {
		super.run()
		while (true) {
			fetchPosts()
			Thread.sleep(30000)
		}
	}

	fun fetchPosts() {
		logger.atInfo().log("Pegando posts do Facebook...")
		try {
			val pagePostsSAM = LorittaUtilsKotlin.getRandomPostsFromPage("samemes2", 25)

			loritta.southAmericaMemesPageCache.addAll(pagePostsSAM)

			if (loritta.southAmericaMemesPageCache.size > 20) {
				loritta.southAmericaMemesPageCache = loritta.southAmericaMemesPageCache.subList(19, loritta.southAmericaMemesPageCache.size)
			}
		} catch (e: Exception) {
			logger.atSevere().log("Erro ao pegar posts do Facebook!", e)
		}
	}
}