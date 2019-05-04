package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator

class MongoDbConfig @JsonCreator constructor(
        val databaseName: String,
        val address: String
)