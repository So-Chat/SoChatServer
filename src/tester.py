import os

# Какие расширения считать "скриптами"
EXTENSIONS = {".py", ".js", ".ts", ".java", ".cpp", ".c", ".cs", ".kt", ".go", ".dart"}

total_lines = 0
total_chars = 0  # новая переменная для подсчёта символов
file_count = 0

# Папка, где находится этот скрипт
base_dir = os.path.dirname(os.path.abspath(__file__))

for root, dirs, files in os.walk(base_dir):
    for file in files:
        if os.path.splitext(file)[1] in EXTENSIONS:
            file_path = os.path.join(root, file)
            try:
                with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
                    lines = 0
                    chars = 0
                    for line in f:
                        lines += 1
                        chars += len(line)
                    total_lines += lines
                    total_chars += chars
                    file_count += 1
            except Exception:
                pass

print(f"Файлов найдено: {file_count}")
print(f"Всего строк: {total_lines}")
print(f"Всего символов: {total_chars}")