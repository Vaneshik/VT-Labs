#include "image.h"

struct image create_image(uint64_t width, uint64_t height)
{
    struct image img = {
        .width = width,
        .height = height,
        .data = malloc(sizeof(struct pixel) * width * height)
    };
    return img;
}

void free_image(struct image* img)
{
    free(img->data);
    img->data = NULL;
}
