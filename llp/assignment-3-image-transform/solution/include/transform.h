#ifndef TRANSFORM_H
#define TRANSFORM_H

#include <string.h>  // Standard library

#include "image.h"   // Project-specific header

struct image rotate_90_right(struct image const source);
struct image rotate_90_left(struct image const source);
struct image flip_v(struct image const source);
struct image flip_h(struct image const source);
struct image transform(struct image const source, char const* transformation);

#endif
