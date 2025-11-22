import os

# Folder containing OBJ files (non-recursive)
OBJ_FOLDER = "/Users/atom20003113/Downloads/interstellar/src/main/resources/assets/ar2/models"

for file in os.listdir(OBJ_FOLDER):
    if file.lower().endswith(".obj"):
        obj_name = os.path.splitext(file)[0]
        obj_path = os.path.join(OBJ_FOLDER, file)
        mtl_name = f"{obj_name}.mtl"
        mtl_path = os.path.join(OBJ_FOLDER, mtl_name)

        # Skip if MTL already exists

        # Write minimal MTL with unique newmtl matching the OBJ name
        with open(mtl_path, "w", encoding="utf-8") as f:
            f.write(f"""# Auto-generated MTL for {file}
newmtl None
map_Kd ar2:block/{obj_name}
""")

        # Read OBJ
        with open(obj_path, "r", encoding="utf-8") as f:
            lines = f.readlines()

        # Ensure OBJ references this MTL
        if not any(line.lower().startswith("mtllib") for line in lines):
            lines.insert(0, f"mtllib {mtl_name}\n")

        # Ensure OBJ uses this material
        if not any(line.lower().startswith("usemtl") for line in lines):
            # Insert after mtllib
            for i, line in enumerate(lines):
                if line.lower().startswith("mtllib"):
                    lines.insert(i + 1, f"usemtl {obj_name}\n")
                    break

        # Write back OBJ
        with open(obj_path, "w", encoding="utf-8") as f:
            f.writelines(lines)

        print(f"[OK] Created {mtl_name} and linked to {file} with newmtl '{obj_name}'")
