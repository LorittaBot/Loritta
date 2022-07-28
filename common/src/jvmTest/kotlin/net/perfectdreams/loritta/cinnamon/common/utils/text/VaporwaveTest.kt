package net.perfectdreams.loritta.cinnamon.utils.text

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VaporwaveTest {
    @Test
    fun `test special characters`() {
        assertThat(
            VaporwaveUtils.vaporwave("A Loritta é muito fofa! Ela é tão fofinha que dá até vontade de abraçar ela!")
        ).isEqualTo("Ａ Ｌｏｒｉｔｔａ é ｍｕｉｔｏ ｆｏｆａ！ Ｅｌａ é ｔãｏ ｆｏｆｉｎｈａ ｑｕｅ ｄá ａｔé ｖｏｎｔａｄｅ ｄｅ ａｂｒａçａｒ ｅｌａ！")
    }
}