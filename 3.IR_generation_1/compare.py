import sys

def compare_files(file1, file2):
    try:
        with open(file1, 'r') as f1, open(file2, 'r') as f2:
            content1 = f1.read()
            content2 = f2.read()
            
            if content1 == content2:
                print("✅ The files have the same content.")
            else:
                print("❌ The files are different.")
    except FileNotFoundError as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python compare.py file1.txt file2.txt")
    else:
        compare_files(sys.argv[1], sys.argv[2])
