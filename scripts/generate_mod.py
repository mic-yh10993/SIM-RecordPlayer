#!/usr/bin/env python3
"""
Generate SIM-RecordPlayer mod sources from MP3 files in music/ folder.
Run this script before building the mod.
"""

import os
import re
import json
import shutil
from pathlib import Path

MUSIC_DIR = Path("music")
SRC_DIR = Path("src/main/java/com/sim/recordplayer")
RES_DIR = Path("src/main/resources")

# Directories to clean and regenerate
GEN_DIRS = [
    SRC_DIR / "block" / "generated",
    SRC_DIR / "item" / "generated",
    SRC_DIR / "client" / "generated",
    RES_DIR / "assets" / "simrecordplayer" / "blockstates",
    RES_DIR / "assets" / "simrecordplayer" / "models" / "block",
    RES_DIR / "assets" / "simrecordplayer" / "models" / "item",
    RES_DIR / "assets" / "simrecordplayer" / "lang",
    RES_DIR / "data" / "simrecordplayer" / "loot_tables" / "blocks",
    RES_DIR / "data" / "simrecordplayer" / "recipes",
]


def derive_song_key(filename):
    """Derive song key from MP3 filename: 'Moon Halo.mp3' -> 'moon_halo'
    Only uses [a-z0-9_] to comply with Minecraft resource location rules.
    For Chinese-only names, uses a short hash."""
    import hashlib
    name = Path(filename).stem
    key = name.lower()
    # Only keep ASCII letters and digits
    key = re.sub(r'[^a-z0-9]+', '_', key)
    key = key.strip('_')
    # If result is empty (e.g. all-Chinese filename), use hash
    if not key:
        key = 'song_' + hashlib.md5(filename.encode()).hexdigest()[:8]
    if key[0].isdigit():
        key = 's_' + key
    return key


def derive_display_name(filename):
    """Derive display name from MP3 filename: 'Moon Halo.mp3' -> 'Moon Halo'"""
    return Path(filename).stem


def scan_music():
    """Scan music/ folder for MP3 files, return list of (song_key, display_name, filename)"""
    songs = []
    if not MUSIC_DIR.exists():
        print("Warning: music/ folder not found, creating empty folder")
        MUSIC_DIR.mkdir(exist_ok=True)
        return songs

    for f in sorted(MUSIC_DIR.iterdir()):
        if f.suffix.lower() == '.mp3' and f.is_file():
            song_key = derive_song_key(f.name)
            display_name = derive_display_name(f.name)
            songs.append((song_key, display_name, f.name))
            print(f"  Found: {f.name} -> key={song_key}, name={display_name}")

    return songs


def clean_generated():
    """Remove previously generated files"""
    for d in GEN_DIRS:
        if d.exists():
            shutil.rmtree(d)
            print(f"  Cleaned: {d}")


def gen_mod_blocks(songs):
    """Generate ModBlocks.java"""
    lines = [
        "package com.sim.recordplayer.block;",
        "",
        "import com.sim.recordplayer.SIMRecordPlayer;",
        "import net.minecraft.block.Block;",
        "import net.minecraft.block.entity.BlockEntityType;",
        "import net.minecraft.registry.Registries;",
        "import net.minecraft.registry.Registry;",
        "import net.minecraft.util.Identifier;",
        "",
        "public class ModBlocks {",
    ]

    block_fields = []
    for song_key, display_name, _ in songs:
        field_name = f"RECORD_PLAYER_{song_key.upper()}"
        block_fields.append(field_name)
        lines.append(
            f'    public static final Block {field_name} = new RecordPlayerBlock("{song_key}", "{display_name}",\n'
            f'            Block.Settings.create().strength(2.0f).nonOpaque());'
        )

    lines.append("")
    block_entity_args = ", ".join(block_fields)
    lines.append(
        "    public static final BlockEntityType<RecordPlayerBlockEntity> RECORD_PLAYER_BLOCK_ENTITY =\n"
        "            BlockEntityType.Builder.create(\n"
        "                    (pos, state) -> new RecordPlayerBlockEntity(\"\", pos, state),\n"
        f"                    {block_entity_args}\n"
        "            ).build(null);"
    )

    lines.append("")
    lines.append("    public static void registerBlocks() {")

    for song_key, _, _ in songs:
        field_name = f"RECORD_PLAYER_{song_key.upper()}"
        lines.append(
            f"        Registry.register(Registries.BLOCK,\n"
            f'                Identifier.of(SIMRecordPlayer.MOD_ID, "record_player_{song_key}"), {field_name});'
        )

    lines.append("")
    lines.append(
        "        Registry.register(Registries.BLOCK_ENTITY_TYPE,\n"
        '                Identifier.of(SIMRecordPlayer.MOD_ID, "record_player"), RECORD_PLAYER_BLOCK_ENTITY);'
    )
    lines.append("")
    lines.append(f'        SIMRecordPlayer.LOGGER.info("Registered {{}} record player blocks", {len(songs)});')
    lines.append("    }")
    lines.append("}")
    lines.append("")

    path = SRC_DIR / "block" / "ModBlocks.java"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("\n".join(lines), encoding="utf-8")
    print(f"  Generated: {path}")


def gen_mod_items(songs):
    """Generate ModItems.java"""
    lines = [
        "package com.sim.recordplayer.item;",
        "",
        "import com.sim.recordplayer.SIMRecordPlayer;",
        "import com.sim.recordplayer.block.ModBlocks;",
        "import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;",
        "import net.minecraft.item.Item;",
        "import net.minecraft.item.ItemGroup;",
        "import net.minecraft.item.ItemStack;",
        "import net.minecraft.registry.Registries;",
        "import net.minecraft.registry.Registry;",
        "import net.minecraft.registry.RegistryKey;",
        "import net.minecraft.registry.RegistryKeys;",
        "import net.minecraft.text.Text;",
        "import net.minecraft.util.Identifier;",
        "",
        "public class ModItems {",
    ]

    item_fields = []
    for song_key, _, _ in songs:
        field_name = f"RECORD_PLAYER_{song_key.upper()}"
        item_fields.append(field_name)
        lines.append(
            f"    public static final Item {field_name} = new RecordPlayerBlockItem(\n"
            f"            ModBlocks.{field_name}, new Item.Settings());"
        )

    lines.append("")
    lines.append(
        "    public static final RegistryKey<ItemGroup> SIM_RECORD_PLAYER_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP,\n"
        '            Identifier.of(SIMRecordPlayer.MOD_ID, "sim_record_player_group"));'
    )

    lines.append("")
    lines.append("    public static void registerItems() {")

    for song_key, _, _ in songs:
        field_name = f"RECORD_PLAYER_{song_key.upper()}"
        lines.append(
            f"        Registry.register(Registries.ITEM,\n"
            f'                Identifier.of(SIMRecordPlayer.MOD_ID, "record_player_{song_key}"), {field_name});'
        )

    lines.append("")
    lines.append("        Registry.register(Registries.ITEM_GROUP, SIM_RECORD_PLAYER_GROUP,")
    lines.append("                FabricItemGroup.builder()")
    lines.append(f"                        .icon(() -> new ItemStack({item_fields[0]}))")
    lines.append('                        .displayName(Text.translatable("itemGroup.simrecordplayer.sim_record_player_group"))')
    lines.append("                        .entries((enabledFeatures, entries) -> {")

    for field_name in item_fields:
        lines.append(f"                            entries.add({field_name});")

    lines.append("                        })")
    lines.append("                        .build());")
    lines.append("")
    lines.append('        SIMRecordPlayer.LOGGER.info("Registered items");')
    lines.append("    }")
    lines.append("}")
    lines.append("")

    path = SRC_DIR / "item" / "ModItems.java"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("\n".join(lines), encoding="utf-8")
    print(f"  Generated: {path}")


def gen_client_manager(songs):
    """Generate RecordPlayerClientManager.java"""
    song_map_lines = []
    for song_key, _, filename in songs:
        song_map_lines.append(f'            case "{song_key}" -> "{filename}";')

    song_map = "\n".join(song_map_lines)

    code = f'''package com.sim.recordplayer.client;

import com.sim.recordplayer.SIMRecordPlayer;
import com.sim.recordplayer.audio.AudioPlayer;
import com.sim.recordplayer.block.RecordPlayerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class RecordPlayerClientManager {{
    private static final Map<BlockPos, String> playingBlocks = new ConcurrentHashMap<>();
    private static AudioPlayer currentAudio;
    private static BlockPos currentPos;
    private static final double MAX_DISTANCE = 32.0;
    private static final double FADE_START = 22.0;

    public static void tick(MinecraftClient client) {{
        if (client.world == null || client.player == null) {{
            stopAll();
            return;
        }}

        playingBlocks.clear();

        Vec3d playerPos = client.player.getPos();
        int px = (int) playerPos.x;
        int py = (int) playerPos.y;
        int pz = (int) playerPos.z;
        int range = (int) Math.ceil(MAX_DISTANCE) + 1;

        for (int x = px - range; x <= px + range; x++) {{
            for (int y = py - range; y <= py + range; y++) {{
                for (int z = pz - range; z <= pz + range; z++) {{
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = client.world.getBlockState(pos);
                    if (state.getBlock() instanceof RecordPlayerBlock block) {{
                        boolean isPlaying = state.get(RecordPlayerBlock.PLAYING);
                        if (isPlaying) {{
                            playingBlocks.put(pos.toImmutable(), block.getSongName());
                        }}
                    }}
                }}
            }}
        }}

        BlockPos foundPos = null;
        String foundSong = null;
        for (Map.Entry<BlockPos, String> entry : playingBlocks.entrySet()) {{
            foundPos = entry.getKey();
            foundSong = entry.getValue();
            break;
        }}

        if (foundPos == null || foundSong == null) {{
            if (currentAudio != null) {{
                SIMRecordPlayer.LOGGER.info("No playing record player found, stopping audio");
            }}
            stopAll();
            return;
        }}

        if (currentPos != null && !currentPos.equals(foundPos)) {{
            SIMRecordPlayer.LOGGER.info("Record player changed from {{}} to {{}}", currentPos, foundPos);
            stopAll();
        }}

        double distance = client.player.getPos().distanceTo(
                new Vec3d(foundPos.getX() + 0.5, foundPos.getY() + 0.5, foundPos.getZ() + 0.5));

        if (distance > MAX_DISTANCE) {{
            stopAll();
            return;
        }}

        if (currentAudio == null || !currentAudio.isPlaying()) {{
            String mp3File = getMp3ForSong(foundSong);
            if (mp3File == null) {{
                SIMRecordPlayer.LOGGER.warn("Unknown song: {{}}", foundSong);
                return;
            }}
            SIMRecordPlayer.LOGGER.info("Starting audio for song: {{}} at distance: {{}}", foundSong, distance);
            currentAudio = new AudioPlayer(mp3File);
            currentAudio.play();
            currentPos = foundPos;
        }}

        float volume;
        if (distance <= FADE_START) {{
            volume = 1.0f;
        }} else {{
            volume = (float) (1.0 - (distance - FADE_START) / (MAX_DISTANCE - FADE_START));
            volume = Math.max(0f, Math.min(1f, volume));
        }}
        currentAudio.setVolume(volume);
    }}

    private static String getMp3ForSong(String songName) {{
        return switch (songName) {{
{song_map}
            default -> null;
        }};
    }}

    public static void stopAll() {{
        if (currentAudio != null) {{
            currentAudio.stop();
            currentAudio = null;
        }}
        currentPos = null;
    }}
}}
'''

    path = SRC_DIR / "client" / "RecordPlayerClientManager.java"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(code, encoding="utf-8")
    print(f"  Generated: {path}")


def gen_blockstates(songs):
    """Generate blockstate JSON files"""
    for song_key, _, _ in songs:
        data = {
            "variants": {
                "playing=false": {"model": f"simrecordplayer:block/record_player_{song_key}"},
                "playing=true": {"model": f"simrecordplayer:block/record_player_{song_key}"}
            }
        }
        path = RES_DIR / "assets" / "simrecordplayer" / "blockstates" / f"record_player_{song_key}.json"
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(data, indent=4, ensure_ascii=False), encoding="utf-8")
    print(f"  Generated: {len(songs)} blockstate files")


def gen_block_models(songs):
    """Generate block model JSON files"""
    for song_key, _, _ in songs:
        data = {
            "parent": "minecraft:block/cube_bottom_top",
            "textures": {
                "top": "simrecordplayer:block/record_player_top",
                "bottom": "simrecordplayer:block/record_player_bottom",
                "side": "simrecordplayer:block/record_player_side"
            }
        }
        path = RES_DIR / "assets" / "simrecordplayer" / "models" / "block" / f"record_player_{song_key}.json"
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(data, indent=4, ensure_ascii=False), encoding="utf-8")
    print(f"  Generated: {len(songs)} block model files")


def gen_item_models(songs):
    """Generate item model JSON files"""
    for song_key, _, _ in songs:
        data = {
            "parent": "simrecordplayer:block/record_player_" + song_key
        }
        path = RES_DIR / "assets" / "simrecordplayer" / "models" / "item" / f"record_player_{song_key}.json"
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(data, indent=4, ensure_ascii=False), encoding="utf-8")
    print(f"  Generated: {len(songs)} item model files")


def gen_lang(songs):
    """Generate language files"""
    zh_cn = {"itemGroup.simrecordplayer.sim_record_player_group": "S_I_M唱片机"}
    en_us = {"itemGroup.simrecordplayer.sim_record_player_group": "S_I_M Record Players"}

    for song_key, display_name, _ in songs:
        block_key = f"block.simrecordplayer.record_player_{song_key}"
        item_key = f"item.simrecordplayer.record_player_{song_key}"
        zh_cn[block_key] = f"唱片机 - {display_name}"
        zh_cn[item_key] = f"唱片机 - {display_name}"
        en_us[block_key] = f"Record Player - {display_name}"
        en_us[item_key] = f"Record Player - {display_name}"

    for lang, data in [("zh_cn", zh_cn), ("en_us", en_us)]:
        path = RES_DIR / "assets" / "simrecordplayer" / "lang" / f"{lang}.json"
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(data, indent=4, ensure_ascii=False), encoding="utf-8")
    print(f"  Generated: 2 lang files")


def gen_loot_tables(songs):
    """Generate loot table JSON files"""
    for song_key, _, _ in songs:
        data = {
            "type": "minecraft:block",
            "pools": [
                {
                    "rolls": 1,
                    "bonus_rolls": 0,
                    "entries": [
                        {
                            "type": "minecraft:item",
                            "name": f"simrecordplayer:record_player_{song_key}"
                        }
                    ],
                    "conditions": [
                        {
                            "condition": "minecraft:survives_explosion"
                        }
                    ]
                }
            ]
        }
        path = RES_DIR / "data" / "simrecordplayer" / "loot_tables" / "blocks" / f"record_player_{song_key}.json"
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(data, indent=4, ensure_ascii=False), encoding="utf-8")
    print(f"  Generated: {len(songs)} loot table files")


def gen_recipes(songs):
    """Generate recipe JSON files"""
    for song_key, _, _ in songs:
        data = {
            "type": "minecraft:crafting_shaped",
            "pattern": [
                "ABA",
                "ACA",
                "DDD"
            ],
            "key": {
                "A": {"item": "minecraft:iron_ingot"},
                "B": {"item": "minecraft:redstone"},
                "C": {"item": "minecraft:note_block"},
                "D": {"item": "minecraft:oak_planks"}
            },
            "result": {
                "id": f"simrecordplayer:record_player_{song_key}",
                "count": 1
            }
        }
        path = RES_DIR / "data" / "simrecordplayer" / "recipes" / f"record_player_{song_key}.json"
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(data, indent=4, ensure_ascii=False), encoding="utf-8")
    print(f"  Generated: {len(songs)} recipe files")


def main():
    print("=== SIM-RecordPlayer Code Generator ===")
    print()

    print("Scanning music/ folder...")
    songs = scan_music()

    if not songs:
        print("No MP3 files found in music/ folder!")
        print("Please add MP3 files to the music/ folder and run this script again.")
        print()
        print("Example:")
        print("  music/")
        print("    Moon Halo.mp3")
        print("    心忆.mp3")
        return

    print(f"\nFound {len(songs)} song(s)")
    print()

    print("Cleaning generated files...")
    clean_generated()
    print()

    print("Generating Java sources...")
    gen_mod_blocks(songs)
    gen_mod_items(songs)
    gen_client_manager(songs)
    print()

    print("Generating resource files...")
    gen_blockstates(songs)
    gen_block_models(songs)
    gen_item_models(songs)
    gen_lang(songs)
    gen_loot_tables(songs)
    gen_recipes(songs)
    print()

    print("=== Done! ===")
    print(f"Generated mod with {len(songs)} record player(s):")
    for song_key, display_name, filename in songs:
        print(f"  - {display_name} ({filename})")


if __name__ == "__main__":
    main()
