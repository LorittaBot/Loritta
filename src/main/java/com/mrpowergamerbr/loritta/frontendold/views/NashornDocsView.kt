package com.mrpowergamerbr.loritta.frontendold.views

import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.commands.nashorn.wrappers.*
import com.mrpowergamerbr.loritta.frontendold.LorittaWebsite
import com.mrpowergamerbr.loritta.frontendold.utils.RenderContext

object NashornDocsView {
	@JvmStatic
	fun render(context: RenderContext): Any {
		context.contextVars.put("docsAnnotation", NashornCommand.NashornDocs::class.java);
		context.contextVars.put("nashClasses",
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

		val template = LorittaWebsite.engine.getTemplate("nashdocs.html")
		return template
	}

	data class NashornPebbleClassWrapper(
			val clazz: Class<*>,
			val id: String
	)
}