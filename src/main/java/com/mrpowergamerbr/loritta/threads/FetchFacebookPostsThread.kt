package com.mrpowergamerbr.loritta.threads

import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin

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
			val pagePostsSAM = LorittaUtilsKotlin.getRandomPostsFromPage("samemes2")
			val groupPostsSAM = LorittaUtilsKotlin.getRandomPostsFromGroup("293117011064847")
			val pagePostsMemeguy = LorittaUtilsKotlin.getRandomPostsFromPage("memeguy1997")
			val groupPostsMemeguy = LorittaUtilsKotlin.getRandomPostsFromGroup("380626148947201")

			loritta.southAmericaMemesPageCache.addAll(pagePostsSAM)
			loritta.southAmericaMemesGroupCache.addAll(groupPostsSAM)
			loritta.memeguy1997PageCache.addAll(pagePostsMemeguy)
			loritta.memeguy1997GroupCache.addAll(groupPostsMemeguy)

			if (loritta.southAmericaMemesPageCache.size > 20) {
				loritta.southAmericaMemesPageCache = loritta.southAmericaMemesPageCache.subList(19, loritta.southAmericaMemesPageCache.size)
			}
			if (loritta.southAmericaMemesGroupCache.size > 20) {
				loritta.southAmericaMemesGroupCache = loritta.southAmericaMemesGroupCache.subList(19, loritta.southAmericaMemesGroupCache.size)
			}
			if (loritta.memeguy1997PageCache.size > 20) {
				loritta.memeguy1997PageCache = loritta.memeguy1997PageCache.subList(19, loritta.memeguy1997PageCache.size)
			}
			if (loritta.memeguy1997GroupCache.size > 20) {
				loritta.memeguy1997GroupCache = loritta.memeguy1997GroupCache.subList(19, loritta.memeguy1997GroupCache.size)
			}
		} catch (e: Exception) {

		}
	}
}