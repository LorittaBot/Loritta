package net.perfectdreams.loritta.placeholders.sections

import net.perfectdreams.loritta.placeholders.sections.SectionPlaceholder

sealed interface SectionPlaceholders<T : SectionPlaceholder> {
    val placeholders: List<SectionPlaceholder>
}