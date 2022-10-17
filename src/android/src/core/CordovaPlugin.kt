package com.ionic.plugin.android.cordova.core

import android.app.Activity
import com.ionic.plugin.android.cordova.core.actions.CallContext
import com.ionic.plugin.core.actions.Delegate
import org.apache.cordova.CallbackContext
import org.json.JSONArray

abstract class CordovaPlugin<TDelegate : Delegate> : org.apache.cordova.CordovaPlugin() {
    private val plugin: com.ionic.plugin.core.Plugin<String, TDelegate>
    private val wrapperDelegate = WrapperDelegateImpl(this)

    init {
        plugin = createPlugin()
        plugin._initializePluginInternal(wrapperDelegate)
    }

    abstract fun createPlugin(): com.ionic.plugin.core.Plugin<String, TDelegate>

    override fun pluginInitialize() {
        plugin.load()
    }

    override fun execute(
        action: String,
        args: JSONArray,
        callbackContext: CallbackContext,
    ): Boolean {
        return plugin.call(action, CallContext(args, callbackContext, wrapperDelegate, plugin.errorMapper))
    }

    class WrapperDelegateImpl<TDelegate : Delegate>(private val wrapper: CordovaPlugin<TDelegate>) :
        WrapperDelegate {
        override val activity: Activity
            get() = wrapper.cordova.activity
    }
}
