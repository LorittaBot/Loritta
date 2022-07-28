package net.perfectdreams.loritta.cinnamon.utils

enum class PendingImportantNotificationState {
    PENDING,
    SUCCESSFULLY_SENT_VIA_DIRECT_MESSAGE,
    FAILED_TO_SEND_VIA_DIRECT_MESSAGE,

    @Deprecated("This is unused")
    SKIPPED_DIRECT_MESSAGE,

    @Deprecated("This is unused")
    SUCCESSFULLY_SENT_VIA_EPHEMERAL_MESSAGE
}