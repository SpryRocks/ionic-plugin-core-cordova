package com.ionic.plugin.android.cordova.core.actions

import com.ionic.plugin.android.cordova.core.WrapperDelegate
import com.ionic.plugin.android.cordova.core.toJSONObject
import com.ionic.plugin.android.core.actions.CallContext
import com.ionic.plugin.core.PluginException
import com.ionic.plugin.core.actions.CallContextResult
import com.ionic.plugin.core.actions.IErrorMapper
import com.spryrocks.kson.JsonArray
import com.spryrocks.kson.JsonObject
import com.spryrocks.kson.MutableJsonObject
import org.apache.cordova.CallbackContext
import org.apache.cordova.PluginResult
import org.json.JSONArray
import org.json.JSONObject

class CallContext(
    jsonArray: JSONArray,
    private val callbackContext: CallbackContext,
    wrapperDelegate: WrapperDelegate,
    private val errorMapper: IErrorMapper,
) : CallContext(wrapperDelegate) {
    private val jsonObject: JSONObject? = jsonArray.optJSONObject(0)

    override fun get(name: String) = throw NotImplementedError()

    override fun getBoolean(name: String) = require(name, ::optBoolean)

    override fun getFloat(name: String) = require(name, ::optFloat)

    override fun getDouble(key: String) = require(key, ::optDouble)

    override fun getInt(name: String) = require(name, ::optInt)

    override fun getJsonArray(name: String) = require(name, ::optJsonArray)

    override fun getJsonObject(name: String) = require(name, ::optJsonObject)

    override fun getLong(name: String) = require(name, ::optLong)

    override fun getNumber(name: String) = throw NotImplementedError()

    override fun getString(name: String) = require(name, ::optString)

    override fun opt(name: String) = throw NotImplementedError()

    override fun optString(name: String) =
        nullable(name) { jsonObject -> jsonObject.getString(name) }

    override fun optInt(name: String) = nullable(name) { jsonObject -> jsonObject.getInt(name) }

    override fun optBoolean(name: String) =
        nullable(name) { jsonObject -> jsonObject.getBoolean(name) }

    override fun optFloat(name: String) =
        nullable(name) { jsonObject -> jsonObject.getDouble(name).toFloat() }

    override fun optDouble(key: String) = nullable(key) { jsonObject -> jsonObject.getDouble(key) }

    override fun optJsonObject(name: String) = nullable(name) { jsonObject ->
        val jsonString = jsonObject.getJSONObject(name).toString()
        return@nullable JsonObject.fromJson(jsonString)
    }

    override fun optLong(name: String) = nullable(name) { jsonObject -> jsonObject.getLong(name) }

    override fun optNumber(name: String) = throw NotImplementedError()

    override fun optJsonArray(name: String) = nullable(name) { jsonObject ->
        val jsonString = jsonObject.getJSONArray(name).toString()
        return@nullable JsonArray.fromJson(jsonString)
    }

    private fun <T> nullable(key: String, getter: (jsonObject: JSONObject) -> T): T? {
        if (jsonObject == null || jsonObject.isNull(key)) return null
        return getter(jsonObject)
    }

    private fun <T> require(name: String, block: (name: String) -> T?) =
        block(name) ?: throw Exception("value with name '${name}' is null")

    override fun result(result: CallContextResult, finish: Boolean) {
        when (result) {
            is CallContextResult.Success -> success(result.data, finish)
            is CallContextResult.Error -> error(result.error, finish)
            else -> error(null, finish)
        }
    }

    private fun success(data: JsonObject?, finish: Boolean) {
        val result = data?.toJSONObject()

        val pluginResult = if (result == null)
            PluginResult(PluginResult.Status.OK)
        else
            PluginResult(PluginResult.Status.OK, result)

        if (!finish) {
            pluginResult.keepCallback = true
        }

        callbackContext.sendPluginResult(pluginResult)
    }

    private fun error(error: Throwable?, finish: Boolean) {
        val exception: Exception? = when (error) {
            is Exception -> error
            is Throwable -> Exception(error)
            else -> null
        }

        val json = (exception as? PluginException)?.let(errorMapper::mapToJson)
        val result = (json ?: MutableJsonObject()).toJSONObject()

        val pluginResult = PluginResult(PluginResult.Status.ERROR, result)

        if (!finish) {
            pluginResult.keepCallback = true
        }

        callbackContext.sendPluginResult(pluginResult)
    }
}
