package net.perfectdreams.loritta.utils

import net.perfectdreams.loritta.common.utils.placeholders.PlaceholderSectionType
import net.perfectdreams.loritta.common.utils.placeholders.SectionPlaceholders
import org.junit.jupiter.api.Test

class PlaceholderSectionsTest {
    @Test
    fun `test if all placeholder section types have a section placeholder`() {
        for (type in PlaceholderSectionType.entries) {
            val section = SectionPlaceholders.sections.firstOrNull { it.type == type }
            if (section == null)
                error("Placeholder Section Type $type does NOT have any sections!")
        }
    }

    @Test
    fun `test if there aren't any duplicated sections`() {
        val dupes = SectionPlaceholders.sections.groupBy { it.type }
            .filter { it.value.size > 1 }

        if (dupes.isNotEmpty())
            error("There are duplicated placeholder sections! $dupes")
    }
}