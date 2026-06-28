# SIM-RecordPlayer

Minecraft Fabric 1.21.1 唱片机 Mod — 自动从 `music/` 文件夹生成唱片机方块，支持 GitHub Actions 自动编译。

## 功能

- 将 MP3 文件放入 `music/` 文件夹，自动为每首歌生成一个唱片机方块
- 唱片机名称自动命名为 `唱片机 - <MP3文件名（不含后缀）>`
- 右键放置，再次右键开始/停止播放
- 32 格音量范围，22 格开始线性衰减
- 独立物品栏分组
- 音频循环播放
- GitHub Actions 自动编译并发布 Release

## 快速开始

### 方式一：直接下载

1. 从 [Releases](https://github.com/你的用户名/SIM-RecordPlayer/releases) 下载最新 JAR
2. 安装 [Fabric Loader](https://fabricmc.net/) 和 [Fabric API](https://modrinth.com/mod/fabric-api)
3. 将 JAR 放入 Minecraft 的 `mods` 文件夹
4. 将 MP3 文件放入 `~/Downloads/music/`（文件名需与 Release 页面列出的一致）

### 方式二：Fork 并自定义

1. Fork 本仓库
2. 将你的 MP3 文件放入 `music/` 文件夹
3. Push 到 GitHub，Actions 会自动编译并发布 Release
4. 从 Release 下载编译好的 JAR

## 添加音乐教程

### 步骤 1：准备 MP3 文件

将 MP3 文件放入项目根目录下的 `music/` 文件夹：

```
SIM-RecordPlayer/
  music/
    Moon Halo.mp3
    心忆.mp3
    轻涟.mp3
    未来再见.mp3
```

### 步骤 2：Push 到 GitHub

```bash
git add music/
git commit -m "Add new songs"
git push
```

### 步骤 3：自动编译

GitHub Actions 会自动：
1. 扫描 `music/` 文件夹中的 MP3 文件
2. 为每首歌生成对应的方块、物品、模型、语言文件等
3. 编译 Mod
4. 发布到 Releases

### 步骤 4：安装 Mod

1. 从 Releases 下载 JAR 文件
2. 放入 Minecraft 的 `mods` 文件夹
3. 将 MP3 文件放入 `~/Downloads/music/`（文件名需与 `music/` 文件夹中的一致）

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

## 本地构建

### 环境要求

- Java 21+
- Python 3.6+
- 网络连接（首次构建需下载依赖）

### 构建步骤

```bash
# 克隆仓库
git clone https://github.com/你的用户名/SIM-RecordPlayer.git
cd SIM-RecordPlayer

# 添加 MP3 文件
cp /path/to/your/songs/*.mp3 music/

# 生成代码并构建
python3 scripts/generate_mod.py
./gradlew build
```

生成的 JAR 文件位于：`build/libs/SIM-RecordPlayer-1.0.0.jar`

## 项目结构

```
SIM-RecordPlayer/
├── .github/workflows/build.yml    # GitHub Actions 工作流
├── scripts/generate_mod.py        # 代码生成脚本
├── music/                         # MP3 文件放这里
├── src/main/java/com/sim/recordplayer/
│   ├── SIMRecordPlayer.java       # Mod 入口
│   ├── audio/
│   │   ├── AudioPlayer.java       # 音频播放器
│   │   └── VolumeAudioDevice.java # 音量控制
│   ├── block/
│   │   ├── RecordPlayerBlock.java      # 唱片机方块类
│   │   ├── RecordPlayerBlockEntity.java # 方块实体
│   │   └── ModBlocks.java              # [自动生成] 方块注册
│   ├── item/
│   │   ├── RecordPlayerBlockItem.java  # 方块物品类
│   │   └── ModItems.java               # [自动生成] 物品注册
│   └── client/
│       ├── SIMRecordPlayerClient.java  # 客户端入口
│       └── RecordPlayerClientManager.java # [自动生成] 音频管理
└── src/main/resources/
    ├── assets/simrecordplayer/
    │   ├── blockstates/           # [自动生成] 方块状态
    │   ├── models/block/          # [自动生成] 方块模型
    │   ├── models/item/           # [自动生成] 物品模型
    │   ├── lang/                  # [自动生成] 语言文件
    │   └── textures/block/        # 方块纹理（需手动添加）
    └── data/simrecordplayer/
        ├── loot_tables/blocks/    # [自动生成] 掉落表
        └── recipes/               # [自动生成] 合成配方
```

## 自定义方块纹理

默认方块纹理位于 `src/main/resources/assets/simrecordplayer/textures/block/`：
- `record_player_top.png` — 顶面纹理
- `record_player_bottom.png` — 底面纹理
- `record_player_side.png` — 侧面纹理

替换这些 PNG 文件来自定义唱片机外观。

## 技术细节

- 基于 Fabric Loom 1.8 + Minecraft 1.21.1
- 使用 jlayer 1.0.1 进行 MP3 解码
- 代码生成脚本自动从 `music/` 文件夹扫描 MP3 文件
- 音频仅在客户端播放，方块状态通过 Minecraft 内置机制同步到服务器
- `VolumeAudioDevice` 继承 `JavaSoundAudioDevice`，在 PCM 写入时应用音量缩放

## 许可

MIT License
