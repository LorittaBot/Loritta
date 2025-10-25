package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.img
import net.perfectdreams.loritta.common.utils.CounterThemes

fun FlowContent.memberCounterPreview(count: Int, theme: CounterThemes, padding: Int) {
    memberCounterPreview(count, getEmojis(theme), padding)
}

private fun FlowContent.memberCounterPreview(count: Int, list: List<String>, padding: Int) {
    div {
        val paddingCount = padding - count.toString().length

        if (paddingCount > 0) {
            for (i in 0 until paddingCount) {
                val list0 = list[0]
                var imageSource: String

                if (list0.startsWith("<") && list0.endsWith(">")) {
                    // <a:super_lori_happy:524893994874961940> <:eu_te_moido:366047906689581085>
                    val emoteId = list0.split(":").last().dropLast(1)
                    val isAnimated = list0.startsWith("<a:")
                    imageSource = "https://cdn.discordapp.com/emojis/$emoteId"
                    imageSource += if (isAnimated)
                        ".gif?v=1"
                    else
                        ".png?v=1"
                } else {
                    // Não é um emote... então é o que?
                    // Bem, provavelmente é os emotes padrões do twitter
                    imageSource = "https://abs.twimg.com/emoji/v2/72x72/30-20e3.png"
                }

                img(src = imageSource) {
                    width = "24"
                    height = "24"
                }
            }
        }

        for (char in count.toString()) {
            val emote = list[char.toString().toInt()]

            var imageSource: String

            if (emote.startsWith("<") && emote.endsWith(">")) {
                // <a:super_lori_happy:524893994874961940> <:eu_te_moido:366047906689581085>
                val emoteId = emote.split(":").last().dropLast(1)
                val isAnimated = emote.startsWith("<a:")
                imageSource = "https://cdn.discordapp.com/emojis/$emoteId"
                imageSource += if (isAnimated)
                    ".gif?v=1"
                else
                    ".png?v=1"
            } else {
                // Não é um emote... então é o que?
                // Bem, provavelmente é os emotes padrões do twitter
                imageSource = when (char.toString().toInt()) {
                    0 -> "https://abs.twimg.com/emoji/v2/72x72/30-20e3.png"
                    1 -> "https://abs.twimg.com/emoji/v2/72x72/31-20e3.png"
                    2 -> "https://abs.twimg.com/emoji/v2/72x72/32-20e3.png"
                    3 -> "https://abs.twimg.com/emoji/v2/72x72/33-20e3.png"
                    4 -> "https://abs.twimg.com/emoji/v2/72x72/34-20e3.png"
                    5 -> "https://abs.twimg.com/emoji/v2/72x72/35-20e3.png"
                    6 -> "https://abs.twimg.com/emoji/v2/72x72/36-20e3.png"
                    7 -> "https://abs.twimg.com/emoji/v2/72x72/37-20e3.png"
                    8 -> "https://abs.twimg.com/emoji/v2/72x72/38-20e3.png"
                    9 -> "https://abs.twimg.com/emoji/v2/72x72/39-20e3.png"
                    else -> throw RuntimeException("Value is invalid!")
                }
            }

            img(src = imageSource) {
                width = "24"
                height = "24"
            }
        }
    }
}

private fun getEmojis(theme: CounterThemes): List<String> {
    return theme.emotes ?: throw UnsupportedOperationException("Theme ${theme.name} doesn't have emotes!")
}