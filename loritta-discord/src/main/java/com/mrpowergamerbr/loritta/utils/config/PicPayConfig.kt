package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator

class PicPayConfig @JsonCreator constructor(
        val picPayToken: String,
		val sellerToken: String
)