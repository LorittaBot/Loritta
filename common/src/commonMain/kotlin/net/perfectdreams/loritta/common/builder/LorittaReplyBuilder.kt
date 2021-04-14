package net.perfectdreams.loritta.common.builder

/**
 * Builds a Loritta Reply, Loritta Replies are a fancy formatting to normal messages, mostly looks like this:
 *
 * Prefix **|** UserMention Content
 *
 * This is open to allow platforms to extend and replace the "build()" method
 */
open class LorittaReplyBuilder {
    var content: String? = null
    var prefix: String? = null
}