import os
import shutil
import subprocess

# List all MiniJava programs you want to test
files = ["Factorial.java"]

for f in files:
    base = os.path.splitext(f)[0]  # e.g. "A"
    print(f"\n=== Running test for {f} ===")

    # Step 1: Copy f -> Test.java
    shutil.copy(f, "Test.java")

    # Step 2: Run IR pipeline on Test.java (produces ir_out.txt)
    subprocess.run(["make", "clean"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    subprocess.run(["make", "ir_out.txt"], check=True)

    # Step 3: Compile and run original MiniJava program f -> jv_out.txt
    subprocess.run(["javac", f], check=True)
    with open("jv_out.txt", "w") as out:
        subprocess.run(["java", base], stdout=out, check=True)

    # Step 4: Compare results
    with open("jv_out.txt") as jv, open("ir_out.txt") as ir:
        jv_out = jv.read().strip()
        ir_out = ir.read().strip()

        if jv_out == ir_out:
            print(f"✅ {f}: Outputs match")
        else:
            print(f"❌ {f}: Outputs differ")
            subprocess.run(["diff", "-u", "jv_out.txt", "ir_out.txt"])
