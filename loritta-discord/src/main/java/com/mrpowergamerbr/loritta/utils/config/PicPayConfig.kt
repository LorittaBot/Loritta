package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

class PicPayConfig @JsonCreator constructor(
		@JsonProperty("picpay-token")
		val picPayToken: String,
		val sellerToken: String
)