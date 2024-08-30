package net.perfectdreams.loritta.morenitta.website.routes.httpapidocs

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.i18n.I18nKeysData

enum class ParameterKind(
    val title: StringI18nData,
    val postParameterPrefix: String,
    val clazzPrefix: String
) {
    PATH(I18nKeysData.DevDocs.ParameterKinds.Path, "pathparameter", "path"),
    QUERY(I18nKeysData.DevDocs.ParameterKinds.Query, "queryparameter", "query"),
    JSON_BODY(I18nKeysData.DevDocs.ParameterKinds.Json, "jsonparameter", "json"),
    FULL_BODY(I18nKeysData.DevDocs.ParameterKinds.FullBody, "fullbodyparameter", "full-body") // the post parameter prefix is unused
}