package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.utils.Jankenpon
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JankenponTest {
	@Test
	fun `check jankenpon win status`() {
		assertThat(Jankenpon.PAPER.getStatus(Jankenpon.ROCK)).isEqualTo(Jankenpon.JankenponStatus.WIN)
		assertThat(Jankenpon.ROCK.getStatus(Jankenpon.SCISSORS)).isEqualTo(Jankenpon.JankenponStatus.WIN)
		assertThat(Jankenpon.SCISSORS.getStatus(Jankenpon.PAPER)).isEqualTo(Jankenpon.JankenponStatus.WIN)
	}

	@Test
	fun `check jankenpon draw status`() {
		assertThat(Jankenpon.PAPER.getStatus(Jankenpon.PAPER)).isEqualTo(Jankenpon.JankenponStatus.DRAW)
		assertThat(Jankenpon.ROCK.getStatus(Jankenpon.ROCK)).isEqualTo(Jankenpon.JankenponStatus.DRAW)
		assertThat(Jankenpon.SCISSORS.getStatus(Jankenpon.SCISSORS)).isEqualTo(Jankenpon.JankenponStatus.DRAW)
	}

	@Test
	fun `check jankenpon lose status`() {
		assertThat(Jankenpon.PAPER.getStatus(Jankenpon.SCISSORS)).isEqualTo(Jankenpon.JankenponStatus.LOSE)
		assertThat(Jankenpon.ROCK.getStatus(Jankenpon.PAPER)).isEqualTo(Jankenpon.JankenponStatus.LOSE)
		assertThat(Jankenpon.SCISSORS.getStatus(Jankenpon.ROCK)).isEqualTo(Jankenpon.JankenponStatus.LOSE)
	}
}