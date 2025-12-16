package net.perfectdreams.loritta.morenitta.websitedashboard.svgicons

import org.jsoup.nodes.Element
import org.jsoup.parser.ParseSettings
import org.jsoup.parser.Parser
import java.nio.file.FileSystemNotFoundException
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.readText
import kotlin.reflect.KClass

abstract class SVGIconManager(val rootClazz: KClass<*>) {
    fun register(name: String, path: String, vararg options: SVGOptions): SVGIcon {
        val svgFile = rootClazz.getPathFromResources(path) ?: error("Could not find SVG file $path")
        val svgText = svgFile.readText(Charsets.UTF_8)

        val parser = Parser.htmlParser()
        parser.settings(ParseSettings(true, true)) // tag, attribute preserve case, if not stuff like viewBox breaks!

        val document = parser.parseInput(svgText, "/")

        val svgTag = document.getElementsByTag("svg")
            .first()!!

        if (SVGOptions.REPLACE_FILLS_WITH_CURRENT_COLOR in options) {
            // Replace "fill" tags into "currentColor"
            val fill = svgTag.getElementsByAttribute("fill")

            if (fill.attr("fill") != "none")
                fill.attr("fill", "currentColor")
        }

        if (SVGOptions.REMOVE_FILLS in options) {
            // Remove all "fill" tags
            svgTag.getElementsByAttribute("fill")
                .removeAttr("fill")
        }

        if (SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT in options) {
            svgTag.attr("fill", "currentColor")
        }

        return SVGIcon(svgTag)
    }

    enum class SVGOptions {
        REPLACE_FILLS_WITH_CURRENT_COLOR,
        SET_CURRENT_COLOR_FILL_ON_ROOT,
        REMOVE_FILLS
    }

    fun KClass<*>.getPathFromResources(path: String) = this.java.getPathFromResources(path)

    fun Class<*>.getPathFromResources(path: String): Path? {
        // https://stackoverflow.com/a/67839914/7271796
        val resource = this.getResource(path) ?: return null
        val uri = resource.toURI()
        val dirPath = try {
            Paths.get(uri)
        } catch (e: FileSystemNotFoundException) {
            // If this is thrown, then it means that we are running the JAR directly (example: not from an IDE)
            val env = mutableMapOf<String, String>()
            FileSystems.newFileSystem(uri, env).getPath(path)
        }
        return dirPath
    }
}