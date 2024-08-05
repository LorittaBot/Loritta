package net.perfectdreams.loritta.discordchatmarkdownparser

actual fun DiscordChatMarkdownParser(): DiscordChatMarkdownParser {
    return DiscordChatMarkdownParser(
        setOf(RegexOption.MULTILINE),
        "(?:.|\\n)"
    )
}