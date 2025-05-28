ASM = nasm
ASM_FLAGS = -f elf64
TARGET = lib.o
SRC = lib.asm

.PHONY: clean test

.DEFAULT_GOAL := $(TARGET)

$(TARGET): $(SRC) lib.inc
	@echo "Собираем... 😼"
	$(ASM) $(ASM_FLAGS) -o $@ $(SRC)
	@echo "ГОООООООООЛ! 🎉"

clean:
	@echo "Чистим... 🧹"
	rm -rf parse* print* read* string* report.xml $(TARGET) __pycache__
	@echo "ГОООООООООЛ! 🚀"

test: $(TARGET)
	@echo "Тестируем... 🧪"
	python test.py || { echo "Тесты слиты, иди переписывай код ❌" && exit 1; }
	@echo "ГОООООООООЛ! ✅"