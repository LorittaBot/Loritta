package com.mrpowergamerbr.loritta.modules

import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

interface MessageReceivedModule {
	/**
	 * If the module should be executed
	 *
	 * @param event        the guild message received event
	 * @param lorittaUser  the message sender
	 * @param serverConfig the server configuration
	 * @return             if the event should be handled
	 */
	fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: LorittaProfile, serverConfig: ServerConfig, locale: BaseLocale): Boolean

	/**
	 * What the module should do when it is executed
	 *
	 * @param event        the guild message received event
	 * @param lorittaUser  the message sender
	 * @param serverConfig the server configuration
	 * @return             if true, the original event should be cancelled and nothing else should be processed
	 */
	fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: LorittaProfile, serverConfig: ServerConfig, locale: BaseLocale): Boolean
}