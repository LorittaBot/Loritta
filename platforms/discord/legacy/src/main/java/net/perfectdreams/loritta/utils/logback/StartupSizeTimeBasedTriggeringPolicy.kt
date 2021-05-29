package net.perfectdreams.loritta.utils.logback

import ch.qos.logback.core.joran.spi.NoAutoStart
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP
import java.io.File

@NoAutoStart
class StartupSizeTimeBasedTriggeringPolicy<E> : SizeAndTimeBasedFNATP<E>() {
	private var startedX = false

	override fun isTriggeringEvent(activeFile: File?, event: E): Boolean {
		if (!startedX) {
			nextCheck = 0L
			return true.also { startedX = it }
		}
		return super.isTriggeringEvent(activeFile, event)
	}
}