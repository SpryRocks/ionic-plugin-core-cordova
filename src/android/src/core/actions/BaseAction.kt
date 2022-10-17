package com.ionic.plugin.android.cordova.core.actions

import com.ionic.plugin.android.cordova.core.WrapperDelegate

abstract class BaseAction<TDelegate : Delegate> :
    com.ionic.plugin.android.core.actions.BaseAction<TDelegate, WrapperDelegate>()
