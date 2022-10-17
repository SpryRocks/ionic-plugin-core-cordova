package com.ionic.plugin.android.cordova.core

import com.ionic.plugin.core.actions.Delegate as CoreDelegate
import com.ionic.plugin.android.cordova.core.actions.Delegate
import java.util.concurrent.Executors

abstract class Plugin<TActionKey, TDelegate : CoreDelegate> :
    com.ionic.plugin.android.core.Plugin<TActionKey, TDelegate>(), Delegate {
    override val threadPool = Executors.newCachedThreadPool()!! // move to wrapper
}
