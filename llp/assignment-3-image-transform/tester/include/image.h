#pragma once

#include <inttypes.h>
#ifdef __APPLE__
#include <stdlib.h>
#else
#include <malloc.h>
#endif
#include <stddef.h>

#include "dimensions.h"

struct pixel {
  uint8_t components[3];
};

struct image {
  struct dimensions size;
  struct pixel* data;
};

struct image image_create( struct dimensions size );
void image_destroy( struct image* image );
