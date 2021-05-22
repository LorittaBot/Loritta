package net.perfectdreams.loritta.common.utils.text

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
}