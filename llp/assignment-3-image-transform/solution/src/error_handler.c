#include "error_handler.h"

void handle_error(enum error_codes code, char** argv) {
    char* input_filename  = argv[1];
    char* output_filename = argv[2];

    switch (code) {
        case INVALID_ARGS:
            fprintf(stderr, "Usage: %s <source-image> <transformed-image> <transformation>\n", argv[0]);
            break;
        case INVALID_INPUT_FILE:
            fprintf(stderr, "Failed to open file %s\n", input_filename);
            break;
        case INVALID_READ:
            fprintf(stderr, "Failed to read from BMP %s\n", input_filename);
            break;
        case INVALID_TRANSFORMATION:
            fprintf(stderr, "Failed to transform image\n");
            break;
        case INVALID_OUTPUT_FILE:
            fprintf(stderr, "Failed to open file %s\n", output_filename);
            break;
        case INVALID_WRITE:
            fprintf(stderr, "Failed to write to BMP %s\n", output_filename);
            break;
        default:
            break;
    }
    exit(code);
}
