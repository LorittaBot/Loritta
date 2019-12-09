package net.perfectdreams.loritta.endpoints

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path
import java.io.File
import kotlin.reflect.full.createType

@Path("/:localeId/christmas2019")
class Christmas2019Controller {
	@GET
	@LoriRequiresVariables(true)
	fun handle(req: Request, res: Response, @Local variables: MutableMap<String, Any?>) {
		val html = runBlocking {
			ScriptingUtils.evaluateWebPageFromTemplate(
					File(
							"${LorittaWebsite.INSTANCE.config.websiteFolder}/views/christmas_2019.kts"
					),
					mapOf(
							"path" to req.path().split("/").drop(2).joinToString("/"),
							"websiteUrl" to LorittaWebsite.INSTANCE.config.websiteUrl,
							"locale" to ScriptingUtils.WebsiteArgumentType(BaseLocale::class.createType(nullable = false), variables["locale"]!!)
					)
			)
		}

		res.send(html)
	}
}