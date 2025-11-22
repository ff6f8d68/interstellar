import os
import re

# Root model input folder — now includes ALL models
MODEL_FOLDER = "models"   # ✅ now scans entire models/ recursively

# Output folders
BLOCK_MODELS_FOLDER = "models/block"
ITEM_MODELS_FOLDER = "models/item"
BLOCKSTATES_FOLDER = "blockstates"

# Ensure output directories exist
os.makedirs(BLOCK_MODELS_FOLDER, exist_ok=True)
os.makedirs(ITEM_MODELS_FOLDER, exist_ok=True)
os.makedirs(BLOCKSTATES_FOLDER, exist_ok=True)

def parse_mtl_texture(line):
    """
    Normalize texture path (remove namespace, extension, uppercase, wrong folder, etc.)
    Example outputs:
      block/name
      block/subfolder/name
    """
    match = re.match(r'map_Kd\s+(.+)', line.strip())
    if not match:
        return None
    raw = match.group(1).strip()

    # Remove quotes
    if raw.startswith('"') and raw.endswith('"'):
        raw = raw[1:-1]

    # Remove extension (.png, .jpg, etc.)
    raw = os.path.splitext(raw)[0].replace("\\", "/")

    # Namespaced?
    if ":" in raw:
        ns, path = raw.split(":", 1)
    else:
        ns, path = None, raw

    path = path.lstrip("/")

    # Remove "textures/" prefix
    if path.startswith("textures/"):
        path = path[len("textures/"):]

    # Fix AR2 namespace models → block
    if ns == "ar2" and path.startswith("models/"):
        path = "block/" + path[len("models/"):]

    # Fix lowercase
    path = path.lower()

    # Default all textures into block/
    if not (path.startswith("block/") or path.startswith("item/")):
        path = f"block/{path}"

    # Remove accidental doubling
    path = path.replace("block/block/", "block/")

    return path


# ✅ Process ALL MTL files in models/ recursively
for root, dirs, files in os.walk(MODEL_FOLDER):
    for file in files:
        if file.lower().endswith(".mtl"):
            mtl_path = os.path.join(root, file)
            name = os.path.splitext(file)[0].lower()  # JSON names must be lowercase

            # Try to find map_Kd texture
            texture_name = None
            with open(mtl_path, "r", encoding="utf-8") as f:
                for line in f:
                    if line.strip().startswith("map_Kd"):
                        texture_name = parse_mtl_texture(line)
                        break

            if not texture_name:
                print(f"[WARN] No map_Kd texture found in {mtl_path}")
                continue

            # ---------------------------
            # BLOCK JSON
            # ---------------------------
            block_json_path = os.path.join(BLOCK_MODELS_FOLDER, f"{name}.json")
            block_json = f"""{{
  "loader": "forge:obj",
  "model": "ar2:{root.replace(os.sep,'/')}/{name}.obj",
  "flip_v": false,
  "textures": {{
    "particle": "ar2:{texture_name}",
    "texture0": "ar2:{texture_name}"
  }}
}}"""

            with open(block_json_path, "w", encoding="utf-8") as f:
                f.write(block_json)

            # ---------------------------
            # ITEM JSON
            # ---------------------------
            item_json_path = os.path.join(ITEM_MODELS_FOLDER, f"{name}.json")
            item_json = f"""{{
  "loader": "forge:obj",
  "model": "ar2:{root.replace(os.sep,'/')}/{name}.obj",
  "flip_v": false,
  "textures": {{
    "particle": "ar2:{texture_name}",
    "texture0": "ar2:{texture_name}"
  }},
  "display": {{
    "thirdperson_righthand": {{
      "rotation": [ 0, 90, 0 ],
      "translation": [ 0, 2, 0 ],
      "scale": [ 0.5, 0.5, 0.5 ]
    }},
    "firstperson_righthand": {{
      "rotation": [ 0, 90, 0 ],
      "translation": [ 0, 2, 0 ],
      "scale": [ 0.5, 0.5, 0.5 ]
    }},
    "ground": {{
      "translation": [ 0, 0.5, 0 ],
      "scale": [ 0.5, 0.5, 0.5 ]
    }},
    "fixed": {{
      "scale": [ 0.5, 0.5, 0.5 ]
    }}
  }}
}}"""

            with open(item_json_path, "w", encoding="utf-8") as f:
                f.write(item_json)

            # ---------------------------
            # BLOCKSTATE JSON
            # ---------------------------
            blockstate_path = os.path.join(BLOCKSTATES_FOLDER, f"{name}.json")
            blockstate_json = f"""{{
  "variants": {{
    "": {{ "model": "ar2:block/{name}" }}
  }}
}}"""

            with open(blockstate_path, "w", encoding="utf-8") as f:
                f.write(blockstate_json)

            print(f"[OK] Generated: {name}   (tex=ar2:{texture_name})")
