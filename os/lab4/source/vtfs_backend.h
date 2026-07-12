#pragma once
#include <linux/types.h>
#include <linux/stat.h>

typedef u64 vtfs_id;

enum vtfs_type {
  VTFS_REG = 1,
  VTFS_DIR = 2,
};

struct vtfs_attr {
  vtfs_id id;
  enum vtfs_type type;
  umode_t mode;
  u64 size;
  u32 nlink;
  u64 atime_ns, mtime_ns, ctime_ns;
};

struct vtfs_dirent {
  char name[256];
  vtfs_id id;
  enum vtfs_type type;
};

struct vtfs_backend;

struct vtfs_backend_ops {
  int (*init)(struct vtfs_backend *b, const char *token);
  void (*destroy)(struct vtfs_backend *b);

  int (*getattr)(struct vtfs_backend *b, vtfs_id id, struct vtfs_attr *out);

  int (*lookup)(struct vtfs_backend *b, vtfs_id parent, const char *name, vtfs_id *out_id);

  int (*readdir)(struct vtfs_backend *b, vtfs_id dir, u64 cursor,
                 struct vtfs_dirent *out, u32 out_cap, u32 *out_n, u64 *out_next_cursor);

  int (*create)(struct vtfs_backend *b, vtfs_id parent, const char *name,
                enum vtfs_type type, umode_t mode, vtfs_id *out_id);

  int (*unlink)(struct vtfs_backend *b, vtfs_id parent, const char *name);
  
  int (*link)(struct vtfs_backend *b, vtfs_id old_id, vtfs_id new_parent, const char *new_name);

  ssize_t (*read)(struct vtfs_backend *b, vtfs_id file, u64 off, void *buf, size_t len);
  ssize_t (*write)(struct vtfs_backend *b, vtfs_id file, u64 off, const void *buf, size_t len);
  int (*truncate)(struct vtfs_backend *b, vtfs_id file, u64 new_size);
};

struct vtfs_backend {
  const struct vtfs_backend_ops *ops;
  void *priv;
  vtfs_id root_id;
};
