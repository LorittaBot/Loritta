package net.perfectdreams.loritta.utils.locale

import net.perfectdreams.loritta.common.locale.BaseLocale

object DebugLocales {
    private val doNotChangeThisKeys = listOf(
            "loritta.inheritsFromLanguageId",
            "commands.command.shop.localeId",
            "website.localePath"
    )

    /**
     * Creates a pseudo locale by transforming all strings (except the keys specified in the [doNotChangeThisKeys] list)
     *
     * @param locale      the base locale that the pseudo locale will be based of
     * @param newLocaleId the pseudo locale ID
     * @return            the newly created base locale with the transformed strings
     */
    fun createPseudoLocaleOf(locale: BaseLocale, newLocaleId: String, newLocaleWebsitePath: String): BaseLocale {
        val localeStringEntries = mutableMapOf<String, String?>()
        val localeListEntries = mutableMapOf<String, List<String>?>()

        for ((key, value) in locale.localeStringEntries) {
            val newValue = value?.let {
                if (key !in doNotChangeThisKeys)
                    PseudoLocalization.convertWord(value)
                else
                    value
            }

            localeStringEntries[key] = newValue
        }

        for ((key, value) in locale.localeListEntries) {
            val newValue = value?.let {
                if (key !in doNotChangeThisKeys)
                    value.map { PseudoLocalization.convertWord(it) }
                else
                    value
            }

            localeListEntries[key] = newValue
        }

        localeStringEntries["website.localePath"] = newLocaleWebsitePath

        return BaseLocale(newLocaleId, localeStringEntries, localeListEntries)
    }
}