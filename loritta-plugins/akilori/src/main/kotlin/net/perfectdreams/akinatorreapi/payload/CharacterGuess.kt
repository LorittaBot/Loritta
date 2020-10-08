package net.perfectdreams.akinatorreapi.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CharacterGuess(
    val id: Long,
    val name: String,
    @SerialName("id_base")
    val idBase: Int,
    @SerialName("proba")
    val probability: Float,
    val description: String?,
    @SerialName("valide_contrainte")
    val valideContrainte: Int,
    @SerialName("ranking")
    val ranking: Int,
    val pseudo: String,
    @SerialName("picture_path")
    val picturePath: String?,
    val corrupt: Int,
    val relative: Int,
    @SerialName("award_id")
    val awardId: Int,
    @SerialName("flag_photo")
    val flagPhoto: Int,
    @SerialName("absolute_picture_path")
    val absolutePicturePath: String?
)