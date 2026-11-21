# Item Despawn Sound & Subtitle

物品到期消失时播放自定义音效，或者在屏幕上显示字幕——适合没有外放设备、安静环境或听障玩家。

A Fabric client mod for Minecraft 1.21.11: plays a custom sound and/or shows an on-screen subtitle when an item entity despawns after its lifetime (6000 ticks / 5 minutes).

## 功能

- 检测掉落物到期消失（客户端 `ClientLevel#removeEntity` + `tickCount` 判定）
- 三种提示方式：**只声音 / 只字幕 / 声音+字幕**
- 字幕显示在快捷栏上方，最多 3 行，同名物品自动合并计数（`钻石 ×12`），淡出效果
- 自定义音轨：支持 `.ogg`（游戏解码器）和 `.mp3`（JLayer 纯 Java 解码）
- 音频文件夹 `config/itemdespawn/sounds`，支持从文件浏览器导入
- Mod Menu 图形配置界面
- 两个可自定义快捷键：打开设置、开关模组
- 配置保存在 `config/itemdespawn/config.json`

## 版本支持

| Minecraft | 代码分支 | Java | Fabric API |
|---|---|---|---|
| 1.20.1 | `mc1.20.1` | 17+ | 0.92.x+1.20.1 |
| 1.21.1 | `mc1.21.1` | 21+ | 0.116.x+1.21.1 |
| 1.21.11 | `main` | 21+ | 0.141.x+1.21.11 |

每个版本单独构建，Release 里按 `+mc<版本>` 区分 jar。

## 环境要求

| 项目 | 版本 |
|---|---|
| Minecraft | 1.20.1 / 1.21.1 / 1.21.11 |
| Fabric Loader | ≥ 0.16.0 |
| Fabric API | 必需 |
| Java | 21+ |
| Mod Menu | 17.0.1（可选，图形界面） |
| Placeholder API | Mod Menu 17 的前置 |

## 安装

1. 安装 Fabric Loader 0.20.x 和 Minecraft 1.21.11
2. 把 [Fabric API](https://modrinth.com/mod/fabric-api) 放进 `mods/`
3. 把本模组的 jar 放进 `mods/`
4. 可选：装 Mod Menu 17.0.1 + Placeholder API，就能在 Mod Menu 里打开配置
5. 进游戏后在 `选项 → 控制 → 按键绑定 → 物品消失提示` 里给快捷键设键（默认不绑定）

## 使用

### 音频文件夹

```
.minecraft/config/itemdespawn/sounds/
├── my_track.ogg
└── another.mp3
```

放进去后重新打开配置界面即可在「音轨」里选择。`.wav`、`.flac`、`.m4a` 不支持，需要先转成 ogg/mp3。

### 配置

`config/itemdespawn/config.json`：

```json
{
  "enabled": true,
  "track": "",
  "volume": 1.0,
  "pitch": 1.0,
  "minAgeTicks": 5980,
  "lenient": false,
  "cooldownMs": 250,
  "mode": "both",
  "subtitleMs": 3000
}
```

- `mode`：`sound` / `subtitle` / `both`
- `minAgeTicks`：判定为"到期消失"的最小存在时间，原版 6000
- `lenient`：宽松模式，任何掉落物消失都提示
- `cooldownMs`：两次声音之间的最小间隔（字幕不受此限制，会合并）

## 构建

需要 **JDK 25** 作为构建 JVM（Loom 1.19.4 的要求），产物目标是 Java 21。

```bash
./gradlew build
```

产物在 `build/libs/item-despawn-sound-<version>.jar`。

> 如果 Fabric 仓库连不上，可以临时把镜像仓库加到 `build.gradle` 的 `repositories` 最前面。

## 项目结构

```
src/main/java/com/mtwork/itemdespawn/
├── ItemDespawnClient.java      # 入口
├── DespawnConfig.java          # 配置读写
├── audio/                      # 音效、音轨扫描、mp3 解码
├── subtitle/                   # 屏幕字幕队列与绘制
├── gui/                        # Mod Menu 配置界面
├── keybind/                    # 可自定义快捷键
├── mixin/                      # ClientLevel / Gui / SoundBufferLibrary
└── integration/                # Mod Menu 接线
```

## 已知限制

- 客户端检测：只有掉落物在你的可视范围内消失才会触发，服务器上别人那边消失的你收不到；「宽松判定」能提高触发率
- 按 F1 隐藏 HUD 时字幕一起隐藏
- 打开聊天框或 ESC 菜单时字幕在底层渲染，可能被界面遮住
- 不支持 wav / flac / m4a

## 许可与第三方

- 本项目代码：MIT，见根目录 `LICENSE`
- 分发的 jar 内含：`LICENSE`、`THIRD-PARTY-NOTICES.md`、`LICENSES/LGPL-2.1.txt`
- mp3 解码使用 JLayer 1.0.1（LGPL-2.1），类文件嵌入 jar，许可文本一并附带

## License

MIT © 2026 Mgmt_Dept
