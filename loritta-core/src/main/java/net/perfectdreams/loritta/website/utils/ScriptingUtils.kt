package net.perfectdreams.loritta.website.utils

import com.mrpowergamerbr.loritta.utils.KtsObjectLoader
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import mu.KotlinLogging
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.utils.extensions.transformToString
import org.jetbrains.kotlin.utils.addToStdlib.measureTimeMillisWithResult
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType

object ScriptingUtils {
    private val logger = KotlinLogging.logger {}
    private val currentlyCompilingFiles = ConcurrentHashMap<File, Deferred<Any>>()

    suspend fun evaluateWebPageFromTemplate(file: File, args: Map<String, Any>): String {
        val document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .newDocument()

        val modifiedArgs = mutableMapOf<String, Any>(
                "document" to WebsiteArgumentType(Document::class.createType(), document)
        ).apply { this.putAll(args) }

        val argTypes = modifiedArgs.map {
            it.key to it.value.run {
                if (this is WebsiteArgumentType) {
                    var str = (this.kType.classifier as KClass<*>).simpleName

                    if (this.kType.arguments.isNotEmpty())
                        str += "<${this.kType.arguments.joinToString(", ", transform = { (it.type!!.classifier as KClass<*>).simpleName!! })}>"

                    if (this.kType.isMarkedNullable)
                        str += "?"

                    str!!
                } else {
                    this::class.simpleName!!
                }
            }
        }.toMap().toMutableMap()

        val test = evaluateTemplate<Any>(
                file,
                argTypes
        )

        // Nós precisamos manter o "document" em PRIMEIRO lugar
        // Então vamos apenas remover o "document" e depois readicionar.
        modifiedArgs.remove("document")

        val argResults = modifiedArgs.map {
            if (it.value is WebsiteArgumentType)
                (it.value as WebsiteArgumentType).value
            else
                it.value
        }.toMutableList()

        argResults.add(0, document)
        argResults.add(0, test)

        val element = test::class.members.first { it.name == "generateHtml" }.call(
                *argResults.toTypedArray()
        ) as Element

        document.appendChild(element)

        return document.transformToString()
    }

    data class WebsiteArgumentType(val kType: KType, val value: Any?)

    suspend fun <T> evaluateTemplate(file: File, args: Map<String, String> = mapOf()): T {
        if (LorittaWebsite.INSTANCE.pathCache[file] != null)
            return LorittaWebsite.INSTANCE.pathCache[file] as T

        val deferredCompilingFile = currentlyCompilingFiles[file]

        if (deferredCompilingFile != null) {
            logger.debug { "File $file is already being compiled by something else! Waiting until it finishes compilation..." }

            return deferredCompilingFile.await() as T
        }

        val code = generateCodeToBeEval(file)
                .replace("@args", args.entries.joinToString(", ", transform = { "${it.key}: ${it.value}"}))
                .replace("@call-args", args.keys.joinToString(", "))

        val editedCode = """
                import kotlinx.html.*
                import kotlinx.html.dom.*
                import com.mrpowergamerbr.loritta.utils.KtsObjectLoader
                import net.perfectdreams.loritta.utils.*
                // import net.perfectdreams.loritta.utils.locale.*
				import com.mrpowergamerbr.loritta.utils.locale.*
                import net.perfectdreams.loritta.website.*
                import net.perfectdreams.loritta.api.entities.*
                import net.perfectdreams.loritta.website.blog.*
                import net.perfectdreams.loritta.website.utils.*
                // import net.perfectdreams.loritta.website.utils.config.*
                import org.w3c.dom.Document
                import org.w3c.dom.Element
                import java.io.File
                // import net.perfectdreams.loritta.utils.oauth2.*
				import net.perfectdreams.loritta.utils.config.*

                $code
            """.trimIndent()

        File("${LorittaWebsite.INSTANCE.config.websiteFolder}/generated_views/${file.name}").writeText(editedCode)

        logger.info("Compiling ${file.name}...")

        val deferred = GlobalScope.async {
            val millis = measureTimeMillisWithResult {
                val test = KtsObjectLoader().load<Any>(editedCode)

                LorittaWebsite.INSTANCE.pathCache[file] = test

                return@measureTimeMillisWithResult test
            }

            logger.info("Took ${millis.first}ms to compile ${file.name}!")
            millis.second
        }
        currentlyCompilingFiles[file] = deferred

        try {
            currentlyCompilingFiles.remove(file)
            return deferred.await() as T
        } catch (e: Exception) {
            currentlyCompilingFiles.remove(file)
            logger.error(e) { "Exception while trying to evaluate $file"}
            throw e
        }
    }

    fun generateCodeToBeEval(file: File): String {
        val stack = fillStack(file, Stack())

        val output = StringBuilder()

        while (!stack.empty()) {
            val holder = stack.pop()
            val tempCode = generateCodeFromFile(holder.file, holder.code)
            output.append(tempCode)
            output.append('\n')

            if (stack.empty()) {
                output.append(holder.file.nameWithoutExtension.capitalize())
                output.append("()")
            }
        }

        return output.toString()
    }

    fun generateCodeFromFile(f: File, code: List<String>): String {
        var isAbstract = false
        var classToBeExtended: String? = null

        val importedCode = mutableListOf<String>()

        // Preprocess stage
        for (line in code) {
            if (line == "@type 'abstract'")
                isAbstract = true
            if (line.startsWith("@extends")) {
                val pathToBeImported = line.substring("@extends '".length, line.length - 1)

                val fileName = pathToBeImported.replace(".kts", "")
                classToBeExtended = fileName.capitalize()
            }
            if (line.startsWith("@import")) {
                val pathToBeImported = line.substring("@import '".length, line.length - 1)

                importedCode.add(File("${LorittaWebsite.INSTANCE.config.websiteFolder}/views/$pathToBeImported").readText())
                continue
            }
        }

        // Generate the code
        var tempCode = ""
        if (isAbstract)
            tempCode += "abstract "
        else
            tempCode += "open "
        tempCode += "class ${f.nameWithoutExtension.replace("-", "").capitalize()} "
        if (classToBeExtended != null)
            tempCode += ": $classToBeExtended() "
        tempCode += "{\n"

        importedCode.forEach {
            tempCode += it
        }

        for (line in code) {
            if (!line.startsWith("@")) {
                tempCode += "    $line\n"
            }
        }

        tempCode += "}\n"

        return tempCode
    }

    fun fillStack(f: File, stack: Stack<CodeHolder>): Stack<CodeHolder> {
        val inputLines = f.readLines()
        val firstLine = inputLines.first()

        stack.push(
                CodeHolder(
                        f,
                        inputLines
                )
        )

        if (firstLine.startsWith("@extends")) {
            val pathToBeExtended = firstLine.substring("@extends '".length, firstLine.length - 1)

            fillStack(File("${LorittaWebsite.INSTANCE.config.websiteFolder}/views/$pathToBeExtended"), stack)
        }
        return stack
    }

    data class CodeHolder(
            val file: File,
            val code: List<String>
    )
}