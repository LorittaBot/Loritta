package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import io.mockk.every
import io.mockk.mockk
import net.dv8tion.jda.api.entities.User
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
		val loriReply = LoriReply(message = "Hello World!")

		assertThat(loriReply.build(user)).isEqualTo(Constants.LEFT_PADDING + " **|** <@123170274651668480> Hello World!")
	}

	@Test
	fun `lori reply with custom prefix`() {
		val loriReply = LoriReply(message = "Hello World!", prefix = "<:whatdog:388420518107414528>")

		assertThat(loriReply.build(user)).isEqualTo("<:whatdog:388420518107414528> **|** <@123170274651668480> Hello World!")
	}

	@Test
	fun `lori reply without mention`() {
		val loriReply = LoriReply(message = "Hello World!", mentionUser = false)

		assertThat(loriReply.build(user)).isEqualTo(Constants.LEFT_PADDING + " **|** Hello World!")
	}

	@Test
	fun `lori reply without mention and with custom prefix`() {
		val loriReply = LoriReply(message = "Hello World!", mentionUser = false, prefix = "<:whatdog:388420518107414528>")

		assertThat(loriReply.build(user)).isEqualTo("<:whatdog:388420518107414528> **|** Hello World!")
	}
}