package net.perfectdreams.loritta.common.utils.text

import org.assertj.core.api.Assertions
import org.junit.Test

class VaporwaveTest {
    @Test
    fun `test special characters`() {
        Assertions.assertThat(
            VaporwaveUtils.vaporwave("A Loritta é muito fofa! Ela é tão fofinha que dá até vontade de abraçar ela!")
        ).isEqualTo("Ａ Ｌｏｒｉｔｔａ é ｍｕｉｔｏ ｆｏｆａ！ Ｅｌａ é ｔãｏ ｆｏｆｉｎｈａ ｑｕｅ ｄá ａｔé ｖｏｎｔａｄｅ ｄｅ ａｂｒａçａｒ ｅｌａ！")
    }
}