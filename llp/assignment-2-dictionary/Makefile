IO_LIBRARY := assignment-1-io-library
LIB_OBJ := $(IO_LIBRARY)/lib.o

.PHONY: clean build test

# Default target
.DEFAULT_GOAL := build

$(LIB_OBJ):
	make -C $(IO_LIBRARY)

dict.o: dict.asm dict.inc
	@echo "Dict up..."
	nasm -f elf64 -o dict.o dict.asm

main.o: main.asm words.inc
	@echo "Main up..."
	nasm -f elf64 -o main.o main.asm

build: main.o dict.o $(LIB_OBJ)
	@echo "Linking object files to create executable..."
	ld -o main main.o dict.o $(LIB_OBJ)

clean:
	@echo "Cleaning up..."
	rm -f main.o dict.o main
	make -C $(IO_LIBRARY) clean

test: build
	python3 test.py || { echo "Тесты слиты, иди переписывай код ❌" && exit 1; }
	@echo "All tests completed. ✅"