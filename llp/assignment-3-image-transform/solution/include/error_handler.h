#ifndef ERROR_HANDLER_H
#define ERROR_HANDLER_H

#include <stdarg.h>  // Standard library
#include <stdio.h>   // Standard library
#include <stdlib.h>  // Standard library

enum error_codes
{
    OK = 0,
    INVALID_ARGS,
    INVALID_INPUT_FILE,
    INVALID_TRANSFORMATION,
    INVALID_OUTPUT_FILE,
    INVALID_WRITE,
    INVALID_READ = 12,
};

void handle_error(enum error_codes code, char** argv);

#endif
