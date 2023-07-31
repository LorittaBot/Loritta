package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths

import net.perfectdreams.loritta.cinnamon.dashboard.common.ScreenPathElement
import net.perfectdreams.loritta.cinnamon.dashboard.common.buildToKtorPath
import net.perfectdreams.loritta.cinnamon.dashboard.common.buildToPath

class ScreenPathWithArguments(val path: ScreenPath, val arguments: Map<String, String>) {
    init {
        for (element in path.elements) {
            when (element) {
                is ScreenPathElement.OptionPathElement -> {
                    if (!arguments.containsKey(element.parameterId))
                        error("Missing argument for parameter ${element.parameterId}!")
                }
                is ScreenPathElement.StringPathElement -> {}
            }
        }
    }

    fun build() = path.elements.buildToPath(arguments)
}