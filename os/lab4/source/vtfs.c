#include <linux/fs.h>
#include <linux/fcntl.h>
#include <linux/init.h>
#include <linux/kernel.h>
#include <linux/module.h>
#include <linux/printk.h>
#include <linux/processor.h>
#include <linux/slab.h>
#include <linux/stat.h>
#include <linux/string.h>
#include <linux/uaccess.h>
#include "vtfs_backend.h"

#define MODULE_NAME "vtfs"

MODULE_LICENSE("GPL");
MODULE_AUTHOR("secs-dev");
MODULE_DESCRIPTION("A simple FS kernel module");

#define LOG(fmt, ...) pr_info("[" MODULE_NAME "]: " fmt, ##__VA_ARGS__)

static inline struct vtfs_backend *VTFS_B(struct super_block *sb) {
  return (struct vtfs_backend *)sb->s_fs_info;
}

extern const struct vtfs_backend_ops vtfs_ram_ops;
extern const struct vtfs_backend_ops vtfs_http_ops;

void vtfs_kill_sb(struct super_block*);
struct dentry* vtfs_mount(struct file_system_type*, int, const char*, void*);
int vtfs_fill_super(struct super_block*, void*, int);
struct inode* vtfs_get_inode(struct super_block*, const struct inode*, umode_t, int);
struct dentry* vtfs_lookup(struct inode*, struct dentry*, unsigned int);
int vtfs_iterate(struct file*, struct dir_context*);
int vtfs_create(struct mnt_idmap*, struct inode*, struct dentry*, umode_t, bool);
int vtfs_unlink(struct inode*, struct dentry*);
int vtfs_mkdir(struct mnt_idmap*, struct inode*, struct dentry*, umode_t);
int vtfs_rmdir(struct inode*, struct dentry*);
ssize_t vtfs_read(struct file*, char __user*, size_t, loff_t*);
ssize_t vtfs_write(struct file*, const char __user*, size_t, loff_t*);
int vtfs_open(struct inode*, struct file*);
int vtfs_link(struct dentry*, struct inode*, struct dentry*);
int vtfs_setattr(struct mnt_idmap *idmap, struct dentry *dentry, struct iattr *iattr);

struct file_system_type vtfs_fs_type = {
  .name = "vtfs",
  .mount = vtfs_mount,
  .kill_sb = vtfs_kill_sb,
};

struct file_operations vtfs_dir_ops = {
  .iterate_shared = vtfs_iterate,
};

static const struct inode_operations vtfs_file_inode_ops = {
  .setattr = vtfs_setattr,
};

struct file_operations vtfs_file_ops = {
  .open  = vtfs_open,
  .read  = vtfs_read,
  .write = vtfs_write,
  .llseek = default_llseek,
};

struct inode_operations vtfs_inode_ops = {
  .lookup = vtfs_lookup,
  .create = vtfs_create,
  .unlink  = vtfs_unlink,
  .mkdir  = vtfs_mkdir,
  .rmdir  = vtfs_rmdir,
  .link   = vtfs_link,
};

int vtfs_open(struct inode* inode, struct file* filp)
{
  struct vtfs_backend *be = VTFS_B(inode->i_sb);

  if ((filp->f_flags & O_TRUNC) && be && be->ops && be->ops->truncate) {
    int err = be->ops->truncate(be, (vtfs_id)inode->i_ino, 0);
    if (err) return err;
    i_size_write(inode, 0);
  }
  return 0;
}

ssize_t vtfs_read(struct file *filp, char __user *ubuf, size_t len, loff_t *off)
{
  struct inode *inode = file_inode(filp);
  struct vtfs_backend *be = VTFS_B(inode->i_sb);
  size_t done = 0;
  int err;

  if (!be || !be->ops || !be->ops->read)
    return -EOPNOTSUPP;

  while (done < len) {
    size_t chunk = min_t(size_t, 4096, len - done);
    char *kbuf = kmalloc(chunk, GFP_KERNEL);
    ssize_t rd;

    if (!kbuf)
      return done ? (ssize_t)done : -ENOMEM;

    rd = be->ops->read(be, (vtfs_id)inode->i_ino, (u64)(*off), kbuf, chunk);
    if (rd < 0) {
      kfree(kbuf);
      return done ? (ssize_t)done : rd;
    }
    if (rd == 0) {
      kfree(kbuf);
      break;
    }

    err = copy_to_user(ubuf + done, kbuf, rd);
    kfree(kbuf);
    if (err)
      return done ? (ssize_t)done : -EFAULT;

    done += rd;
    *off += rd;
  }

  return (ssize_t)done;
}

ssize_t vtfs_write(struct file *filp, const char __user *ubuf, size_t len, loff_t *off)
{
  struct inode *inode = file_inode(filp);
  struct vtfs_backend *be = VTFS_B(inode->i_sb);
  size_t done = 0;

  if (!be || !be->ops || !be->ops->write)
    return -EOPNOTSUPP;
  
  if (filp->f_flags & O_APPEND) {
    *off = i_size_read(inode);
  }

  while (done < len) {
    size_t chunk = min_t(size_t, 4096, len - done);
    char *kbuf = kmalloc(chunk, GFP_KERNEL);
    ssize_t wr;

    if (!kbuf)
      return done ? (ssize_t)done : -ENOMEM;

    if (copy_from_user(kbuf, ubuf + done, chunk)) {
      kfree(kbuf);
      return done ? (ssize_t)done : -EFAULT;
    }

    wr = be->ops->write(be, (vtfs_id)inode->i_ino, (u64)(*off), kbuf, chunk);
    kfree(kbuf);
    if (wr < 0)
      return done ? (ssize_t)done : wr;

    done += wr;
    *off += wr;
  }

  if (be && be->ops && be->ops->getattr) {
    struct vtfs_attr a;
    if (be->ops->getattr(be, (vtfs_id)inode->i_ino, &a) == 0)
      i_size_write(inode, (loff_t)a.size);
  }

  return (ssize_t)done;
}

int vtfs_setattr(struct mnt_idmap *idmap, struct dentry *dentry, struct iattr *iattr)
{
  struct inode *inode = d_inode(dentry);
  struct vtfs_backend *be = VTFS_B(inode->i_sb);

  (void)idmap;

  if ((iattr->ia_valid & ATTR_SIZE) && be && be->ops && be->ops->truncate) {
    int err = be->ops->truncate(be, (vtfs_id)inode->i_ino, (u64)iattr->ia_size);
    if (err)
      return err;
    i_size_write(inode, iattr->ia_size);
  }

  mark_inode_dirty(inode);
  return 0;
}

int vtfs_create(struct mnt_idmap* idmap,
                struct inode* parent_inode,
                struct dentry* child_dentry,
                umode_t mode,
                bool bflag)
{
  struct vtfs_backend *b = VTFS_B(parent_inode->i_sb);
  vtfs_id new_id;
  struct vtfs_attr attr;
  int err;

  (void)idmap;
  (void)bflag;

  err = b->ops->create(b, (vtfs_id)parent_inode->i_ino, child_dentry->d_name.name,
                      VTFS_REG, mode, &new_id);
  if (err) return err;

  err = b->ops->getattr(b, new_id, &attr);
  if (err) return err;

  umode_t imode = S_IFREG | (attr.mode & 0777);
  struct inode *inode = vtfs_get_inode(parent_inode->i_sb, parent_inode, imode, (int)new_id);
  if (!inode) return -ENOMEM;

  inode->i_op = &vtfs_file_inode_ops;
  inode->i_fop = &vtfs_file_ops;
  i_size_write(inode, (loff_t)attr.size);
  set_nlink(inode, attr.nlink ? attr.nlink : 1);

  d_add(child_dentry, inode);
  return 0;
}

int vtfs_unlink(struct inode* parent_inode, struct dentry* child_dentry) {
  struct vtfs_backend *b = VTFS_B(parent_inode->i_sb);
  int err = b->ops->unlink(b, (vtfs_id)parent_inode->i_ino, child_dentry->d_name.name);
  if (err) return err;
  d_drop(child_dentry);
  return 0;
}

int vtfs_iterate(struct file* flip, struct dir_context* ctx) {
  struct dentry* dentry = flip->f_path.dentry;
  struct inode* inode = dentry->d_inode;
  struct vtfs_backend *b = VTFS_B(inode->i_sb);

  if (ctx->pos == 0) {
    if (!dir_emit(ctx, ".", 1, inode->i_ino, DT_DIR)) return 0;
    ctx->pos++;
  }
  if (ctx->pos == 1) {
    ino_t pino = dentry->d_parent->d_inode->i_ino;
    if (!dir_emit(ctx, "..", 2, pino, DT_DIR)) return 0;
    ctx->pos++;
  }

  while (true) {
    struct vtfs_dirent ents[8];
    u32 n = 0;
    u64 next_cursor = 0;
    u64 cursor = (ctx->pos >= 2) ? (u64)(ctx->pos - 2) : 0;

    int err = b->ops->readdir(b, (vtfs_id)inode->i_ino, cursor,
                             ents, 8, &n, &next_cursor);
    if (err) return err;
    if (n == 0) return 0;

    for (u32 i = 0; i < n; i++) {
      unsigned char dtype = (ents[i].type == VTFS_DIR) ? DT_DIR : DT_REG;
      if (!dir_emit(ctx, ents[i].name, strlen(ents[i].name), (ino_t)ents[i].id, dtype))
        return 0;
      ctx->pos++;
    }
  }
}

int vtfs_mkdir(struct mnt_idmap* idmap,
               struct inode* parent_inode,
               struct dentry* child_dentry,
               umode_t mode)
{
  struct vtfs_backend *be = VTFS_B(parent_inode->i_sb);
  vtfs_id new_id;
  struct vtfs_attr attr;
  int err;

  (void)idmap;

  err = be->ops->create(be,
                        (vtfs_id)parent_inode->i_ino,
                        child_dentry->d_name.name,
                        VTFS_DIR,
                        mode,
                        &new_id);
  if (err) return err;

  err = be->ops->getattr(be, new_id, &attr);
  if (err) return err;

  umode_t imode = S_IFDIR | (attr.mode & 0777);
  struct inode *inode = vtfs_get_inode(parent_inode->i_sb, parent_inode, imode, (int)new_id);
  if (!inode) return -ENOMEM;

  inode->i_op = &vtfs_inode_ops;
  inode->i_fop = &vtfs_dir_ops;

  d_add(child_dentry, inode);
  return 0;
}

int vtfs_rmdir(struct inode* parent_inode, struct dentry* child_dentry)
{
  struct vtfs_backend *be = VTFS_B(parent_inode->i_sb);
  int err = be->ops->unlink(be,
                            (vtfs_id)parent_inode->i_ino,
                            child_dentry->d_name.name);
  if (err) return err;

  d_drop(child_dentry);
  return 0;
}

int vtfs_link(struct dentry *old_dentry, struct inode *parent_dir, struct dentry *new_dentry) {
  struct inode *old_inode = d_inode(old_dentry);
  struct vtfs_backend *be = VTFS_B(parent_dir->i_sb);
  struct vtfs_attr attr;
  vtfs_id old_id;
  int err;

  if (!old_inode)
    return -ENOENT;

  if (S_ISDIR(old_inode->i_mode))
    return -EPERM;

  if (!be || !be->ops || !be->ops->link)
    return -EOPNOTSUPP;

  old_id = (vtfs_id)old_inode->i_ino;

  err = be->ops->link(be, old_id, (vtfs_id)parent_dir->i_ino, new_dentry->d_name.name);
  if (err)
    return err;

  err = be->ops->getattr(be, old_id, &attr);
  if (err)
    return err;

  set_nlink(old_inode, attr.nlink ? attr.nlink : 1);
  mark_inode_dirty(old_inode);

  {
    umode_t imode = S_IFREG | (attr.mode & 0777);
    struct inode *inode = vtfs_get_inode(parent_dir->i_sb, parent_dir, imode, (int)old_id);
    if (!inode)
      return -ENOMEM;

    inode->i_op = &vtfs_file_inode_ops;
    inode->i_fop = &vtfs_file_ops;
    i_size_write(inode, (loff_t)attr.size);
    set_nlink(inode, attr.nlink ? attr.nlink : 1);
    mark_inode_dirty(inode);

    d_add(new_dentry, inode);
  }

  return 0;
}

struct dentry* vtfs_lookup(
    struct inode* parent_inode, struct dentry* child_dentry, unsigned int flag
) {
  struct vtfs_backend *b = VTFS_B(parent_inode->i_sb);
  vtfs_id child_id;
  struct vtfs_attr attr;
  const char *name = child_dentry->d_name.name;
  int err;

  (void)flag;

  err = b->ops->lookup(b, (vtfs_id)parent_inode->i_ino, name, &child_id);
  if (err == -ENOENT) {
    d_add(child_dentry, NULL);
    return NULL;
  }
  if (err) return ERR_PTR(err);

  err = b->ops->getattr(b, child_id, &attr);
  if (err) return ERR_PTR(err);

  umode_t mode = (attr.type == VTFS_DIR ? S_IFDIR : S_IFREG) | (attr.mode & 0777);

  struct inode *inode = vtfs_get_inode(parent_inode->i_sb, parent_inode, mode, (int)child_id);
  if (!inode) return ERR_PTR(-ENOMEM);

  if (attr.type == VTFS_DIR) {
    inode->i_op = &vtfs_inode_ops;
    inode->i_fop = &vtfs_dir_ops;
    set_nlink(inode, 2);
  } else {
    inode->i_op = &vtfs_file_inode_ops;
    inode->i_fop = &vtfs_file_ops;
    i_size_write(inode, (loff_t)attr.size);
    set_nlink(inode, attr.nlink ? attr.nlink : 1);
  }

  d_add(child_dentry, inode);
  return NULL;
}

struct inode* vtfs_get_inode(
    struct super_block* sb, const struct inode* dir, umode_t mode, int i_ino
) {
  struct inode* inode = iget_locked(sb, (unsigned long)i_ino);
  struct mnt_idmap* idmap = &nop_mnt_idmap;

  if (!inode)
    return NULL;

  inode->i_mode = mode;

  if (S_ISDIR(mode)) {
    inode->i_op  = &vtfs_inode_ops;
    inode->i_fop = &vtfs_dir_ops;
    set_nlink(inode, 2);
  } else {
    inode->i_op  = &vtfs_file_inode_ops;
    inode->i_fop = &vtfs_file_ops;
  }

  if (!(inode->i_state & I_NEW))
    return inode;

  inode_init_owner(idmap, inode, dir, mode);
  inode->i_ino = i_ino;

  if (S_ISDIR(mode)) {
    i_size_write(inode, 0);
    set_nlink(inode, 2);
  } else {
    i_size_write(inode, 0);
    set_nlink(inode, 1);
  }

  unlock_new_inode(inode);
  return inode;
}

int vtfs_fill_super(struct super_block* sb, void* data, int silent) {
  const char *token = (const char *)data;

  struct vtfs_backend *b = kzalloc(sizeof(*b), GFP_KERNEL);
  if (!b) return -ENOMEM;

  b->ops = &vtfs_http_ops;

  if (token && !strncmp(token, "ram:", 4)) {
    b->ops = &vtfs_ram_ops;
  }

  sb->s_fs_info = b;

  {
    int err = b->ops->init(b, token);
    if (err) {
      kfree(b);
      sb->s_fs_info = NULL;
      return err;
    }
  }

  struct inode* inode = vtfs_get_inode(sb, NULL, S_IFDIR | 0777, (int)b->root_id);

  if (inode == NULL) {
    return -ENOMEM;
  }

  inode->i_op = &vtfs_inode_ops;
  inode->i_fop = &vtfs_dir_ops;
  set_nlink(inode, 2);

  sb->s_root = d_make_root(inode);
  if (sb->s_root == NULL) {
    return -ENOMEM;
  }

  printk(KERN_INFO "return 0\n");
  return 0;
}

void vtfs_kill_sb(struct super_block* sb) {
  struct vtfs_backend *b = VTFS_B(sb);
  if (b) {
    if (b->ops && b->ops->destroy)
      b->ops->destroy(b);
    kfree(b);
    sb->s_fs_info = NULL;
  }
  printk(KERN_INFO "vtfs super block is destroyed. Unmount successfully.\n");
}

struct dentry* vtfs_mount(
    struct file_system_type* fs_type, int flags, const char* token, void* data
) {
  struct dentry* ret = mount_nodev(fs_type, flags, (void*)token, vtfs_fill_super);

  if (IS_ERR(ret)) {
    printk(KERN_ERR "Can't mount file system: %ld\n", PTR_ERR(ret));
    return ret;
  }

  printk(KERN_INFO "Mounted successfully\n");
  return ret;
}

static int __init vtfs_init(void) {
  int err = register_filesystem(&vtfs_fs_type);
  if (err) {
    LOG("register_filesystem failed: %d\n", err);
    return err;
  }

  LOG("VTFS joined the kernel\n");
  return 0;
}

static void __exit vtfs_exit(void) {
  int err = unregister_filesystem(&vtfs_fs_type);
  if (err) {
    LOG("unregister_filesystem failed: %d\n", err);
  }

  LOG("VTFS left the kernel\n");
}

module_init(vtfs_init);
module_exit(vtfs_exit);
