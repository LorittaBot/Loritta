package net.perfectdreams.loritta.cinnamon.dashboard.common

import io.ktor.http.*

sealed class ScreenPathElement {
    class StringPathElement(val text: String) : ScreenPathElement()
    class OptionPathElement(val parameterId: String) : ScreenPathElement()
}

fun List<ScreenPathElement>.buildToPath(arguments: Map<String, String>): String {
    return buildString {
        for (el in this@buildToPath) {
            append("/")
            when (el) {
                is ScreenPathElement.OptionPathElement -> append(arguments[el.parameterId] ?: error("Missing argument for parameter ${el.parameterId}!"))
                is ScreenPathElement.StringPathElement -> append(el.text)
            }
        }
    }.ifEmpty { "/" }
}

fun List<ScreenPathElement>.buildToPathWithQueryArguments(arguments: Map<String, String>, queryArguments: Map<String, String>): String {
    return buildString {
        for (el in this@buildToPathWithQueryArguments) {
            append("/")
            when (el) {
                is ScreenPathElement.OptionPathElement -> append(arguments[el.parameterId] ?: error("Missing argument for parameter ${el.parameterId}!"))
                is ScreenPathElement.StringPathElement -> append(el.text)
            }
        }

        var isFirst = true
        for ((str, value) in queryArguments) {
            if (isFirst)
                append("?")
            else
                append("&")
            append(str)
            append("=")
            append(value.encodeURLParameter(true))
            isFirst = false
        }
    }.ifEmpty { "/" }
}

fun List<ScreenPathElement>.buildToKtorPath(): String {
    return buildString {
        for (el in this@buildToKtorPath) {
            append("/")
            when (el) {
                is ScreenPathElement.OptionPathElement -> append("{${el.parameterId}}")
                is ScreenPathElement.StringPathElement -> append(el.text)
            }
        }
    }.ifEmpty { "/" }
}