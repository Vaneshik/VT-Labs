#include "transform.h"

/*
 * Creates a clockwise rotated copy of the image.
 * Mapping formula: [x, y] -> [y, w - x - 1]
 */
struct image rotate_90_right(struct image const source)
{
    struct image new_image = create_image(source.height, source.width);
    for (size_t y = 0; y < source.height; y++)
    {
        for (size_t x = 0; x < source.width; x++)
        {
            new_image.data[(source.width - x - 1) * new_image.width + y] = source.data[y * source.width + x];
        }
    }
    return new_image;
}

/*
 * Creates a counterclockwise rotated copy of the image.
 * Mapping formula: [x, y] -> [h - y - 1, x]
 */
struct image rotate_90_left(struct image const source)
{
    struct image new_image = create_image(source.height, source.width);
    for (size_t y = 0; y < source.height; y++)
    {
        for (size_t x = 0; x < source.width; x++)
        {
            new_image.data[x * new_image.width + source.height - y - 1] = source.data[y * source.width + x];
        }
    }
    return new_image;
}

/*
 * Creates a vertically flipped copy of the image.
 * Mapping formula: [x, y] -> [x, h - y - 1]
 */
struct image flip_v(struct image const source)
{
    struct image new_image = create_image(source.width, source.height);
    for (size_t y = 0; y < source.height; y++)
    {
        for (size_t x = 0; x < source.width; x++)
        {
            new_image.data[(source.height - y - 1) * source.width + x] =
                source.data[y * source.width + x];
        }
    }
    return new_image;
}

/*
 * Creates a horizontally flipped copy of the image.
 * Mapping formula: [x, y] -> [w - x - 1, y]
 */
struct image flip_h(struct image const source)
{
    struct image new_image = create_image(source.width, source.height);
    for (size_t y = 0; y < source.height; y++)
    {
        for (size_t x = 0; x < source.width; x++)
        {
            new_image.data[y * source.width + (source.width - x - 1)] =
                source.data[y * source.width + x];
        }
    }
    return new_image;
}

struct image transform(struct image const source, const char* transformation)
{
    struct image output_image = {0};

    if (strcmp(transformation, "cw90") == 0)
    {
        output_image = rotate_90_right(source);
    }
    else if (strcmp(transformation, "ccw90") == 0)
    {
        output_image = rotate_90_left(source);
    }
    else if (strcmp(transformation, "fliph") == 0)
    {
        output_image = flip_h(source);
    }
    else if (strcmp(transformation, "flipv") == 0)
    {
        output_image = flip_v(source);
    }
    else if (strcmp(transformation, "none") == 0)
    {
        output_image = create_image(source.width, source.height);
        memcpy(output_image.data, source.data, source.width * source.height * sizeof(struct pixel));
    }
    else
    {
        output_image.data = NULL;
    }

    return output_image;
}
