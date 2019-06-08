package com.mrpowergamerbr.loritta.modules

import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale

interface MessageReceivedModule {
	/**
	 * If the module should be executed
	 *
	 * @param event        the guild message received event
	 * @param lorittaUser  the message sender
	 * @param serverConfig the server configuration
	 * @return             if the event should be handled
	 */
	fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean

	/**
	 * What the module should do when it is executed
	 *
	 * @param event        the guild message received event
	 * @param lorittaUser  the message sender
	 * @param serverConfig the server configuration
	 * @return             if true, the original event should be cancelled and nothing else should be processed
	 */
	suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean
}