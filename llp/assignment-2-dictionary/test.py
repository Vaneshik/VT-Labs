import subprocess
import sys

hello_string = "Привет! Введи строку для поиска: "
not_found_error_message = "Ничего не смог найти =("
buffer_overflow_error_message = "!!! [PWNED] Buffer OverFlow!!!"

test_cases = [
    ("pivopivo\n", not_found_error_message),
    ("a"*1337 + "\n", buffer_overflow_error_message),
    ("\t\n", "tab tab tab tab tab tab tab tab tab tab tab tab tab tab tab tab tab tab tab tab tab tab tab tab tab tab"),
    (" \n", "lol whitespace >_<"),
    ("dora\n", "dura"),
    ("Nazar Klyuhanov\n", "https://www.youtube.com/watch?v=FKg8HpO0HYc")
]

def run_test(input_str, expected_output):
    process = subprocess.Popen(
        ["./main"], stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE
    )
    stdout, stderr = process.communicate(input=input_str.encode())
    
    output = stdout.decode().replace(hello_string, "").strip()   
    
    passed = (output == expected_output or stderr.decode() == expected_output)
    
    print(f"Test {'PASSED' if passed else 'FAILED'}")
    if not passed:
        print(f"Input:\n{input_str.encode()}")
        print(f"Expected Output:\n{expected_output.encode()}")
        print(f"Actual Output:\n{output.encode()}")
    #     if error_output:
    #         print(f"Error Output:\n{error_output}")
    return passed


def main():
    print("Running tests...")
    all_passed = True
    for input_str, expected_output in test_cases:
        if not run_test(input_str, expected_output):
            all_passed = False

    if all_passed:
        print("All tests passed successfully! ✅")
    else:
        print("GGWP Some tests failed ❌")
        sys.exit(1)

if __name__ == "__main__":
    main()