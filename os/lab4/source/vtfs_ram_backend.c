#include <linux/errno.h>
#include <linux/list.h>
#include <linux/mutex.h>
#include <linux/string.h>
#include <linux/slab.h>

#include "vtfs_backend.h"

struct vtfs_ram_inode {
  struct list_head list;
  vtfs_id id;
  u32 nlink;
  umode_t mode;
  u64 size;
  u64 cap;
  char *data;
};

struct vtfs_ram_node {
  struct list_head siblings;
  struct list_head children;
  vtfs_id id;
  enum vtfs_type type;
  umode_t mode;
  struct vtfs_ram_inode *inode;
  char name[256];
};

struct vtfs_ram_state {
  struct mutex lock;
  struct vtfs_ram_node *root;
  vtfs_id next_id;
  struct list_head inodes;
};

static struct vtfs_ram_inode *find_inode_by_id_locked(struct vtfs_ram_state *st, vtfs_id id) {
  struct vtfs_ram_inode *ino;
  list_for_each_entry(ino, &st->inodes, list) {
    if (ino->id == id)
      return ino;
  }
  return NULL;
}

static struct vtfs_ram_node *find_by_id_locked(struct vtfs_ram_node *dir, vtfs_id id) {
  struct vtfs_ram_node *n;

  if (!dir) return NULL;
  if (dir->id == id) return dir;

  if (dir->type != VTFS_DIR) return NULL;

  list_for_each_entry(n, &dir->children, siblings) {
    struct vtfs_ram_node *hit = find_by_id_locked(n, id);
    if (hit) return hit;
  }
  return NULL;
}

static struct vtfs_ram_node *find_child_by_name_locked(struct vtfs_ram_node *dir, const char *name) {
  struct vtfs_ram_node *n;

  if (!dir || dir->type != VTFS_DIR) return NULL;

  list_for_each_entry(n, &dir->children, siblings) {
    if (!strcmp(n->name, name))
      return n;
  }
  return NULL;
}

static int add_child_locked(struct vtfs_ram_state *st,
                            struct vtfs_ram_node *parent,
                            const char *name,
                            enum vtfs_type type,
                            umode_t mode,
                            vtfs_id fixed_id_or_0,
                            vtfs_id *out_id) {
  struct vtfs_ram_node *n;

  if (!parent || parent->type != VTFS_DIR) return -ENOTDIR;
  if (!name || name[0] == '\0') return -EINVAL;
  if (strlen(name) >= sizeof(n->name)) return -ENAMETOOLONG;

  if (find_child_by_name_locked(parent, name))
    return -EEXIST;

  n = kzalloc(sizeof(*n), GFP_KERNEL);
  if (!n) return -ENOMEM;

  INIT_LIST_HEAD(&n->siblings);
  INIT_LIST_HEAD(&n->children);

  n->type = type;
  n->mode = (mode & 0777) ? (mode & 0777) : 0777;
  n->inode = NULL;

  n->id = fixed_id_or_0 ? fixed_id_or_0 : st->next_id++;

  strscpy(n->name, name, sizeof(n->name));

  if (type == VTFS_REG) {
    struct vtfs_ram_inode *ino = kzalloc(sizeof(*ino), GFP_KERNEL);
    if (!ino) {
      kfree(n);
      return -ENOMEM;
    }
    INIT_LIST_HEAD(&ino->list);
    ino->id = n->id;
    ino->nlink = 1;
    ino->mode = n->mode;
    ino->size = 0;
    ino->cap = 0;
    ino->data = NULL;

    list_add_tail(&ino->list, &st->inodes);
    n->inode = ino;
  }

  list_add_tail(&n->siblings, &parent->children);

  if (out_id) *out_id = n->id;
  return 0;
}

static int vtfs_ram_init(struct vtfs_backend *b, const char *token) {
  struct vtfs_ram_state *st;
  struct vtfs_ram_node *root;

  st = kzalloc(sizeof(*st), GFP_KERNEL);
  if (!st) return -ENOMEM;

  mutex_init(&st->lock);
  INIT_LIST_HEAD(&st->inodes);

  st->next_id = 1001;

  root = kzalloc(sizeof(*root), GFP_KERNEL);
  if (!root) {
    kfree(st);
    return -ENOMEM;
  }

  INIT_LIST_HEAD(&root->siblings);
  INIT_LIST_HEAD(&root->children);
  root->id = 1000;
  root->type = VTFS_DIR;
  root->mode = 0777;
  root->inode = NULL;
  strscpy(root->name, "/", sizeof(root->name));

  st->root = root;

  b->priv = st;
  b->root_id = 1000;

  (void)token;
  return 0;
}

static void free_subtree_nodes_only_locked(struct vtfs_ram_node *n) {
  struct vtfs_ram_node *c, *tmp;

  if (!n) return;

  if (n->type == VTFS_DIR) {
    list_for_each_entry_safe(c, tmp, &n->children, siblings) {
      list_del(&c->siblings);
      free_subtree_nodes_only_locked(c);
    }
  }
  kfree(n);
}

static void vtfs_ram_destroy(struct vtfs_backend *b) {
  struct vtfs_ram_state *st = (struct vtfs_ram_state *)b->priv;
  struct vtfs_ram_inode *ino, *itmp;

  if (!st) return;

  mutex_lock(&st->lock);
  free_subtree_nodes_only_locked(st->root);
  st->root = NULL;

  list_for_each_entry_safe(ino, itmp, &st->inodes, list) {
    list_del(&ino->list);
    kfree(ino->data);
    kfree(ino);
  }
  mutex_unlock(&st->lock);

  kfree(st);
  b->priv = NULL;
}

static int vtfs_ram_getattr(struct vtfs_backend *b, vtfs_id id, struct vtfs_attr *out) {
  struct vtfs_ram_state *st = (struct vtfs_ram_state *)b->priv;
  struct vtfs_ram_node *n;
  struct vtfs_ram_inode *ino;

  if (!st || !out) return -EINVAL;

  mutex_lock(&st->lock);

  n = find_by_id_locked(st->root, id);
  if (n && n->type == VTFS_DIR) {
    out->id = n->id;
    out->type = VTFS_DIR;
    out->mode = n->mode;
    out->size = 0;
    out->nlink = 2;
    out->atime_ns = out->mtime_ns = out->ctime_ns = 0;
    mutex_unlock(&st->lock);
    return 0;
  }

  ino = find_inode_by_id_locked(st, id);
  if (!ino) {
    mutex_unlock(&st->lock);
    return -ENOENT;
  }

  out->id = ino->id;
  out->type = VTFS_REG;
  out->mode = ino->mode;
  out->size = ino->size;
  out->nlink = ino->nlink;
  out->atime_ns = out->mtime_ns = out->ctime_ns = 0;
  mutex_unlock(&st->lock);
  return 0;
}

static int vtfs_ram_lookup(struct vtfs_backend *b, vtfs_id parent, const char *name, vtfs_id *out_id) {
  struct vtfs_ram_state *st = (struct vtfs_ram_state *)b->priv;
  struct vtfs_ram_node *p, *child;

  if (!st || !name || !out_id) return -EINVAL;

  mutex_lock(&st->lock);
  p = find_by_id_locked(st->root, parent);
  if (!p || p->type != VTFS_DIR) {
    mutex_unlock(&st->lock);
    return -ENOENT;
  }

  child = find_child_by_name_locked(p, name);
  if (!child) {
    mutex_unlock(&st->lock);
    return -ENOENT;
  }

  *out_id = child->id;
  mutex_unlock(&st->lock);
  return 0;
}

static int vtfs_ram_readdir(struct vtfs_backend *b, vtfs_id dir, u64 cursor,
                            struct vtfs_dirent *out, u32 out_cap,
                            u32 *out_n, u64 *out_next_cursor) {
  struct vtfs_ram_state *st = (struct vtfs_ram_state *)b->priv;
  struct vtfs_ram_node *d, *n;
  u64 idx = 0;
  u32 n_out = 0;

  if (!st || !out || !out_n || !out_next_cursor) return -EINVAL;
  *out_n = 0;
  *out_next_cursor = cursor;

  mutex_lock(&st->lock);
  d = find_by_id_locked(st->root, dir);
  if (!d || d->type != VTFS_DIR) {
    mutex_unlock(&st->lock);
    return 0;
  }

  list_for_each_entry(n, &d->children, siblings) {
    if (idx < cursor) {
      idx++;
      continue;
    }
    if (n_out >= out_cap)
      break;

    strscpy(out[n_out].name, n->name, sizeof(out[n_out].name));
    out[n_out].id = n->id;
    out[n_out].type = n->type;
    n_out++;
    idx++;
  }

  mutex_unlock(&st->lock);

  *out_n = n_out;
  *out_next_cursor = cursor + n_out;
  return 0;
}

static int vtfs_ram_create(struct vtfs_backend *b, vtfs_id parent, const char *name,
                           enum vtfs_type type, umode_t mode, vtfs_id *out_id) {
  struct vtfs_ram_state *st = (struct vtfs_ram_state *)b->priv;
  struct vtfs_ram_node *p;
  int err;

  if (!st || !name || !out_id) return -EINVAL;

  if (type != VTFS_REG && type != VTFS_DIR)
    return -EACCES;

  mutex_lock(&st->lock);
  p = find_by_id_locked(st->root, parent);
  if (!p || p->type != VTFS_DIR) {
    mutex_unlock(&st->lock);
    return -ENOTDIR;
  }

  err = add_child_locked(st, p, name, type, mode, 0, out_id);
  mutex_unlock(&st->lock);
  return err;
}

static int vtfs_ram_link(struct vtfs_backend *b, vtfs_id old_id, vtfs_id new_parent, const char *new_name) {
  struct vtfs_ram_state *st = (struct vtfs_ram_state *)b->priv;
  struct vtfs_ram_node *p;
  struct vtfs_ram_node *n;
  struct vtfs_ram_inode *ino;

  if (!st || !new_name || new_name[0] == '\0')
    return -EINVAL;

  mutex_lock(&st->lock);

  p = find_by_id_locked(st->root, new_parent);
  if (!p || p->type != VTFS_DIR) {
    mutex_unlock(&st->lock);
    return -ENOTDIR;
  }

  if (find_child_by_name_locked(p, new_name)) {
    mutex_unlock(&st->lock);
    return -EEXIST;
  }

  ino = find_inode_by_id_locked(st, old_id);
  if (!ino) {
    mutex_unlock(&st->lock);
    return -ENOENT;
  }

  n = kzalloc(sizeof(*n), GFP_KERNEL);
  if (!n) {
    mutex_unlock(&st->lock);
    return -ENOMEM;
  }

  INIT_LIST_HEAD(&n->siblings);
  INIT_LIST_HEAD(&n->children);

  n->id = old_id;
  n->type = VTFS_REG;
  n->mode = ino->mode;
  n->inode = ino;
  strscpy(n->name, new_name, sizeof(n->name));

  list_add_tail(&n->siblings, &p->children);

  ino->nlink++;

  mutex_unlock(&st->lock);
  return 0;
}

static int vtfs_ram_unlink(struct vtfs_backend *b, vtfs_id parent, const char *name) {
  struct vtfs_ram_state *st = (struct vtfs_ram_state *)b->priv;
  struct vtfs_ram_node *p, *child;

  if (!st || !name) return -EINVAL;

  mutex_lock(&st->lock);
  p = find_by_id_locked(st->root, parent);
  if (!p || p->type != VTFS_DIR) {
    mutex_unlock(&st->lock);
    return -ENOTDIR;
  }

  child = find_child_by_name_locked(p, name);
  if (!child) {
    mutex_unlock(&st->lock);
    return -ENOENT;
  }

  if (child->type == VTFS_DIR) {
    if (!list_empty(&child->children)) {
      mutex_unlock(&st->lock);
      return -ENOTEMPTY;
    }
    list_del(&child->siblings);
    kfree(child);
    mutex_unlock(&st->lock);
    return 0;
  }

  if (child->inode && child->inode->nlink > 0)
    child->inode->nlink--;

  if (child->inode && child->inode->nlink == 0) {
    list_del(&child->inode->list);
    kfree(child->inode->data);
    kfree(child->inode);
  }

  list_del(&child->siblings);
  kfree(child);
  mutex_unlock(&st->lock);
  return 0;
}

static ssize_t vtfs_ram_read(struct vtfs_backend *b, vtfs_id file, u64 off, void *buf, size_t len) {
  struct vtfs_ram_state *st = (struct vtfs_ram_state *)b->priv;
  struct vtfs_ram_inode *ino;
  size_t nread;

  if (!st || !buf)
    return -EINVAL;

  mutex_lock(&st->lock);
  ino = find_inode_by_id_locked(st, file);
  if (!ino) {
    mutex_unlock(&st->lock);
    return -ENOENT;
  }

  if (off >= ino->size) {
    mutex_unlock(&st->lock);
    return 0;
  }

  nread = min_t(u64, (u64)len, ino->size - off);
  if (nread > 0 && ino->data)
    memcpy(buf, ino->data + off, nread);

  mutex_unlock(&st->lock);
  return (ssize_t)nread;
}

static ssize_t vtfs_ram_write(struct vtfs_backend *b, vtfs_id file, u64 off, const void *buf, size_t len) {
  struct vtfs_ram_state *st = (struct vtfs_ram_state *)b->priv;
  struct vtfs_ram_inode *ino;
  u64 need;
  char *newbuf;

  if (!st || (!buf && len))
    return -EINVAL;

  mutex_lock(&st->lock);
  ino = find_inode_by_id_locked(st, file);
  if (!ino) {
    mutex_unlock(&st->lock);
    return -ENOENT;
  }

  need = off + (u64)len;
  if (need > ino->cap) {
    u64 newcap = max_t(u64, need, ino->cap ? (ino->cap * 2) : 4096);
    newbuf = krealloc(ino->data, newcap, GFP_KERNEL);
    if (!newbuf) {
      mutex_unlock(&st->lock);
      return -ENOMEM;
    }
    if (newcap > ino->cap)
      memset(newbuf + ino->cap, 0, newcap - ino->cap);
    ino->data = newbuf;
    ino->cap = newcap;
  }

  if (off > ino->size && ino->data)
    memset(ino->data + ino->size, 0, off - ino->size);

  if (len && ino->data)
    memcpy(ino->data + off, buf, len);

  ino->size = max_t(u64, ino->size, need);

  mutex_unlock(&st->lock);
  return (ssize_t)len;
}

static int vtfs_ram_truncate(struct vtfs_backend *b, vtfs_id file, u64 new_size) {
  struct vtfs_ram_state *st = (struct vtfs_ram_state *)b->priv;
  struct vtfs_ram_inode *ino;
  char *newbuf;

  if (!st)
    return -EINVAL;

  mutex_lock(&st->lock);
  ino = find_inode_by_id_locked(st, file);
  if (!ino) {
    mutex_unlock(&st->lock);
    return -ENOENT;
  }

  if (new_size == ino->size) {
    mutex_unlock(&st->lock);
    return 0;
  }

  if (new_size == 0) {
    kfree(ino->data);
    ino->data = NULL;
    ino->cap = 0;
    ino->size = 0;
    mutex_unlock(&st->lock);
    return 0;
  }

  if (new_size > ino->cap) {
    u64 newcap = max_t(u64, new_size, ino->cap ? (ino->cap * 2) : 4096);
    newbuf = krealloc(ino->data, newcap, GFP_KERNEL);
    if (!newbuf) {
      mutex_unlock(&st->lock);
      return -ENOMEM;
    }
    if (newcap > ino->cap)
      memset(newbuf + ino->cap, 0, newcap - ino->cap);
    ino->data = newbuf;
    ino->cap = newcap;
  }

  if (new_size > ino->size && ino->data)
    memset(ino->data + ino->size, 0, new_size - ino->size);

  ino->size = new_size;
  mutex_unlock(&st->lock);
  return 0;
}

const struct vtfs_backend_ops vtfs_ram_ops = {
  .init = vtfs_ram_init,
  .destroy = vtfs_ram_destroy,

  .getattr = vtfs_ram_getattr,
  .lookup = vtfs_ram_lookup,
  .readdir = vtfs_ram_readdir,

  .create = vtfs_ram_create,
  .unlink = vtfs_ram_unlink,
  .link = vtfs_ram_link,

  .read = vtfs_ram_read,
  .write = vtfs_ram_write,
  .truncate = vtfs_ram_truncate,
};
