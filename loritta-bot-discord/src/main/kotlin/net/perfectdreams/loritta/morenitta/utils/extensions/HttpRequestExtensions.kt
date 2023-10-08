package net.perfectdreams.loritta.morenitta.utils.extensions

import com.github.kevinsawicki.http.HttpRequest

fun HttpRequest.success() = this.code() in 200..226