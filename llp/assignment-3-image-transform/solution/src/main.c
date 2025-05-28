#include "main.h"

int main(int argc, char** argv)
{
    if (argc != 4)
    {
        handle_error(INVALID_ARGS, argv);
    }

    char* input_filename = argv[1];
    char* output_filename = argv[2];
    char* image_transform = argv[3];

    FILE* input_file = fopen(input_filename, "rb");
    if (!input_file)
    {
        handle_error(INVALID_INPUT_FILE, argv);
    }

    struct image img;
    enum read_status status = from_bmp(input_file, &img);
    fclose(input_file);

    if (status != READ_OK)
    {
        handle_error(INVALID_READ, argv);
    }

    struct image output_image = transform(img, image_transform);
    if (output_image.data == NULL)
    {
        handle_error(INVALID_TRANSFORMATION, argv);
    }

    FILE* output_file = fopen(output_filename, "wb");
    if (!output_file)
    {
        free_image(&output_image);
        free_image(&img);
        handle_error(INVALID_OUTPUT_FILE, argv);
    }

    const enum write_status write_status = to_bmp(output_file, &output_image);
    fclose(output_file);

    if (write_status != WRITE_OK)
    {
        free_image(&output_image);
        free_image(&img);
        handle_error(INVALID_WRITE, argv);
    }

    free_image(&output_image);
    free_image(&img);

    return 0;
}
