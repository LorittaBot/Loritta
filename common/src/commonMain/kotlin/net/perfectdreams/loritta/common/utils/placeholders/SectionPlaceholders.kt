package net.perfectdreams.loritta.common.utils.placeholders

sealed interface SectionPlaceholders<T : MessagePlaceholder> {
    companion object {
        val sections = listOf<SectionPlaceholders<*>>(
            JoinMessagePlaceholders,
            LeaveMessagePlaceholders,
            TwitchStreamOnlineMessagePlaceholders,
            BlueskyPostMessagePlaceholders,
            YouTubePostMessagePlaceholders
        )

        init {
            sections.groupBy { it.type }
                .forEach {
                    if (it.value.size != 1)
                        error("There are ${it.value.size} sections using type ${it.key}, this is NOT ALLOWED!")
                }
        }
    }

    val type: PlaceholderSectionType
    val placeholders: List<T>
}