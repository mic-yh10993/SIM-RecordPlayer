# SIM-RecordPlayer

Minecraft Fabric 1.21.1 唱片机 Mod — 每首歌一个唱片机方块，音频嵌入 JAR，支持 GitHub Actions 自动编译。

## 功能

- 将 MP3 文件放入 `music/` 文件夹，自动生成对应的唱片机方块（代码、模型、语言文件等全部由脚本生成）
- 音频文件嵌入 JAR，无需额外放置外部文件
- 右键放置唱片机方块，再次右键开始/停止播放
- 32 格音量范围，22 格开始线性衰减
- 自动循环播放
- 独立物品栏分组
- 支持合成配方（铁锭 + 红石 + 音符盒 + 木板）

## 快速开始

### 安装

1. 安装 [Fabric Loader](https://fabricmc.net/)（≥0.16.14）和 [Fabric API](https://modrinth.com/mod/fabric-api)（≥0.109.0）
2. 从 [build/output/](build/output/) 下载编译好的 JAR
3. 将 JAR 放入 Minecraft 的 `mods` 文件夹

### 使用

1. 在创造模式物品栏中搜索「S_I_M唱片机」分组
2. 选择对应歌曲的唱片机方块放置到世界中
3. 右键点击唱片机开始播放，再次右键停止
4. 靠近唱片机音量增大，远离音量减小，32 格外听不到

## 添加音乐教程

### 步骤 1：准备 MP3 文件

将 MP3 文件放入项目根目录下的 `music/` 文件夹：

```
SIM-RecordPlayer/
  music/
    Moon Halo.mp3
    HOYO-MiX - 心忆 Remembrance of the Heart.mp3
    青空.mp3
```

### 步骤 2：运行代码生成脚本

```bash
python3 scripts/generate_mod.py
```

脚本会扫描 `music/` 文件夹，自动生成：
- 方块注册代码 (`ModBlocks.java`)
- 物品注册代码 (`ModItems.java`)
- 客户端音频管理代码 (`RecordPlayerClientManager.java`)
- 方块状态、模型、语言文件、掉落表、合成配方

### 步骤 3：构建

```bash
./gradlew build
```

生成的 JAR 位于 `build/libs/SIM-RecordPlayer-1.0.0.jar`

### 步骤 4：提交到 GitHub

```bash
git add music/ src/
git commit -m "Add new songs"
git push
```

GitHub Actions 会自动执行上述流程并把编译好的 JAR 提交到 `build/output/`。

## 文件命名规则

### MP3 文件名 → 唱片机名称

| MP3 文件名 | 唱片机名称 |
|-----------|-----------|
| `Moon Halo.mp3` | 唱片机 - Moon Halo |
| `心忆.mp3` | 唱片机 - 心忆 |
| `轻涟 La vaguelette.mp3` | 唱片机 - 轻涟 La vaguelette |

### MP3 文件名 → 游戏内 ID

| MP3 文件名 | 游戏内 ID |
|-----------|-----------|
| `Moon Halo.mp3` | `simrecordplayer:record_player_moon_halo` |
| `心忆.mp3` | `simrecordplayer:record_player_心忆` |
| `轻涟 La vaguelette.mp3` | `simrecordplayer:record_player_轻涟_la_vaguelette` |
| `青空.mp3` | `simrecordplayer:record_player_qingkong` |

### 命名规则

- 文件名转小写，空格转下划线，仅保留 `[a-z0-9_]`
- 数字开头加前缀 `s_`
- 纯中文文件名使用 MD5 哈希前 8 位（如 `song_cb29d761`）

## 项目结构

```
SIM-RecordPlayer/
├── .github/workflows/build.yml      # GitHub Actions 工作流
├── scripts/generate_mod.py          # 代码生成脚本（扫描 music/）
├── music/                           # MP3 文件放这里（代码生成的输入源）
├── libs/
│   └── jlayer-1.0.1.jar             # MP3 解码库（打包进 JAR）
├── src/main/java/com/sim/recordplayer/
│   ├── SIMRecordPlayer.java         # Mod 入口
│   ├── audio/
│   │   ├── AudioPlayer.java         # 音频播放器（从 JAR classpath 读取 MP3）
│   │   └── VolumeAudioDevice.java   # 音量控制
│   ├── block/
│   │   ├── RecordPlayerBlock.java       # 唱片机方块
│   │   ├── RecordPlayerBlockEntity.java # 方块实体
│   │   └── ModBlocks.java              # [自动生成] 方块注册
│   ├── item/
│   │   ├── RecordPlayerBlockItem.java  # 方块物品
│   │   └── ModItems.java              # [自动生成] 物品注册
│   └── client/
│       ├── SIMRecordPlayerClient.java  # 客户端入口
│       └── RecordPlayerClientManager.java # [自动生成] 音频管理
└── src/main/resources/
    ├── assets/simrecordplayer/
    │   ├── music/                  # [自动生成] MP3 资源（打包进 JAR）
    │   ├── blockstates/           # [自动生成]
    │   ├── models/block/          # [自动生成]
    │   ├── models/item/           # [自动生成]
    │   ├── lang/                  # [自动生成] 中文 + 英文
    │   └── textures/block/        # 方块纹理（手动维护）
    └── data/simrecordplayer/
        ├── loot_tables/blocks/    # [自动生成]
        └── recipes/               # [自动生成]
```

## 自定义方块纹理

方块纹理位于 `src/main/resources/assets/simrecordplayer/textures/block/`：
- `record_player_top.png` — 顶面
- `record_player_side.png` — 侧面
- `record_player_bottom.png` — 底面

所有唱片机共用同一套纹理。

## 技术细节

- MC 1.21.1 + Fabric Loader 0.16.14 + Fabric API 0.109.0+1.21.1
- Fabric Loom 1.8 + Java 21
- jlayer 1.0.1（bundled in JAR）进行 MP3 解码
- 音频从 JAR classpath 资源读取（`assets/simrecordplayer/music/`），不依赖外部文件系统
- `VolumeAudioDevice` 继承 `JavaSoundAudioDevice`，在 PCM 写入时应用音量缩放
- 每个客户端独立扫描方块状态、独立播放、独立计算音量
- 方块 `playing` 状态通过 Minecraft 内置机制（flag 3）同步到客户端
- GitHub Actions 使用 `permissions: contents: write` + `git add -f` 绕过 `.gitignore` 提交 JAR 到 `build/output/`

## 许可

MIT License
