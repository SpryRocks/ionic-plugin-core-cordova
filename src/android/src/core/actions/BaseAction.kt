package com.ionic.plugin.android.cordova.core.actions

import com.ionic.plugin.android.cordova.core.WrapperDelegate
import com.ionic.plugin.core.actions.Mappers

abstract class BaseAction<TDelegate : Delegate<TMappers>, TMappers: Mappers> :
    com.ionic.plugin.android.core.actions.BaseAction<TDelegate, WrapperDelegate, TMappers>()
