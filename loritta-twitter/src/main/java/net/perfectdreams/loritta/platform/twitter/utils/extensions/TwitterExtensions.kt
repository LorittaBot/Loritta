package net.perfectdreams.loritta.platform.twitter.utils.extensions

import twitter4j.Status
import twitter4j.StatusUpdate
import twitter4j.Twitter

fun Status.reply(twitter: Twitter, text: String, builder: (StatusUpdate.() -> (Unit))? = null): Status {
    val stat = StatusUpdate("@${this.user.screenName} " + text)
    stat.inReplyToStatusId = this.id
    builder?.invoke(stat)
    return twitter.updateStatus(stat)
}