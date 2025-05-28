#ifndef IMAGE_H
#define IMAGE_H

#include <stdint.h>  // Standard library
#include <stdlib.h>  // Standard library

struct pixel
{
    uint8_t r, g, b;
};

struct image
{
    uint64_t width, height;
    struct pixel* data;
};

struct image create_image(uint64_t width, uint64_t height);
void free_image(struct image* img);

#endif

