#ifndef BMP_H
#define BMP_H

#include <stdint.h>  // Standard library
#include <stdio.h>   // Standard library

#include "image.h"   // Project-specific header

#define BMP_HEADER_SIZE 40
#define BMP_SIGNATURE 0x4D42
#define BMP_BITS_PER_PIXEL 24
#define BMP_COMPRESSION 0
#define BMP_PLANES 1
#define BYTES_PER_PIXEL 3
#define ROW_ALIGNMENT 4

struct __attribute__((packed)) bmp_header
{
    uint16_t bfType;
    uint32_t bfileSize;
    uint32_t bfReserved;
    uint32_t bOffBits;
    uint32_t biSize;
    uint32_t biWidth;
    uint32_t biHeight;
    uint16_t biPlanes;
    uint16_t biBitCount;
    uint32_t biCompression;
    uint32_t biSizeImage;
    uint32_t biXPelsPerMeter;
    uint32_t biYPelsPerMeter;
    uint32_t biClrUsed;
    uint32_t biClrImportant;
};

enum read_status
{
    READ_OK = 0,
    READ_INVALID_SIGNATURE,
    READ_INVALID_BITS,
    READ_INVALID_HEADER,
};

enum write_status
{
    WRITE_OK = 0,
    WRITE_ERROR
};

enum read_status from_bmp(FILE* in, struct image* img);
enum write_status to_bmp(FILE* out, struct image const* img);

#endif
