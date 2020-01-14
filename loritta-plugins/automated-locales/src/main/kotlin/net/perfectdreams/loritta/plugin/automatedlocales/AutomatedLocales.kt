package net.perfectdreams.loritta.plugin.automatedlocales

import com.google.gson.GsonBuilder
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.plugin.LorittaPlugin
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import mu.KotlinLogging
import java.io.File
import kotlin.random.Random

class AutomatedLocales : LorittaPlugin() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun onEnable() {
        if (!loritta.isMaster)
            return

        logger.info { "Generating automated locales..." }
        val defaultLocale = loritta.getLocaleById(Constants.DEFAULT_LOCALE_ID)
        val englishLocale = loritta.getLocaleById("en-us")

        // Remover linguagens furrificadas carregadas da memória, nós iremos recarregar depois
        loritta.locales = loritta.locales.toMutableMap().apply {
            this.remove("pt-furry")
            this.remove("en-furry")
            this.remove("auto-pt-furry")
            this.remove("auto-en-furry")
        }
        val autoPtFurry = furrifyLocale("auto-pt-furry", defaultLocale)
        val autoEnFurry = furrifyLocale("auto-en-furry", englishLocale)
        autoPtFurry.localeEntries["loritta.inheritsFromLanguageId"] = "default"
        autoEnFurry.localeEntries["loritta.inheritsFromLanguageId"] = "en-us"

        // Salvar as locales em arquivos
        val autoPtFurryFolder = File(Loritta.LOCALES, "auto-pt-furry")
        val autoEnFurryFolder = File(Loritta.LOCALES, "auto-en-furry")
        autoPtFurryFolder.mkdirs()
        autoEnFurryFolder.mkdirs()

        File(autoPtFurryFolder, "furrified.json")
                .writeText(
                        GsonBuilder()
                                .setPrettyPrinting()
                                .create()
                                .toJson(
                                        autoPtFurry.localeEntries
                                )
                )

        File(autoEnFurryFolder, "furrified.json")
                .writeText(
                        GsonBuilder()
                                .setPrettyPrinting()
                                .create()
                                .toJson(
                                        autoPtFurry.localeEntries
                                )
                )

        // Recarregar as locales editadas
        loritta.loadLocale("auto-pt-furry", defaultLocale)
        loritta.loadLocale("auto-en-furry", englishLocale)
        loritta.loadLocale("pt-furry", autoPtFurry)
        loritta.loadLocale("en-furry", autoEnFurry)
    }

    val replacements = mapOf(
            "tal" to "taw",
            "quer" to "quew",
            "ser" to "sew",
            "dir" to "diw",
            "R" to "W",
            "L" to "W",
            "ow" to "OwO",
            "no" to "nu",
            "has" to "haz",
            "have" to "haz",
            "you" to "uu",
            "the " to "da ",
            "fofo" to "foof",
            "fofa" to "foof",
            "ito" to "it",
            "dade" to "dad",
            "tando" to "tand",
            "ens" to "e",
            "tas" to "ts",
            "quanto" to "quant",
            "ente" to "ent",
            "não" to "naum"
    )

    val suffixes = listOf(
            ":3",
            "UwU",
            "ʕʘ‿ʘʔ",
            ">_>",
            "^_^",
            "^-^",
            ";_;",
            ";-;",
            "xD",
            "x3",
            ":D",
            ":P",
            ";3",
            "XDDD",
            "ㅇㅅㅇ",
            "(人◕ω◕)",
            "（＾ｖ＾）",
            ">_<"
    )

    fun furrifyLocale(localeId: String, originalLocale: BaseLocale): BaseLocale {
        val newLocale = BaseLocale(localeId)

        for ((key, value) in originalLocale.localeEntries) {
            if (value is String) {
                val furrifiedMessage = furrify(value)
                if (furrifiedMessage != value)
                    newLocale.localeEntries[key] = furrifiedMessage
            } else if (value is List<*>) {
                value as List<String>
                newLocale.localeEntries[key] =  value.map { furrify(it) }
            }
        }

        return newLocale
    }

    fun furrify(input: String): String {
        var new = input

        val suffix = if (new.length % 4 == 0) {
            when {
                new.contains("triste", true) || new.contains("desculp", true) || new.contains("sorry", true) -> ">_<"
                new.contains("parabéns", true) -> "(人◕ω◕)"
                else -> suffixes.random(Random(new.hashCode()))
            }
        } else ""

        for ((from, to) in replacements) {
            new = new.replace(from, to)
        }

        new += " $suffix"
        new = new.trim()

        return new
    }
}
