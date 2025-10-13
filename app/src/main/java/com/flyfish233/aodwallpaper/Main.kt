package com.flyfish233.aodwallpaper

import android.util.Log
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.makeAccessible
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import java.lang.ref.WeakReference
import java.lang.reflect.Method

private const val TAG = "AoDWallpaper"

@InjectYukiHookWithXposed
class Main : IYukiHookXposedInit {

    private object Config {
        const val SCRIM_BEHIND_ALPHA = 0.7f // 0..1（越小越透）
    }

    private object State {
        @Volatile
        var inAod = false
    }

    private object Holders {
        @Volatile
        var scrimBehindRef: WeakReference<Any>? = null
        fun isBehindScrim(obj: Any?) = obj != null && scrimBehindRef?.get() === obj
        fun resetBehindScrimAlphaToZero() {
            val v = scrimBehindRef?.get() ?: return
            runCatching {
                val m = v.javaClass.resolve().firstMethod {
                        name = "setViewAlpha"
                        parameters(Float::class.javaPrimitiveType!!)
                    }.self
                m.makeAccessible()
                m.invoke(v, 0f)
                Log.d(TAG, "reset behind scrim alpha -> 0f")
            }.onFailure { Log.w(TAG, "reset behind scrim alpha fail: $it") }
        }
    }

    override fun onHook() = YukiHookAPI.encase {
        loadApp("com.android.systemui") {
            Log.d(TAG, "hooking SystemUI...")

            wireAodSignalsBroadly()
            forceWallpaperAlways()
            allowAmbientWallpaper()
            cacheBehindScrimView()
            clampBehindScrimAlphaInAod()

            Log.d(TAG, "hooks installed")
        }
    }

    // Precheck if method exists
    private fun PackageParam.classExists(name: String): Class<Any>? =
        runCatching { name.toClass() }.getOrNull().also {
            if (it == null) Log.d(TAG, "skip hooking: class not found -> $name")
        }

    private fun hasMethod(clazz: Class<*>, name: String, vararg params: Class<*>): Boolean =
        runCatching { clazz.getDeclaredMethod(name, *params) }.isSuccess

    private inline fun Method.hookBefore(crossinline block: (XC_MethodHook.MethodHookParam) -> Unit) {
        makeAccessible()
        XposedBridge.hookMethod(this, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) = block(param)
        })
    }

    private inline fun Method.hookAfter(crossinline block: (XC_MethodHook.MethodHookParam) -> Unit) {
        makeAccessible()
        XposedBridge.hookMethod(this, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) = block(param)
        })
    }

    // AOD 信号源
    private fun PackageParam.wireAodSignalsBroadly() {
        fun setAod(active: Boolean, reason: String) {
            val old = State.inAod
            State.inAod = active
            if (old != active) {
                Log.d(TAG, "AOD state -> $active by $reason")
                if (!active) Holders.resetBehindScrimAlphaToZero()
            }
        }

        // Oplus DozeService //
        classExists("com.android.systemui.aod.OplusDozeServiceEx")?.let { c ->
            val r = c.resolve()
            // startShow(boolean)
            r.firstMethod {
                name = "startShow"
                parameters(Boolean::class.javaPrimitiveType!!)
            }.self.hookAfter { setAod(true, "OplusDozeServiceEx.startShow") }

            // hideAod()
            r.firstMethod {
                name = "hideAod"
                emptyParameters()
            }.self.hookAfter { setAod(false, "OplusDozeServiceEx.hideAod") }

            // onDreamingStarted(boolean)
            r.firstMethod {
                name = "onDreamingStarted"
                parameters(Boolean::class.javaPrimitiveType!!)
            }.self.hookAfter {
                val start = it.args[0] as Boolean
                if (start) setAod(true, "onDreamingStarted(true)")
            }

            // onDreamingStopped()
            r.firstMethod {
                name = "onDreamingStopped"
                emptyParameters()
            }.self.hookAfter { setAod(false, "onDreamingStopped") }

            // updateAodIsShow(boolean)
            r.firstMethod {
                name = "updateAodIsShow"
                parameters(Boolean::class.javaPrimitiveType!!)
            }.self.hookAfter { setAod(it.args[0] as Boolean, "updateAodIsShow") }
        }

        // —— AOSP DozeServiceHost —— //
        classExists("com.android.systemui.statusbar.phone.DozeServiceHost")?.let { c ->
            val r = c.resolve()
            if (hasMethod(c, "startDozing")) {
                r.firstMethod {
                    name = "startDozing"
                    emptyParameters()
                }.self.hookAfter { setAod(true, "DozeServiceHost.startDozing") }
            } else Log.d(TAG, "skip hooking: DozeServiceHost.startDozing() not found")

            if (hasMethod(c, "stopDozing")) {
                r.firstMethod {
                    name = "stopDozing"
                    emptyParameters()
                }.self.hookAfter { setAod(false, "DozeServiceHost.stopDozing") }
            } else Log.d(TAG, "skip hooking: DozeServiceHost.stopDozing() not found")
        }

        // —— 解锁完成 —— //
        classExists("com.android.systemui.keyguard.KeyguardViewMediator")?.let { c ->
            c.resolve().firstMethod {
                name = "onKeyguardExitFinished"
                emptyParameters()
            }.self.hookAfter { setAod(false, "KeyguardViewMediator.onKeyguardExitFinished") }
        }
    }

    /** 1) 放行壁纸 */
    private fun PackageParam.forceWallpaperAlways() {
        // shouldShowWallpaperWhileEnterAod(): boolean
        classExists("com.oplus.systemui.statusbar.phone.ScrimStateExImp")?.let { c ->
            c.resolve().firstMethod {
                name = "shouldShowWallpaperWhileEnterAod"
                emptyParameters()
            }.self.hookBefore {
                Log.d(TAG, "shouldShowWallpaperWhileEnterAod -> true")
                it.result = true
            }
        }

        // shouldShowWallpaper(bool, NotificationShadeWindowState, KeyguardViewMediator): boolean
        classExists("com.oplus.systemui.shade.OplusNotificationShadeWindowControllerExImpl")?.let { c ->
            c.resolve().firstMethod {
                name = "shouldShowWallpaper"
                parameters(
                    Boolean::class.javaPrimitiveType!!,
                    "com.android.systemui.shade.NotificationShadeWindowState".toClass(),
                    "com.android.systemui.keyguard.KeyguardViewMediator".toClass()
                )
            }.self.hookBefore {
                Log.d(TAG, "shouldShowWallpaper(...) -> true")
                it.result = true
            }
        }

        // BaseWallpaperHelper.hide() in AOD 拦截
        classExists("com.oplus.systemui.keyguard.wallpaper.BaseWallpaperHelper")?.let { c ->
            c.resolve().firstMethod {
                name = "hide"
                emptyParameters()
            }.self.hookBefore {
                if (State.inAod) {
                    Log.d(TAG, "intercept BaseWallpaperHelper.hide() in AOD")
                    it.result = null // void 返回，阻止原方法
                }
            }
        }
    }

    /** 2) Ambient 允许 */
    private fun PackageParam.allowAmbientWallpaper() {
        classExists("com.android.systemui.statusbar.phone.ScrimController")?.let { c ->
            c.resolve().firstMethod {
                name = "setWallpaperSupportsAmbientMode"
                parameters(Boolean::class.javaPrimitiveType!!)
            }.self.hookBefore {
                if (!(it.args[0] as Boolean)) {
                    it.args[0] = true
                    Log.d(TAG, "setWallpaperSupportsAmbientMode(true)")
                }
            }
        }
    }

    /** 3) 缓存 behind scrim 实例 */
    private fun PackageParam.cacheBehindScrimView() {
        classExists("com.android.systemui.statusbar.phone.ScrimController")?.let { c ->
            c.resolve().firstMethod {
                name = "getScrimBehind"
                emptyParameters()
            }.self.hookAfter {
                val obj = it.result
                Holders.scrimBehindRef = WeakReference(obj)
                Log.d(TAG, "cache getScrimBehind -> $obj")
            }
        }
    }

    /** 4) AOD 时钳制 behind scrim 透明度 */
    private fun PackageParam.clampBehindScrimAlphaInAod() {
        val want = Config.SCRIM_BEHIND_ALPHA.coerceIn(0f, 1f)

        // ScrimView#setViewAlpha(float)
        classExists("com.android.systemui.scrim.ScrimView")?.let { c ->
            c.resolve().firstMethod {
                name = "setViewAlpha"
                parameters(Float::class.javaPrimitiveType!!)
            }.self.hookBefore {
                val self = it.thisObject
                val orig = (it.args[0] as Float)
                if (State.inAod && Holders.isBehindScrim(self)) {
                    it.args[0] = want
                    Log.d(TAG, "ScrimView#setViewAlpha(behind) $orig -> $want (AOD)")
                }
            }
        }

        // ScrimController#setScrimBehindValues(float)
        classExists("com.android.systemui.statusbar.phone.ScrimController")?.let { c ->
            c.resolve().firstMethod {
                name = "setScrimBehindValues"
                parameters(Float::class.javaPrimitiveType!!)
            }.self.hookBefore {
                if (State.inAod) {
                    val orig = (it.args[0] as Float)
                    it.args[0] = want
                    Log.d(TAG, "setScrimBehindValues $orig -> $want (AOD)")
                }
            }
        }
    }
}