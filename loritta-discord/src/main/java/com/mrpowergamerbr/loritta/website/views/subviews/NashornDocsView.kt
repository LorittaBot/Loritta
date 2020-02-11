package com.mrpowergamerbr.loritta.website.views.subviews

import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.nashorn.wrappers.NashornContext
import com.mrpowergamerbr.loritta.nashorn.wrappers.NashornGuild
import com.mrpowergamerbr.loritta.nashorn.wrappers.NashornImage
import com.mrpowergamerbr.loritta.nashorn.wrappers.NashornLorittaUser
import com.mrpowergamerbr.loritta.nashorn.wrappers.NashornMember
import com.mrpowergamerbr.loritta.nashorn.wrappers.NashornMessage
import com.mrpowergamerbr.loritta.nashorn.wrappers.NashornRole
import com.mrpowergamerbr.loritta.nashorn.wrappers.NashornUser
import com.mrpowergamerbr.loritta.website.evaluate
import org.jooby.Request
import org.jooby.Response

class NashornDocsView : AbstractView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		return path == "/loriapi"
	}

	override fun render(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): String {
		variables.put("docsAnnotation", NashornCommand.NashornDocs::class.java)
		variables.put("nashClasses",
				listOf(
						NashornPebbleClassWrapper(NashornContext::class.java, "docsNashContext"),
						NashornPebbleClassWrapper(NashornGuild::class.java, "docsNashGuild"),
						NashornPebbleClassWrapper(NashornImage::class.java, "docsNashImage"),
						NashornPebbleClassWrapper(NashornLorittaUser::class.java, "docsNashLorittaUser"),
						NashornPebbleClassWrapper(NashornMember::class.java, "docsNashMember"),
						NashornPebbleClassWrapper(NashornMessage::class.java, "docsNashMessage"),
						NashornPebbleClassWrapper(NashornRole::class.java, "docsNashRole"),
						NashornPebbleClassWrapper(NashornUser::class.java, "docsNashUser")
				)
		)

		return evaluate("loriapi.html", variables)
	}

	data class NashornPebbleClassWrapper(
			val clazz: Class<*>,
			val id: String
	)
}