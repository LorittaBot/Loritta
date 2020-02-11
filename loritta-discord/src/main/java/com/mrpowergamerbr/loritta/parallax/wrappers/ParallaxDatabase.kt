package com.mrpowergamerbr.loritta.parallax.wrappers

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ParallaxMetaStorage
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.ParallaxMetaStorages
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.jsonParser
import net.dv8tion.jda.api.entities.Guild
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.function.Function

class ParallaxDatabase(private val guild: Guild) {
    fun storage(storageName: String): ParallaxPromise<ParallaxStorage> {
        if (storageName.isEmpty())
            throw IllegalArgumentException("$storageName is too short!")

        if (storageName.length > 24)
            throw IllegalArgumentException("$storageName is too long!")

        return object: ParallaxPromise<ParallaxStorage>() {
            override fun queue(success: Function<ParallaxStorage, Any?>?, failure: Function<Any?, Any?>?) {
                // Get from db
                val storage = transaction(Databases.loritta) {
                    ParallaxStorage(guild, storageName,
                            ParallaxMetaStorage.find {
                                ParallaxMetaStorages.guildId eq guild.idLong and
                                        (ParallaxMetaStorages.storageName eq storageName)
                            }.firstOrNull()
                    )
                }

                success?.apply(storage)
            }
        }
    }

    fun delete(storageName: String): ParallaxPromise<Void?> {
        if (storageName.isEmpty())
            throw IllegalArgumentException("$storageName is too short!")

        if (storageName.length > 24)
            throw IllegalArgumentException("$storageName is too long!")

        return object: ParallaxPromise<Void?>() {
            override fun queue(success: Function<Void?, Any?>?, failure: Function<Any?, Any?>?) {
                // Get from db
                transaction(Databases.loritta) {
                    ParallaxMetaStorages.deleteWhere {
                        ParallaxMetaStorages.guildId eq guild.idLong and
                                (ParallaxMetaStorages.storageName eq storageName)
                    }
                }

                success?.apply(null)
            }
        }
    }

    fun deleteAll(): ParallaxPromise<Void?> {
        return object: ParallaxPromise<Void?>() {
            override fun queue(success: Function<Void?, Any?>?, failure: Function<Any?, Any?>?) {
                // Get from db
                transaction(Databases.loritta) {
                    ParallaxMetaStorages.deleteWhere {
                        ParallaxMetaStorages.guildId eq guild.idLong
                    }
                }

                success?.apply(null)
            }
        }
    }

    class ParallaxStorage(private val guild: Guild, private val storageName: String, private var backed: ParallaxMetaStorage?) {
        private val data: JsonObject = if (backed == null)
            jsonObject()
        else
            jsonParser.parse(backed!!.data).obj

        fun set(path: String, value: Any) {
            data[path] = value
        }

        fun get(path: String): Any? {
            return data[path].nullString
        }

        fun save(): ParallaxPromise<Void?> {
            return object: ParallaxPromise<Void?>() {
                override fun queue(success: Function<Void?, Any?>?, failure: Function<Any?, Any?>?) {
                    transaction(Databases.loritta) {
                        if (backed == null) {
                            backed = ParallaxMetaStorage.new {
                                this.guildId = guild.idLong
                                this.storageName = this@ParallaxStorage.storageName
                                this.data = gson.toJson(this@ParallaxStorage.data)
                            }
                        } else {
                            backed!!.data = gson.toJson(this@ParallaxStorage.data)
                        }
                    }

                    success?.apply(null)
                }
            }
        }
    }
}