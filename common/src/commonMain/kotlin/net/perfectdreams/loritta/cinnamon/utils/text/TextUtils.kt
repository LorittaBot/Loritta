package net.perfectdreams.loritta.cinnamon.utils.text

object TextUtils {
    // https://stackoverflow.com/a/60010299/7271796
    private val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
    private val snakeRegex = "_[a-zA-Z]".toRegex()
    private val kebabRegex = "-[a-zA-Z]".toRegex()

    // String extensions
    fun camelToSnakeCase(string: String) = camelRegex.replace(string) {
        "_${it.value}"
    }.toLowerCase()

    fun snakeToLowerCamelCase(string: String) = snakeRegex.replace(string) {
        it.value.replace("_","")
            .toUpperCase()
    }

    fun kebabToLowerCamelCase(string: String) = kebabRegex.replace(string) {
        it.value.replace("-","")
            .toUpperCase()
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
}