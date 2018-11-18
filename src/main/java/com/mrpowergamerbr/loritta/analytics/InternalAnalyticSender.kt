package com.mrpowergamerbr.loritta.analytics

import com.mrpowergamerbr.loritta.utils.debug.DebugLog

/**
 * Sends internal analytics to the console
 */
class InternalAnalyticSender : Runnable {
	override fun run() {
		DebugLog.showExtendedInfo()
	}
}