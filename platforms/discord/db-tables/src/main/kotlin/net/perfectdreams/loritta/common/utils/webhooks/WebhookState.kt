package net.perfectdreams.loritta.common.utils.webhooks

enum class WebhookState {
    SUCCESS,
    MISSING_PERMISSION,
    UNKNOWN_CHANNEL,

    /**
     * Used when a webhook triggers a "Unknown Webhook", this should cause a retry with no consequences.
     */
    UNKNOWN_WEBHOOK_PHASE_1,

    /**
     * Used when a webhook triggers a "Unknown Webhook" more than once, a new webhook should NOT be retrieved because the guild may
     * have a bot that automatically deletes webhooks, so it is better to wait for a while before retrying.
     */
    UNKNOWN_WEBHOOK_PHASE_2
}