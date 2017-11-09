package com.mrpowergamerbr.loritta.frontend.views.subviews

import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.commands.nashorn.wrappers.NashornContext
import com.mrpowergamerbr.loritta.commands.nashorn.wrappers.NashornGuild
import com.mrpowergamerbr.loritta.commands.nashorn.wrappers.NashornImage
import com.mrpowergamerbr.loritta.commands.nashorn.wrappers.NashornLorittaUser
import com.mrpowergamerbr.loritta.commands.nashorn.wrappers.NashornMember
import com.mrpowergamerbr.loritta.commands.nashorn.wrappers.NashornMessage
import com.mrpowergamerbr.loritta.commands.nashorn.wrappers.NashornRole
import com.mrpowergamerbr.loritta.commands.nashorn.wrappers.NashornUser
import com.mrpowergamerbr.loritta.frontend.evaluate
import org.jooby.Request
import org.jooby.Response

class NashornDocsView : AbstractView() {
	override fun handleRender(req: Request, res: Response, variables: MutableMap<String, Any?>): Boolean {
		return req.path() == "/loriapi"
	}

	override fun render(req: Request, res: Response, variables: MutableMap<String, Any?>): String {
		variables.put("docsAnnotation", NashornCommand.NashornDocs::class.java);
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
		);

		return evaluate("loriapi.html", variables)
	}

	data class NashornPebbleClassWrapper(
			val clazz: Class<*>,
			val id: String
	)
}