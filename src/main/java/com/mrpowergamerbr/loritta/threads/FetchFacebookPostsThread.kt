package com.mrpowergamerbr.loritta.threads

import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.loritta

class FetchFacebookPostsThread : Thread("Fetch Facebook Posts Thread") {
	override fun run() {
		super.run()
		while (true) {
			fetchPosts();
			Thread.sleep(10000);
		}
	}

	fun fetchPosts() {
		try {
			val pagePostSAM = LorittaUtilsKotlin.getRandomPostFromPage("samemes2")
			val groupPostSAM = LorittaUtilsKotlin.getRandomPostFromGroup("293117011064847")
			val pagePostMemeguy = LorittaUtilsKotlin.getRandomPostFromPage("memeguy1997")
			val groupPostMemeguy = LorittaUtilsKotlin.getRandomPostFromGroup("380626148947201")

			if (pagePostSAM != null) {
				if (loritta.southAmericaMemesPageCache.size > 20) {
					loritta.southAmericaMemesPageCache.removeAt(0)
				}
				loritta.southAmericaMemesPageCache.add(pagePostSAM)
			}
			if (groupPostSAM != null) {
				if (loritta.southAmericaMemesGroupCache.size > 20) {
					loritta.southAmericaMemesGroupCache.removeAt(0)
				}
				loritta.southAmericaMemesGroupCache.add(groupPostSAM)
			}
			if (pagePostMemeguy != null) {
				if (loritta.memeguy1997PageCache.size > 20) {
					loritta.memeguy1997PageCache.removeAt(0)
				}
				loritta.memeguy1997PageCache.add(pagePostMemeguy)
			}
			if (groupPostMemeguy != null) {
				if (loritta.memeguy1997GroupCache.size > 20) {
					loritta.memeguy1997GroupCache.removeAt(0)
				}
				loritta.memeguy1997GroupCache.add(groupPostMemeguy)
			}
		} catch (e: Exception) {

		}
	}
}