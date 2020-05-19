package net.perfectdreams.loritta.parallax.wrapper

// Currently we do this hack because we can't select the overload with GraalJS
// Maybe we could look about this? https://www.graalvm.org/sdk/javadoc/org/graalvm/polyglot/HostAccess.Builder.html
class JSLorittaReply @JvmOverloads constructor(
		val message: String,
		val emote: String? = null,
		val mentionUser: Boolean = true
)