package net.perfectdreams.loritta.website.backend.utils.extras

import net.perfectdreams.loritta.common.locale.BaseLocale
import com.typesafe.config.ConfigFactory
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend
import net.perfectdreams.loritta.website.backend.utils.extras.dynamic.DynamicExtras
import net.perfectdreams.loritta.website.backend.utils.extras.dynamic.OfficialIllustrationsDynamicExtras
import org.yaml.snakeyaml.Yaml

object ExtrasUtils {
    private val dynamicallyGeneratedExtras = mutableListOf<DynamicExtras>(
        OfficialIllustrationsDynamicExtras()
    )

    fun loadCategories(showtime: LorittaWebsiteBackend): List<CategoryConfig> {
        val categoriesConfig = ConfigFactory.parseString(
            // Workaround because HOCON can't deserialize root lists (sad)
            "categories=" + LorittaWebsiteBackend::class.java.getResourceAsStream("/extras/categories.conf")!!
                .readAllBytes()
                .toString(Charsets.UTF_8)
        )
            .resolve()

        // Workaround because HOCON can't deserialize root lists (sad)
        return Hocon.decodeFromConfig<CategoriesConfig>(categoriesConfig)
            .categories
    }

    fun loadAuthors(showtime: LorittaWebsiteBackend): List<AuthorConfig> {
        // Also load the authors
        val authorsConfig = ConfigFactory.parseString(
            // Workaround because HOCON can't deserialize root lists (sad)
            "authors=" + LorittaWebsiteBackend::class.java.getResourceAsStream("/extras/authors.conf")!!
                .readAllBytes().toString(Charsets.UTF_8)
        )
            .resolve()

        // Workaround because HOCON can't deserialize root lists (sad)
        return Hocon.decodeFromConfig<AuthorsConfig>(authorsConfig)
            .authors
    }

    fun loadWikiEntries(showtime: LorittaWebsiteBackend, locale: BaseLocale) = loadWikiEntries(showtime, locale, loadCategories(showtime))

    fun loadWikiEntries(showtime: LorittaWebsiteBackend, locale: BaseLocale, categories: List<CategoryConfig>): List<ExtrasCategory> {
        return categories.map {
            val entries = mutableListOf<ExtrasEntry>()

            val category = ExtrasCategory(
                it.title,
                entries
            )

            for (entry in it.entries) {
                val defaultFile = entry.files["default"] ?: throw RuntimeException("Missing default file entry for ${entry}!")
                val file = (entry.files[locale.id] ?: defaultFile)

                println(file)

                // Files that don't have ".md" extension are "dynamically generated" by the server
                // Mostly used for dynamic stuff or stuff that is easier to be done in code
                if (file.endsWith(".md")) {
                    try {
                        val data = Yaml().load<Map<String, String?>>(
                            LorittaWebsiteBackend::class.java.getResourceAsStream("/extras/$file")
                                .readAllBytes()
                                .toString(Charsets.UTF_8)
                                .substringBefore("---")
                                .also { println(it) }
                        )

                        entries.add(
                            MarkdownExtrasEntry(
                                defaultFile.removeSuffix(".md").removeSuffix("index"),
                                data["title"] as String,
                                entry.icon,
                                (data["authors"] as List<String>?) ?: listOf(),
                                file
                            )
                        )
                    } catch (e: NullPointerException) {
                        println("Something went wrong while loading $file")
                        e.printStackTrace()
                    }
                } else {
                    val dynamicExtrasEntry = dynamicallyGeneratedExtras.first { it.path == file }

                    entries.add(
                        DynamicExtrasEntry(
                            defaultFile,
                            dynamicExtrasEntry.title,
                            entry.icon,
                            dynamicExtrasEntry.authors,
                            dynamicExtrasEntry
                        )
                    )
                }
            }

            category
        }
    }

    data class RenderEntry(
        val htmlContent: String,
        val entry: ExtrasEntry
    )

    data class ExtrasCategory(
        val title: String,
        val entries: List<ExtrasEntry>
    )

    open class ExtrasEntry(
        val path: String,
        val title: String,
        val icon: String,
        val authors: List<String>
    )

    class MarkdownExtrasEntry(
        path: String,
        title: String,
        icon: String,
        authors: List<String>,
        val file: String
    ) : ExtrasEntry(path, title, icon, authors)

    class DynamicExtrasEntry(
        path: String,
        title: String,
        icon: String,
        authors: List<String>,
        val generator: DynamicExtras
    ) : ExtrasEntry(path, title, icon, authors)
}