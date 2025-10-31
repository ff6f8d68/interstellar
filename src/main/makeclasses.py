import os
import re

# Paths
SRC_MAIN = "src/main/java"  # adjust if needed
BLOCKS_PACKAGE = os.path.join(SRC_MAIN, "mods/hexagonal/ar2/blocks")
MODBLOCKS_FILE = os.path.join(SRC_MAIN, "mods/hexagonal/ar2/ModBlocks.java")
BLOCK_JSON_FOLDER = "/Users/atom20003113/Downloads/interstellar/src/main/resources/assets/ar2/models/block"
  # folder containing your generated block JSONs

# Ensure blocks package exists
os.makedirs(BLOCKS_PACKAGE, exist_ok=True)

# Helper to convert snake_case to PascalCase for class names
def to_class_name(name):
    parts = re.split(r'[_\s]', name)
    return ''.join(p.capitalize() for p in parts)

# Read existing ModBlocks.java
if os.path.exists(MODBLOCKS_FILE):
    with open(MODBLOCKS_FILE, "r", encoding="utf-8") as f:
        modblocks_content = f.read()
else:
    # Create a minimal template if missing
    modblocks_content = """package mods.hexagonal.ar2;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ar2.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ar2.MOD_ID);

}
"""

# Loop through all block JSONs
for file in os.listdir(BLOCK_JSON_FOLDER):
    if file.endswith(".json"):
        block_name = os.path.splitext(file)[0]  # e.g., "advbipropellantrocketmotor"
        class_name = to_class_name(block_name)  # e.g., "Advbipropellantrocketmotor"

        java_file_path = os.path.join(BLOCKS_PACKAGE, f"{class_name}.java")

        # Skip if file already exists
        if os.path.exists(java_file_path):
            print(f"[SKIP] {java_file_path} already exists")
            continue

        # Create Java class
        java_content = f"""package mods.hexagonal.ar2.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class {class_name} extends Block {{

    public {class_name}() {{
        super(BlockBehaviour.Properties
                .of()
                .strength(3.0f, 6.0f)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        );
    }}
}}
"""
        with open(java_file_path, "w", encoding="utf-8") as f:
            f.write(java_content)
        print(f"[CREATE] {java_file_path}")

        # Check if block is already registered in ModBlocks
        if block_name not in modblocks_content:
            # Add registration lines before the last closing brace
            registration_block = f"""

    public static final RegistryObject<Block> {block_name.upper()} =
            BLOCKS.register("{block_name}",
                    () -> new {class_name}()
            );

    public static final RegistryObject<Item> {block_name.upper()}_ITEM =
            ITEMS.register("{block_name}",
                    () -> new BlockItem({block_name.upper()}.get(),
                            new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS))
            );
"""
            # Insert before the last closing brace
            modblocks_content = modblocks_content.rstrip("}") + registration_block + "\n}"

# Write updated ModBlocks.java
with open(MODBLOCKS_FILE, "w", encoding="utf-8") as f:
    f.write(modblocks_content)

print("[DONE] ModBlocks.java updated")
