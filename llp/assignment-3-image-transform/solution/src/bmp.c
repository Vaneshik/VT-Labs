#include "bmp.h"

uint8_t calc_padding(const uint32_t image_width)
{
    return (ROW_ALIGNMENT - (image_width * BYTES_PER_PIXEL) % ROW_ALIGNMENT) % ROW_ALIGNMENT;
}

enum read_status from_bmp(FILE* in, struct image* img)
{
    struct bmp_header header;
    if (fread(&header, sizeof(header), 1, in) != 1)
    {
        return READ_INVALID_HEADER;
    }

    // Check the signature
    if (header.bfType != BMP_SIGNATURE)
    {
        return READ_INVALID_SIGNATURE;
    }

    // Only handle 24-bit BMP
    if (header.biBitCount != 24)
    {
        return READ_INVALID_BITS;
    }

    // Check dimension validity
    if (header.biWidth <= 0 || header.biHeight <= 0)
    {
        return READ_INVALID_HEADER;
    }

    // Safe multiplication to avoid overflow
    size_t pixel_count = (size_t)header.biWidth * (size_t)header.biHeight;
    if (pixel_count > SIZE_MAX / sizeof(struct pixel))
    {
        return READ_INVALID_HEADER;
    }

    // Attempt to read file size for offset checks
    uint32_t current_pos = ftell(in);
    fseek(in, 0, SEEK_END);
    uint32_t file_size = ftell(in);
    fseek(in, current_pos, SEEK_SET);

    // Check offset validity
    if (header.bOffBits > (uint32_t)file_size)
    {
        return READ_INVALID_HEADER;
    }

    // Allocate
    *img = create_image(header.biWidth, header.biHeight);

    // Move to pixel data
    if (fseek(in, header.bOffBits, SEEK_SET) != 0)
    {
        free_image(img);
        return READ_INVALID_HEADER;
    }

    // Calculate row padding
    uint8_t padding = calc_padding(header.biWidth);

    for (uint32_t y = 0; y < (uint32_t)header.biHeight; y++)
    {
        // Check that we won't read beyond EOF
        uint32_t expected_pos_after = ftell(in) + header.biWidth * sizeof(struct pixel);
        if (expected_pos_after > file_size)
        {
            free_image(img);
            return READ_INVALID_HEADER;
        }

        // read row
        if (fread(img->data + y * img->width, sizeof(struct pixel), img->width, in) != img->width)
        {
            free_image(img);
            return READ_INVALID_HEADER;
        }

        // skip padding
        if (fseek(in, padding, SEEK_CUR) != 0)
        {
            free_image(img);
            return READ_INVALID_HEADER;
        }
    }
    return READ_OK;
}

/*  serializer   */
enum write_status to_bmp(FILE* out, struct image const* img)
{
    uint8_t padding = calc_padding(img->width);
    size_t image_data_size = (img->width * sizeof(struct pixel) + padding) * img->height;

    struct bmp_header header = {
        .bfType = BMP_SIGNATURE,
        .bfileSize = sizeof(header) + image_data_size,
        .bfReserved = 0,
        .bOffBits = sizeof(header),
        .biSize = BMP_HEADER_SIZE,
        .biWidth = img->width,
        .biHeight = img->height,
        .biPlanes = BMP_PLANES,
        .biBitCount = BMP_BITS_PER_PIXEL,
        .biCompression = BMP_COMPRESSION,
        .biSizeImage = image_data_size,
        .biXPelsPerMeter = 0,
        .biYPelsPerMeter = 0,
        .biClrUsed = 0,
        .biClrImportant = 0
    };

    // write header
    if (fwrite(&header, sizeof(header), 1, out) != 1)
    {
        return WRITE_ERROR;
    }

    // write image data
    for (size_t y = 0; y < img->height; y++)
    {
        // write row
        if (fwrite(img->data + y * img->width, sizeof(struct pixel), img->width, out) != img->width)
        {
            return WRITE_ERROR;
        }

        // write padding
        for (size_t i = 0; i < padding; i++)
        {
            const char* PADDING_WORD = "CAT";
            if (fputc(PADDING_WORD[i], out) == EOF)
            {
                return WRITE_ERROR;
            }
        }
    }
    return WRITE_OK;
}
