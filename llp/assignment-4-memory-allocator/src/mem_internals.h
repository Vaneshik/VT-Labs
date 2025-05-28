#ifndef _MEM_INTERNALS_
#define _MEM_INTERNALS_

#include <inttypes.h>
#include <stdbool.h>
#include <stddef.h>

#define REGION_MIN_SIZE (2 * 4096)

struct region
{
  void* addr;
  size_t size;
  bool extends;
};

static const struct region REGION_INVALID = {0};

typedef struct
{
  size_t bytes;
} block_capacity;

typedef struct
{
  size_t bytes;
} block_size;

struct block_header {
  struct block_header *next;
  bool is_free;
  block_capacity capacity;
  uint8_t contents[];
} __attribute__((aligned(8)));

#endif
