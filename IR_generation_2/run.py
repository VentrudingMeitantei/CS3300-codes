import os
import subprocess
import difflib
from pathlib import Path

# --- CONFIG ---
INPUT_DIR = Path("inputs")              # MiniIR files
MICRO_DIR = Path("micro_outputs")       # MicroIR outputs
MINI_OUT_DIR = Path("mini_outputs")     # MiniIR run outputs
DIFF_DIR = Path("diffs")                # Differences
P4_FILE = "P4.java"
PGI_JAR = "pgi.jar"

# --- Setup ---
for d in [MICRO_DIR, MINI_OUT_DIR, DIFF_DIR]:
    d.mkdir(exist_ok=True)

# --- Compile P4.java ---
print("🔧 Compiling P4.java...")
try:
    subprocess.run(["javac", P4_FILE], check=True, stderr=subprocess.PIPE, text=True)
    print("✅ P4.java compiled successfully.\n")
except subprocess.CalledProcessError as e:
    print(f"❌ Failed to compile P4.java:\n{e.stderr}")
    exit(1)

if not INPUT_DIR.exists():
    print(f"❌ Folder '{INPUT_DIR}' not found.")
    exit(1)

# --- Process each MiniIR file ---
for file in sorted(INPUT_DIR.glob("*.miniIR")):
    basename = file.stem
    print(f"🔹 Processing {basename}.miniIR...")

    micro_path = MICRO_DIR / f"{basename}.microIR"
    mini_out_path = MINI_OUT_DIR / f"{basename}_mini.out"
    micro_out_path = MICRO_DIR / f"{basename}_micro.out"
    diff_path = DIFF_DIR / f"{basename}.diff"

    # 1️⃣ Generate MicroIR from MiniIR using P4
    try:
        with open(file, "r") as src, open(micro_path, "w") as out:
            subprocess.run(
                ["java", "-cp", ".", "P4", str(file)],
                stdin=src,
                stdout=out,
                stderr=subprocess.PIPE,
                check=True,
                text=True
            )
    except subprocess.CalledProcessError as e:
        print(f"❌ {basename}: MicroIR generation failed:\n{e.stderr}\n")
        continue

    # 2️⃣ Run MiniIR through pgi.jar
    try:
        with open(file, "r") as mini_in, open(mini_out_path, "w") as out:
            subprocess.run(
                ["java", "-jar", PGI_JAR],
                stdin=mini_in,
                stdout=out,
                stderr=subprocess.PIPE,
                check=True,
                text=True
            )
    except subprocess.CalledProcessError as e:
        print(f"❌ {basename}: Error running MiniIR:\n{e.stderr}\n")
        continue

    # 3️⃣ Run MicroIR through pgi.jar
    try:
        with open(micro_path, "r") as micro_in, open(micro_out_path, "w") as out:
            subprocess.run(
                ["java", "-jar", PGI_JAR],
                stdin=micro_in,
                stdout=out,
                stderr=subprocess.PIPE,
                check=True,
                text=True
            )
    except subprocess.CalledProcessError as e:
        print(f"❌ {basename}: Error running MicroIR:\n{e.stderr}\n")
        continue

    # 4️⃣ Compare outputs
    mini_output = mini_out_path.read_text().splitlines()
    micro_output = micro_out_path.read_text().splitlines()

    if mini_output == micro_output:
        print(f"✅ {basename}: Outputs match!\n")
    else:
        print(f"❌ {basename}: Outputs differ!")
        diff = difflib.unified_diff(
            mini_output,
            micro_output,
            fromfile="MiniIR",
            tofile="MicroIR",
            lineterm=""
        )
        diff_path.write_text("\n".join(diff))
        print(f"🔹 Diff saved to {diff_path}\n")

print("🏁 Done!")