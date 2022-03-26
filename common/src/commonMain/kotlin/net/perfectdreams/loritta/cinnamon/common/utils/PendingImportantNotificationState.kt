package net.perfectdreams.loritta.cinnamon.common.utils

enum class PendingImportantNotificationState {
    PENDING,
    SUCCESSFULLY_SENT_VIA_DIRECT_MESSAGE,
    FAILED_TO_SEND_VIA_DIRECT_MESSAGE,
    SKIPPED_DIRECT_MESSAGE,
    SUCCESSFULLY_SENT_VIA_EPHEMERAL_MESSAGE
}