# ColorOS 全天候息屏

在 ColorOS 15 上引入的**灵感接续**息屏模式支持全天候息屏，即全局
AoD。本模块强制所有灵感接续主题在息屏时显示锁屏壁纸。息屏效果见下图，使用 ADB 截取。

[下载](https://github.com/Flyfish233/coloros-aod/releases)

## 如何使用

1. 在 Xposed 管理器中启用模块，然后重新启动设备。
2. 在**壁纸与个性化**中应用**灵感接续**息屏。
   **注意：**如果你打开了景深功能，则请避免在第三方应用内直接应用壁纸，而是下载后进入**壁纸与个性化**
   更换壁纸。这样会导致景深出现问题，因为景深是由 AI 提前生成保存的，更换第三方壁纸不会重建景深元素。
   *未打开景深或者使用了动态壁纸则忽略此项。*

## LTPO

**确保您的屏幕支持 LTPO。** 模块可以在任何设备上工作，但非 LTPO 设备则将造成耗电问题。

**请不要刷入强制刷新率模块。** 确保您的设备正确实现了 LTPO，并建议使用 LuckyTool 或其他方式打开
**启用全亮度刷新率最低为1**。

<img src="assets\PJE110.15.0.0.100.png" style="zoom:25%;" />