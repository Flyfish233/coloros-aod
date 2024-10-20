package com.flyfish233.aodwallpaper

import android.util.Log
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

@InjectYukiHookWithXposed
class Main : IYukiHookXposedInit {
    override fun onInit() = configs {}

    override fun onHook() = encase {
        loadApp("com.android.systemui") {
            Log.i("AoDWallpaper", "HookEntry onInit")
            "com.oplus.systemui.aod.proxy.AodSettingsValueProxy".toClass().apply {
                method {
                    name = "getAodShouldShowWallpaper"
                }.hook {
                    Log.i("AoDWallpaper", "AodAlwaysWallpaper")
                    val enable: Int = 1
                    replaceTo(enable)
                }
            }
        }
    }
}