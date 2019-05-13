package net.perfectdreams.loritta.platform.amino.utils.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class GeneralAminoConfig @JsonCreator constructor(
        val amino: AminoConfig
)