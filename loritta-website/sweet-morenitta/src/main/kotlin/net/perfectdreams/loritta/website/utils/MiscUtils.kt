package net.perfectdreams.loritta.website.utils

import net.perfectdreams.loritta.utils.locale.BaseLocale

object MiscUtils {
    fun getFurryLocale(defaultLocale: BaseLocale): BaseLocale {
        val locale = BaseLocale("furry")

        defaultLocale.localeEntries.forEach {
            if (it.value is String) {
                locale.localeEntries[it.key] = (it.value as String)
                    .replace("ort", "pawrt")
                    .replace("art", "aww")
                    .replace("do", "dowo")
            } else {
                if (it.value is List<*>) {
                    locale.localeEntries[it.key] = (it.value as List<String>).map {
                        it.replace("ort", "pawrt")
                        .replace("art", "aww")
                        .replace("do", "dowo")
                    }
                } else {
                    locale.localeEntries[it.key] = it.value
                }
            }
        }

        locale.localeEntries["website.localePath"] = "furry"

        return locale
    }
}