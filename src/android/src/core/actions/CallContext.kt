package com.ionic.plugin.android.cordova.core.actions

import com.ionic.plugin.android.cordova.core.WrapperDelegate
import com.ionic.plugin.android.cordova.core.toJSONArray
import com.ionic.plugin.android.cordova.core.toJSONObject
import com.ionic.plugin.android.core.actions.CallContext
import com.ionic.plugin.core.PluginException
import com.ionic.plugin.core.actions.Mappers
import com.spryrocks.kson.JsonArray
import com.spryrocks.kson.JsonObject
import com.spryrocks.kson.MutableJsonObject
import org.apache.cordova.CallbackContext
import org.apache.cordova.PluginResult
import org.json.JSONArray

class CallContext(
    private val jsonArray: JSONArray,
    private val callbackContext: CallbackContext,
    wrapperDelegate: WrapperDelegate,
    private val mappers: Mappers,
) : CallContext(wrapperDelegate) {
    override fun asArray() = AsArray(jsonArray)

    override fun asObject(): AsObject {
        throw PluginException("Not implemented")
    }

    class AsArray(private val jsonArray: JSONArray): com.ionic.plugin.core.actions.CallContext.AsArray() {
        override val size get() = jsonArray.length()

        override fun opt(index: Int) = throw NotImplementedError()

        override fun optString(index: Int) =
            nullable(index) { jsonObject -> jsonObject.getString(index) }

        override fun optInt(index: Int) = nullable(index) { jsonObject -> jsonObject.getInt(index) }

        override fun optBoolean(index: Int) =
            nullable(index) { jsonObject -> jsonObject.getBoolean(index) }

        override fun optFloat(index: Int) =
            nullable(index) { jsonObject -> jsonObject.getDouble(index).toFloat() }

        override fun optDouble(index: Int) = nullable(index) { jsonObject -> jsonObject.getDouble(index) }

        override fun optJsonObject(index: Int) = nullable(index) { jsonObject ->
            val jsonString = jsonObject.getJSONObject(index).toString()
            return@nullable JsonObject.fromJson(jsonString)
        }

        override fun optLong(index: Int) = nullable(index) { jsonObject -> jsonObject.getLong(index) }

        override fun optNumber(index: Int) = throw NotImplementedError()

        override fun optJsonArray(index: Int) = nullable(index) { jsonObject ->
            val jsonString = jsonObject.getJSONArray(index).toString()
            return@nullable JsonArray.fromJson(jsonString)
        }

        private fun <T> nullable(index: Int, getter: (jsonArray: JSONArray) -> T): T? {
            if (jsonArray.isNull(index)) return null
            return getter(jsonArray)
        }
    }

    override fun success(data: Any?, finish: Boolean) {
        val status = PluginResult.Status.OK

        val pluginResult = when(data) {
            null -> PluginResult(status)
            is String -> PluginResult(status, data)
            is JsonArray -> PluginResult(status, data.toJSONArray())
            is JsonObject -> PluginResult(status, data.toJSONObject())
            is Int -> PluginResult(status, data)
            is Float -> PluginResult(status, data)
            is Boolean -> PluginResult(status, data)
            else -> throw NotImplementedError("This data type is not supported")
        }

        if (!finish) {
            pluginResult.keepCallback = true
        }

        callbackContext.sendPluginResult(pluginResult)
    }

    override fun error(error: Throwable?, finish: Boolean) {
        val exception: Exception? = when (error) {
            is Exception -> error
            is Throwable -> Exception(error)
            else -> null
        }

        val json = (exception as? PluginException)?.let(mappers.errorMapper::mapToJson)
        val result = (json ?: MutableJsonObject()).toJSONObject()

        val pluginResult = PluginResult(PluginResult.Status.ERROR, result)

        if (!finish) {
            pluginResult.keepCallback = true
        }

        callbackContext.sendPluginResult(pluginResult)
    }
}
