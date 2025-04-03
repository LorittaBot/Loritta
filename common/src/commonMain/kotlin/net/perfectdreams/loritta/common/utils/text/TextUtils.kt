package net.perfectdreams.loritta.common.utils.text

object TextUtils {
    // https://stackoverflow.com/a/60010299/7271796
    private val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
    private val snakeRegex = "_[a-zA-Z]".toRegex()
    private val kebabRegex = "-[a-zA-Z]".toRegex()
    private val markdownUrlsRegex = Regex("\\[(.+?)]\\((<?https?://.+?>?)\\)(!|\\.|\\.\\.\\.|\\?|;|:)?")

    // String extensions
    fun camelToSnakeCase(string: String) = camelRegex.replace(string) {
        "_${it.value}"
    }.lowercase()

    fun snakeToLowerCamelCase(string: String) = snakeRegex.replace(string) {
        it.value.replace("_","")
            .uppercase()
    }

    fun kebabToLowerCamelCase(string: String) = kebabRegex.replace(string) {
        it.value.replace("-","")
            .uppercase()
    }

    fun snakeToUpperCamelCase(string: String) = snakeToLowerCamelCase(string).capitalize()

    /**
     * Shortens [this] to [maxLength], if the text would overflow, [suffix] is appended to the end of the string
     */
    fun String.shortenWithEllipsis(maxLength: Int, suffix: String = "..."): String {
        if (this.length >= maxLength)
            return this.take(maxLength - suffix.length) + suffix
        return this
    }

    /**
     * Strips code backticks from [this] and then [shortenWithEllipsis]
     */
    fun String.shortenAndStripCodeBackticks(maxLength: Int, suffix: String = "..."): String =
        this.stripCodeBackticks().shortenWithEllipsis(maxLength, suffix)

    /**
     * Strips code backticks from [this]
     */
    fun String.stripCodeBackticks(): String {
        return this.replace("`", "")
    }

    /**
     * Strips new lines from [this]
     */
    fun String.stripNewLines() = this.replace(Regex("[\\r\\n]"), "")

    /**
     * Converts Markdown links with label such as "[link here](https://loritta.website/)" to "link here https://loritta.website/"
     *
     * Links that have punctuation after the link, such as "[link here](https://loritta.website/)!" are converted to "link here! https://loritta.website/"
     *
     * Useful to convert messages to something that can be sent within bot messages that aren't within a webhook/interaction!
     */
    fun String.convertMarkdownLinksWithLabelsToPlainLinks(): String {
        return this.replace(markdownUrlsRegex) {
            val punctuation = it.groupValues[3]
            if (punctuation.isNotEmpty()) {
                "${it.groupValues[1]}${it.groupValues[3]} ${it.groupValues[2]}"
            } else {
                "${it.groupValues[1]} ${it.groupValues[2]}"
            }
        }
    }
}