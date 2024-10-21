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

            // Deprecated: Use `SmoothTransitionController` instead
            "com.oplus.systemui.aod.proxy.AodSettingsValueProxy".toClass().apply {
                method {
                    name = "getAodShouldShowWallpaper"
                }.hook {
                    // Log.i("AoDWallpaper", "Force getAodShouldShowWallpaper=1")
                    // val enable: Int = 1
                    // replaceTo(enable)
                }
            }

            "com.oplus.systemui.aod.display.SmoothTransitionController".toClass().apply {
                method {
                    name = "shouldWindowBeTransparent"
                }.hook {
                    Log.i("AoDWallpaper", "Force shouldWindowBeTransparent=true")
                    after {
                        resultTrue()
                    }
                }
            }
        }
    }
}