package com.mrpowergamerbr.loritta.website.views.subviews

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.website.evaluate
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.utils.FeatureFlags
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import org.jooby.Request
import org.jooby.Response
import java.io.File
import kotlin.reflect.full.createType

class SupportView : AbstractView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		return path == "/support"
	}

	override fun render(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): String {
		if (FeatureFlags.NEW_WEBSITE_PORT && FeatureFlags.isEnabled(FeatureFlags.Names.NEW_WEBSITE_PORT + "-support")) {
			val html = runBlocking {
				ScriptingUtils.evaluateWebPageFromTemplate(
						File(
								"${LorittaWebsite.INSTANCE.config.websiteFolder}/views/support.kts"
						),
						mapOf(
								"path" to path,
								"websiteUrl" to LorittaWebsite.INSTANCE.config.websiteUrl,
								"locale" to ScriptingUtils.WebsiteArgumentType(BaseLocale::class.createType(nullable = false), variables["locale"]!!)
						)
				)
			}

			return html
		} else {
			return evaluate("support.html", variables)
		}
	}
}