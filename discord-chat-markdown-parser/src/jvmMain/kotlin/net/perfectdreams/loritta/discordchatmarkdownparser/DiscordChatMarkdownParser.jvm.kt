package net.perfectdreams.loritta.discordchatmarkdownparser

actual fun DiscordChatMarkdownParser(): DiscordChatMarkdownParser {
    return DiscordChatMarkdownParser(
        setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL),
        "."
    )
}