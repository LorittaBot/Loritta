package net.perfectdreams.loritta.plugin.automatedlocales

import com.google.gson.GsonBuilder
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.plugin.LorittaPlugin
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import mu.KotlinLogging
import net.perfectdreams.loritta.api.LorittaBot
import java.io.File
import kotlin.random.Random

class AutomatedLocales(name: String, loritta: LorittaBot) : LorittaPlugin(name, loritta) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun onEnable() {
        if (!lorittaDiscord.isMaster)
            return

        logger.info { "Generating automated locales..." }
        val defaultLocale = lorittaDiscord.getLocaleById(Constants.DEFAULT_LOCALE_ID)
        val englishLocale = lorittaDiscord.getLocaleById("en-us")

        // Remover linguagens furrificadas carregadas da memória, nós iremos recarregar depois
        lorittaDiscord.locales = lorittaDiscord.locales.toMutableMap().apply {
            this.remove("pt-debug")
            this.remove("en-debug")
            this.remove("pt-furry")
            this.remove("en-furry")
            this.remove("auto-pt-furry")
            this.remove("auto-en-furry")
        }

        val autoPtFurry = furrifyLocale("auto-pt-furry", defaultLocale)
        val autoEnFurry = furrifyLocale("auto-en-furry", englishLocale)
        autoPtFurry.localeEntries["loritta.inheritsFromLanguageId"] = "default"
        autoEnFurry.localeEntries["loritta.inheritsFromLanguageId"] = "en-us"

        val autoDebugPt = convertLocaleToPseudoLocalization("pt-debug", defaultLocale)
        val autoDebugEn = convertLocaleToPseudoLocalization("en-debug", englishLocale)
        autoDebugPt.localeEntries["loritta.inheritsFromLanguageId"] = "default"
        autoDebugEn.localeEntries["loritta.inheritsFromLanguageId"] = "en-us"

        // Salvar as locales em arquivos
        val autoPtFurryFolder = File(Loritta.LOCALES, "auto-pt-furry")
        val autoEnFurryFolder = File(Loritta.LOCALES, "auto-en-furry")
        val autoDebugPtFolder = File(Loritta.LOCALES, "pt-debug")
        val autoDebugEnFolder = File(Loritta.LOCALES, "en-debug")
        autoPtFurryFolder.mkdirs()
        autoEnFurryFolder.mkdirs()
        autoDebugPtFolder.mkdirs()
        autoDebugEnFolder.mkdirs()

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

        File(autoDebugPtFolder, "debug.json")
                .writeText(
                        GsonBuilder()
                                .setPrettyPrinting()
                                .create()
                                .toJson(
                                        autoDebugPt.localeEntries
                                )
                )

        File(autoDebugEnFolder, "debug.json")
                .writeText(
                        GsonBuilder()
                                .setPrettyPrinting()
                                .create()
                                .toJson(
                                        autoDebugEn.localeEntries
                                )
                )

        // Recarregar as locales editadas
        lorittaDiscord.locales = lorittaDiscord.locales.toMutableMap().apply {
            this.put("pt-debug", lorittaDiscord.loadLocale("pt-debug", defaultLocale))
            this.put("en-debug", lorittaDiscord.loadLocale("en-debug", englishLocale))
            this.put("auto-pt-furry", lorittaDiscord.loadLocale("auto-pt-furry", defaultLocale))
            this.put("auto-en-furry", lorittaDiscord.loadLocale("auto-en-furry", englishLocale))
            this.put("pt-furry", lorittaDiscord.loadLocale("pt-furry", autoPtFurry))
            this.put("en-furry", lorittaDiscord.loadLocale("en-furry", autoEnFurry))
        }
    }

    val replacements = mapOf(
            "tal" to "taw",
            "quer" to "quew",
            "ser" to "sew",
            "dir" to "diw",
            "per" to "pew",
            "par" to "paw",
            "eat" to "eaw",
            "vez" to "vew",
            "isso" to "issu",
            "dio" to "diu",
            "bado" to "bad",
            "dos" to "dus",
            "mente" to "ment",
            "servidor" to "servidOwOr",
            "Loritta" to "OwOrittaw",
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

    val doNotChangeThisKeys = listOf(
            "loritta.inheritsFromLanguageId",
            "commands.fortnite.shop.localeId",
            "website.localePath"
    )

    fun convertLocaleToPseudoLocalization(localeId: String, originalLocale: BaseLocale): BaseLocale {
        val newLocale = BaseLocale(localeId)

        for ((key, value) in originalLocale.localeEntries) {
            if (key in doNotChangeThisKeys) {
                newLocale.localeEntries[key] = value
            } else {
                if (value is String) {
                    val furrifiedMessage = PseudoLocalization.convertWord(value)
                    if (furrifiedMessage != value)
                        newLocale.localeEntries[key] = furrifiedMessage
                } else if (value is List<*>) {
                    value as List<String>
                    newLocale.localeEntries[key] = value.map { PseudoLocalization.convertWord(it) }
                }
            }
        }

        newLocale.localeEntries["website.localePath"] = "${originalLocale.path}-debug"

        return newLocale
    }

    fun furrifyLocale(localeId: String, originalLocale: BaseLocale): BaseLocale {
        val newLocale = BaseLocale(localeId)

        for ((key, value) in originalLocale.localeEntries) {
            if (key in doNotChangeThisKeys) {
                newLocale.localeEntries[key] = value
            } else if (value is String) {
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
