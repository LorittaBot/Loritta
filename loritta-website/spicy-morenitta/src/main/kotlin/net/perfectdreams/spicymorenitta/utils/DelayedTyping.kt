package net.perfectdreams.spicymorenitta.utils

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import org.w3c.dom.HTMLInputElement

var delayedTypingTask: Job? = null

fun HTMLInputElement.delayedTyping(m: SpicyMorenitta, wait: Long, onTypingStarted: () -> (Unit), onTypingEnded: () -> (Unit)) {
	this.oninput = {
		onTypingStarted.invoke()
		delayedTypingTask?.cancel()

		delayedTypingTask = m.launch {
			delay(wait)
			if (!this.isActive)
				return@launch

			onTypingEnded.invoke()
		}
		asDynamic()
	}
}