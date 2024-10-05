package net.perfectdreams.loritta.lorituber

import kotlinx.serialization.Serializable

@Serializable
sealed class PhoneCall {
    @Serializable
    sealed class SonhosReward(val sonhosReward: Long) : PhoneCall() {
        @Serializable
        data object SonhosRewardCall0 : SonhosReward(1_000)

        @Serializable
        data object SonhosRewardCall1 : SonhosReward(500)

        @Serializable
        data object SonhosRewardCall2 : SonhosReward(50)

        @Serializable
        data object SonhosRewardCall3 : SonhosReward(25_000)

        @Serializable
        data object SonhosRewardCall4 : SonhosReward(2_000)

        @Serializable
        data object SonhosRewardCall5 : SonhosReward(500)

        @Serializable
        data object SonhosRewardCall6 : SonhosReward(2_000)

        @Serializable
        data object SonhosRewardCall7 : SonhosReward(1_000)

        @Serializable
        data object SonhosRewardCall8 : SonhosReward(5_000)

        @Serializable
        data object SonhosRewardCall9 : SonhosReward(100)

        @Serializable
        data object SonhosRewardCall10 : SonhosReward(250)

        @Serializable
        data object SonhosRewardCall11 : SonhosReward(100)

        @Serializable
        data object SonhosRewardCall12 : SonhosReward(75)

        @Serializable
        data object SonhosRewardCall13 : SonhosReward(50)

        @Serializable
        data object SonhosRewardCall14 : SonhosReward(250)

        @Serializable
        data object SonhosRewardCall15 : SonhosReward(300)

        @Serializable
        data object SonhosRewardCall16 : SonhosReward(50)

        @Serializable
        data object SonhosRewardCall17 : SonhosReward(5_000)

        @Serializable
        data object SonhosRewardCall18 : SonhosReward(250)

        @Serializable
        data object SonhosRewardCall19 : SonhosReward(100)

        @Serializable
        data object SonhosRewardCall20 : SonhosReward(1_000)

        @Serializable
        data object SonhosRewardCall21 : SonhosReward(3_000)

        @Serializable
        data object SonhosRewardCall22 : SonhosReward(100)
    }

    @Serializable
    data object OddCall0 : PhoneCall()

    @Serializable
    data object OddCall1 : PhoneCall()

    @Serializable
    data object OddCall2 : PhoneCall()

    @Serializable
    data object OddCall3 : PhoneCall()

    @Serializable
    data object OddCall4 : PhoneCall()

    @Serializable
    data object OddCall5 : PhoneCall()

    @Serializable
    data object OddCall6 : PhoneCall()

    @Serializable
    data object OddCall7 : PhoneCall()

    @Serializable
    data object OddCall8 : PhoneCall()

    @Serializable
    data object OddCall9 : PhoneCall()

    @Serializable
    data object OddCall10 : PhoneCall()

    @Serializable
    data object OddCall11 : PhoneCall()

    @Serializable
    data object OddCall12 : PhoneCall()

    @Serializable
    data object OddCall13 : PhoneCall()

    @Serializable
    data object OddCall14 : PhoneCall()

    @Serializable
    data object OddCall15 : PhoneCall()

    @Serializable
    data object OddCall16 : PhoneCall()

    @Serializable
    data object OddCall17 : PhoneCall()

    @Serializable
    data object OddCall18 : PhoneCall()

    @Serializable
    data object OddCall19 : PhoneCall()

    @Serializable
    data object OddCall20 : PhoneCall()

    @Serializable
    data object OddCall21 : PhoneCall()

    @Serializable
    data object OddCall22 : PhoneCall()

    @Serializable
    data object OddCall23 : PhoneCall()

    @Serializable
    data object OddCall24 : PhoneCall()

    @Serializable
    data object OddCall25 : PhoneCall()

    @Serializable
    data object OddCall26 : PhoneCall()

    @Serializable
    data object OddCall27 : PhoneCall()
}