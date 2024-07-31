package net.perfectdreams.loritta.discordchatmessagerendererserver

object DiscordChatMessageRendererServerLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val server = DiscordChatMessageRendererServer()
        server.start()
    }
}