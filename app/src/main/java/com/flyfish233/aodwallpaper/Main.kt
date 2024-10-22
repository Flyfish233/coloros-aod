package com.flyfish233.aodwallpaper

import android.provider.Settings
import android.util.Log
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

// const val ENABLE_LOG = false

@InjectYukiHookWithXposed
class Main : IYukiHookXposedInit {
    override fun onInit() = configs {}

    override fun onHook() = encase {
        loadApp("com.android.systemui") {
            Log.i("AoDWallpaper", "HookEntry onInit")

            // 开启 AoD 壁纸
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

            // 强制触发 LTPO
            "com.oplus.systemui.aod.display.BaseDisplayUtil".toClass().apply {
                constructor().hook {
                    Log.i("AoDWallpaper", "Hook BaseDisplayUtil constructor")
                    before {
                        Log.i("AoDWallpaper", "Force 1Hz")
                        Settings.Secure.putInt(
                            appContext!!.contentResolver,
                            "Setting_AodClockModeOriginalType_ONEHZ",
                            1
                        )
                    }
                }
            }

            // Deprecated: Use `SmoothTransitionController` instead
            // "com.oplus.systemui.aod.proxy.AodSettingsValueProxy".toClass().apply {
            //     method {
            //         name = "getAodShouldShowWallpaper"
            //     }.hook {
            //         // Log.i("AoDWallpaper", "Force getAodShouldShowWallpaper=1")
            //         // val enable: Int = 1
            //         // replaceTo(enable)
            //     }
            // }

            // 启用日志
            /*
            if (ENABLE_LOG) {
                "com.oplusos.systemui.common.util.LogUtil".toClass().apply {
                    method {
                        // Log normal: Aod AODDisplayUtil --> enableLTPOAod true
                        name = "normal"
                        param(StringClass, StringClass, StringClass)
                    }.hook {
                        after {
                            val arg0 = args[0] as String
                            val arg1 = args[1] as String
                            val arg2 = args[2] as String
                            Log.d("AoDWallpaper", "$arg0 $arg1 --> $arg2")
                        }
                    }
                }
                "com.oplusos.systemui.common.util.LogUtil".toClass().apply {
                    method {
                        name = "w"
                    }.hook {
                        after {
                            val arg0 = args[0] as String
                            val arg1 = args[1] as String
                            val arg2 = args[2] as String
                            Log.d("AoDWallpaper", "$arg0 $arg1 --> $arg2")
                        }
                    }
                }
                "com.oplusos.systemui.common.util.LogUtil".toClass().apply {
                    method {
                        name = "i"
                    }.hook {
                        after {
                            val arg0 = args[0] as String
                            val arg1 = args[1] as String
                            val arg2 = args[2] as String
                            Log.d("AoDWallpaper", "$arg0 $arg1 --> $arg2")
                        }
                    }
                }
            }
             */
        }
    }
}