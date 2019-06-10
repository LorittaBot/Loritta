package net.perfectdreams.loritta.website.utils

enum class LoriWebCode(val errorId: Int, val fancyName: String) {
    // Unauthorized
    UNAUTHORIZED(40001, "Unauthorized"),
    UNKNOWN_SOMETHING(40002, "Something is missing, whoops")
}