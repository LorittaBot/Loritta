package net.perfectdreams.loritta.common.locale

import mu.KotlinLogging
import net.perfectdreams.loritta.common.utils.extensions.format
import org.yaml.snakeyaml.Yaml
import java.io.File

/**
 * Manages Loritta's localizations
 *
 * @param localesFolder where the localizations are stored
 */
class LocaleManager(val localesFolder: File) {
    companion object {
        const val DEFAULT_LOCALE_ID = "default"
        private val logger = KotlinLogging.logger {}
        private val yaml = Yaml()
    }

    var locales = mapOf<String, BaseLocale>()

    /**
     * Initializes the [id] locale and adds missing translation strings to non-default languages
     *
     * @see BaseLocale
     */
    fun loadLocale(id: String, defaultLocale: BaseLocale?): BaseLocale {
        val localeStrings = mutableMapOf<String, String?>()
        val localeLists = mutableMapOf<String, List<String>?>()
        val locale = BaseLocale(id, localeStrings, localeLists)

        if (defaultLocale != null) {
            // Colocar todos os valores padrões
            localeStrings.putAll(defaultLocale.localeStringEntries)
            localeLists.putAll(defaultLocale.localeListEntries)
        }

        val localeFolder = File(localesFolder, id)

        // Does exactly what the variable says: Only matches single quotes (') that do not have a slash (\) preceding it
        // Example: It's me, Mario!
        // But if there is a slash preceding it...
        // Example: \'{@user}\'
        // It won't match!
        val singleQuotesWithoutSlashPrecedingItRegex = Regex("(?<!(?:\\\\))'")

        if (localeFolder.exists()) {
            fun loadFromFolder(folder: File, keyPrefix: (File) -> (String) = { "" }) {
                folder.listFiles().filter { it.extension == "yml" || it.extension == "json" }.forEach {
                    val entries = yaml.load<MutableMap<String, Any?>>(it.readText())

                    fun transformIntoFlatMap(map: MutableMap<String, Any?>, prefix: String) {
                        map.forEach { (key, value) ->
                            if (value is Map<*, *>) {
                                transformIntoFlatMap(value as MutableMap<String, Any?>, "$prefix$key.")
                            } else {
                                if (value is List<*>) {
                                    localeLists[keyPrefix.invoke(it) + prefix + key] = try {
                                        (value as List<String>).map {
                                            it.replace(singleQuotesWithoutSlashPrecedingItRegex, "''") // Escape single quotes
                                                .replace("\\'", "'") // Replace \' with '
                                        }
                                    } catch (e: ClassCastException) {
                                        // A LinkedHashMap does match the "is List<*>" check, but it fails when we cast the subtype to String
                                        // If that happens, we will just ignore the exception and use the raw "value" list.
                                        (value as List<String>)
                                    }
                                } else if (value is String) {
                                    localeStrings[keyPrefix.invoke(it) + prefix + key] = value.replace(singleQuotesWithoutSlashPrecedingItRegex, "''") // Escape single quotes
                                        .replace("\\'", "'") // Replace \' with '
                                } else throw IllegalArgumentException("Invalid object type detected in YAML! $value")
                            }
                        }
                    }

                    transformIntoFlatMap(entries, "")
                }
            }

            loadFromFolder(localeFolder)

            // Before, all commands locales were split up into different files, based on the category, example:
            // commands-discord.yml
            // commands:
            //   discord:
            //     userinfo:
            //       description: "owo"
            //
            // However, this had a issue that, if we wanted to move commands from a category to another, we would need to move the locales from
            // the file AND change the locale key, so, if we wanted to change a command category, that would also need to change all locale keys
            // to match. I think that was not a great thing to have.
            //
            // I thought that maybe we could remove the category from the command itself and keep it as "command:" or something, like this:
            // commands-discord.yml
            // commands:
            //   command:
            //     userinfo:
            //       description: "owo"
            //
            // This avoids the issue of needing to change the locale keys in the source code, but we still need to move stuff around if a category changes!
            // (due to the file name)
            // This also has a issue that Crowdin "forgets" who did the translation because the file changed, which is very undesirable.
            //
            // I thought that all the command keys could be in the same file and, while that would work, it would become a mess.
            //
            // So I decided to spice things up and split every command locale into different files, so, as an example:
            // userinfo.yml
            // commands:
            //   discord:
            //     userinfo:
            //       description: "owo"
            //
            // But that's boring, let's spice it up even more!
            // userinfo.yml
            // description: "owo"
            //
            // And, when loading the file, the prefix "commands.command.FileNameHere." is automatically appended to the key!
            // This fixes our previous issues:
            // * No need to change the source code on category changes, because the locale key doesn't has any category related stuff
            // * No need to change locales to other files due to category changes
            // * More tidy
            // * If a command is removed from Loritta, removing the locales is a breeze because you just need to delete the locale key related to the command!
            //
            // Very nice :3
            //
            // So, first, we will check if the commands folder exist and, if it is, we are going to load all the files within the folder and apply a
            // auto prefix to it.
            val commandsLocaleFolder = File(localeFolder, "commands")
            if (commandsLocaleFolder.exists())
                loadFromFolder(commandsLocaleFolder) { "commands.command.${it.nameWithoutExtension}." }
        }

        // Before we say "okay everything is OK! Let's go!!" we are going to format every single string on the locale
        // to check if everything is really OK
        for ((key, string) in locale.localeStringEntries) {
            try {
                string?.format()
            } catch (e: IllegalArgumentException) {
                logger.error("String \"$string\" stored in \"$key\" from $id can't be formatted! If you are using {...} formatted placeholders, do not forget to add \\' before and after the placeholder!")
                throw e
            }
        }

        return locale
    }

    /**
     * Initializes the available locales and adds missing translation strings to non-default languages
     *
     * @see BaseLocale
     */
    fun loadLocales() {
        val locales = mutableMapOf<String, BaseLocale>()

        val defaultLocale = loadLocale(DEFAULT_LOCALE_ID, null)
        locales[DEFAULT_LOCALE_ID] = defaultLocale

        localesFolder.listFiles().filter { it.isDirectory && it.name != DEFAULT_LOCALE_ID && !it.name.startsWith(".") /* ignorar .git */ && it.name != "legacy" /* Do not try to load legacy locales */ }.forEach {
            locales[it.name] = loadLocale(it.name, defaultLocale)
        }

        for ((localeId, locale) in locales) {
            val languageInheritsFromLanguageId = locale["loritta.inheritsFromLanguageId"]

            if (languageInheritsFromLanguageId != DEFAULT_LOCALE_ID) {
                // Caso a linguagem seja filha de outra linguagem que não seja a default, nós iremos recarregar a linguagem usando o pai correto
                // Isso é útil já que linguagens internacionais seriam melhor que dependa de "en-us" em vez de "default".
                // Também seria possível implementar "linguagens auto geradas" com overrides específicos, por exemplo: "auto-en-us" -> "en-us"
                locales[localeId] = loadLocale(localeId, locales[languageInheritsFromLanguageId])
            }
        }

        this.locales = locales
    }

    /**
     * Gets the BaseLocale from the ID, if the locale doesn't exist, the default locale ("default") will be retrieved
     *
     * @param localeId the ID of the locale
     * @return         the locale on BaseLocale format or, if the locale doesn't exist, the default locale will be loaded
     * @see            LegacyBaseLocale
     */
    fun getLocaleById(localeId: String): BaseLocale {
        return locales.getOrDefault(localeId, locales[DEFAULT_LOCALE_ID]!!)
    }
}