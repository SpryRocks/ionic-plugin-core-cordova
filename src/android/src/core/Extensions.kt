package com.ionic.plugin.android.cordova.core

import com.spryrocks.kson.JsonObject
import org.json.JSONObject

fun JsonObject.toJSONObject() = JSONObject(this.toString())
