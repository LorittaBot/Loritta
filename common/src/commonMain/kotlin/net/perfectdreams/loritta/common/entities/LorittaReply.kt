package net.perfectdreams.loritta.common.entities

class LorittaReply(
    val content: String,
    val prefix: String,
    val inReplyToUser: User? = null,
    val mentionSenderHint: Boolean = false
) {
    init {
        if (mentionSenderHint && inReplyToUser == null)
            throw IllegalArgumentException("Mention Sender Hint is enabled, however the replied user is null! If you are using a \"sendMessage { ... }\" function, try replacing it with the contextual reply alternative \"sendReply { ... }\" or \"sendReplies { ... }\" provided by the CommandContext")
    }
}