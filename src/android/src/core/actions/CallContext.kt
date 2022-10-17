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

class CallContext(
    jsonArray: JSONArray,
    private val callbackContext: CallbackContext,
    wrapperDelegate: WrapperDelegate,
    private val errorMapper: IErrorMapper,
) : CallContext(wrapperDelegate) {
    private val jsonObject = jsonArray.getJSONObject(0)

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

    override fun optString(name: String) = nullable(name) { jsonObject.getString(name) }

    override fun optInt(name: String) = nullable(name) { jsonObject.getInt(name) }

    override fun optBoolean(name: String) = nullable(name) { jsonObject.getBoolean(name) }

    override fun optFloat(name: String) = nullable(name) { jsonObject.getDouble(name).toFloat() }

    override fun optDouble(key: String) = nullable(key) { jsonObject.getDouble(key) }

    override fun optJsonObject(name: String) = nullable(name) {
        val jsonString = jsonObject.getJSONObject(name).toString()
        return@nullable JsonObject.fromJson(jsonString)
    }

    override fun optLong(name: String) = nullable(name) { jsonObject.getLong(name) }

    override fun optNumber(name: String) = throw NotImplementedError()

    override fun optJsonArray(name: String) = nullable(name) {
        val jsonString = jsonObject.getJSONArray(name).toString()
        return@nullable JsonArray.fromJson(jsonString)
    }

    private fun <T> nullable(key: String, getter: () -> T): T? {
        if (jsonObject.isNull(key)) return null
        return getter()
    }

    private fun <T> require(name: String, block: (name: String) -> T?) =
        block(name) ?: throw Exception("value with name '${name}' is null")

    override fun result(result: CallContextResult, finish: Boolean) {
//        if (!finish) callbackContext.k
        when (result) {
            is CallContextResult.Success -> success(result.data, finish)
            is CallContextResult.Error -> error(result.error)
            else -> error(null)
        }
    }

    private fun success(data: JsonObject?, finish: Boolean) {
        val jsonObject = data?.toJSONObject()
        val result = if (jsonObject == null)
            PluginResult(PluginResult.Status.OK)
        else
            PluginResult(PluginResult.Status.OK, jsonObject)

        callbackContext.sendPluginResult(result)
    }

    private fun error(error: Throwable?) {
        val exception: Exception? = when (error) {
            is Exception -> error
            is Throwable -> Exception(error)
            else -> null
        }

        val defaultMessage = "Unknown error"

        val message = exception?.message ?: defaultMessage
        val json = (exception as? PluginException)?.let(errorMapper::mapToJson)

        val result = MutableJsonObject()
        result.put("message", message)
        if (json != null) {
            result.put("status", json)
        }

        callbackContext.error(result.toJSONObject())
    }
}
