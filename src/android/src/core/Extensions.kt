package com.ionic.plugin.android.cordova.core

import com.spryrocks.kson.JsonArray
import com.spryrocks.kson.JsonObject
import org.json.JSONArray
import org.json.JSONObject

fun JsonObject.toJSONObject() = JSONObject(this.toString())
fun JsonArray.toJSONArray() = JSONArray(this.toString())
