package net.perfectdreams.loritta.common.utils.math

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

object Easings {
    fun easeInOutSine(x: Double): Double {
        return -(cos(PI * x) - 1) / 2
    }

    fun easeInSine(x: Double): Double {
        return 1 - cos((x * PI) / 2)
    }

    fun easeOutSine(x: Double): Double {
        return sin((x * PI) / 2)
    }

    fun easeLinear(start: Double, end: Double, percent: Double): Double {
        return start+(end-start)*percent
    }

    fun easeLinear(start: Int, end: Int, percent: Double): Int {
        return (start+(end-start)*percent).toInt()
    }

    /**
     * @param bounciness controls the "bounciness" of the easing, more bounciness = more "bounces" in the easing. However, the amount of bounciness does NOT correlate to how many bounces are in the easing! The default is 2.0
     * @param bouncinessLength controls the "bouncinessLength" of the easing. This also controls the intensity of the bounce because the bounces are "stretched". The default is 3.0
     * @param bounceIntensity controls the intensity of the bounces, lower values equals to more bounce intensity. Default value 2.0
     */
    fun easeOutElastic(
        x: Double,
        bounciness: Double = 2.0,
        bouncinessLength: Double = 3.0,
        bounceIntensity: Double = 2.0
    ): Double {
        val c4 = (bounciness * PI) / bouncinessLength

        return when {
            x == 0.0 -> 0.0
            x == 1.0 -> 1.0
            else -> (bounceIntensity.pow(-10 * x) * sin((x * 10 - 0.75) * c4)) + 1
        }
    }
}