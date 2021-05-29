package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.messages.LorittaReply
import io.mockk.every
import io.mockk.mockk
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.platform.discord.legacy.entities.jda.JDAUser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LoriReplyTest {
	val user = mockk<User>()
	val userId = "123170274651668480"

	init {
		every { user.asMention } returns "<@$userId>"
	}

	@Test
	fun `simple lori reply`() {
		val loriReply = LorittaReply(message = "Hello World!")

		assertThat(loriReply.build(JDAUser(user))).isEqualTo(Constants.LEFT_PADDING + " **|** <@123170274651668480> Hello World!")
	}

	@Test
	fun `lori reply with custom prefix`() {
		val loriReply = LorittaReply(message = "Hello World!", prefix = "<:whatdog:388420518107414528>")

		assertThat(loriReply.build(JDAUser(user))).isEqualTo("<:whatdog:388420518107414528> **|** <@123170274651668480> Hello World!")
	}

	@Test
	fun `lori reply without mention`() {
		val loriReply = LorittaReply(message = "Hello World!", mentionUser = false)

		assertThat(loriReply.build(JDAUser(user))).isEqualTo(Constants.LEFT_PADDING + " **|** Hello World!")
	}

	@Test
	fun `lori reply without mention and with custom prefix`() {
		val loriReply = LorittaReply(message = "Hello World!", mentionUser = false, prefix = "<:whatdog:388420518107414528>")

		assertThat(loriReply.build(JDAUser(user))).isEqualTo("<:whatdog:388420518107414528> **|** Hello World!")
	}
}