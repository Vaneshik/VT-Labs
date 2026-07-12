#include <linux/errno.h>
#include <linux/slab.h>
#include <linux/string.h>
#include <linux/types.h>
#include <linux/kernel.h>

#include "vtfs_backend.h"
#include "http.h"

struct vtfs_http_state {
  char *token;
};

static inline u16 vtfs_get_le16(const void *p)
{
  const u8 *b = (const u8 *)p;
  return (u16)b[0] | ((u16)b[1] << 8);
}

static inline u32 vtfs_get_le32(const void *p)
{
  const u8 *b = (const u8 *)p;
  return (u32)b[0]
       | ((u32)b[1] << 8)
       | ((u32)b[2] << 16)
       | ((u32)b[3] << 24);
}

static inline u64 vtfs_get_le64(const void *p)
{
  const u8 *b = (const u8 *)p;
  return (u64)b[0]
       | ((u64)b[1] << 8)
       | ((u64)b[2] << 16)
       | ((u64)b[3] << 24)
       | ((u64)b[4] << 32)
       | ((u64)b[5] << 40)
       | ((u64)b[6] << 48)
       | ((u64)b[7] << 56);
}

static inline void vtfs_put_u64(char *dst, u64 v)
{
  (void)scnprintf(dst, 32, "%llu", (unsigned long long)v);
}

static inline void vtfs_put_u32(char *dst, u32 v)
{
  (void)scnprintf(dst, 16, "%u", v);
}

static inline void vtfs_put_u16_octal_mode(char *dst, umode_t mode)
{
  (void)scnprintf(dst, 16, "%u", (u32)(mode & 0777));
}

static char *vtfs_urlenc_dup(const char *s)
{
  size_t n = strlen(s);
  char *enc = kmalloc(3 * n + 1, GFP_KERNEL);
  if (!enc) return NULL;
  encode(s, enc);
  return enc;
}

static const char vtfs_b64url_tbl[] =
  "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";

static char *vtfs_b64url_encode(const u8 *in, size_t inlen, size_t *outlen)
{
  size_t olen = ((inlen + 2) / 3) * 4;
  char *out = kmalloc(olen + 1, GFP_KERNEL);
  size_t i = 0, o = 0;

  if (!out) return NULL;

  while (i < inlen) {
    u32 v = 0;
    int n = 0;

    v |= (u32)in[i++] << 16; n++;
    if (i < inlen) { v |= (u32)in[i++] << 8; n++; }
    if (i < inlen) { v |= (u32)in[i++]; n++; }

    out[o++] = vtfs_b64url_tbl[(v >> 18) & 0x3f];
    out[o++] = vtfs_b64url_tbl[(v >> 12) & 0x3f];
    out[o++] = (n >= 2) ? vtfs_b64url_tbl[(v >> 6) & 0x3f] : '=';
    out[o++] = (n == 3) ? vtfs_b64url_tbl[v & 0x3f] : '=';
  }

  out[o] = '\0';
  if (outlen) *outlen = o;
  return out;
}

static inline int vtfs_http_to_errno(int64_t rv)
{
  if (rv == 0) return 0;
  if (rv < 0) return (int)rv;
  return -(int)rv;
}

static int vtfs_http_init(struct vtfs_backend *b, const char *token)
{
  struct vtfs_http_state *st;
  char resp[32];
  int64_t rv;

  st = kzalloc(sizeof(*st), GFP_KERNEL);
  if (!st) return -ENOMEM;

  st->token = kstrdup(token ? token : "", GFP_KERNEL);
  if (!st->token) {
    kfree(st);
    return -ENOMEM;
  }

  b->priv = st;

  rv = vtfs_http_call(st->token, "init", resp, sizeof(resp), 0);
  if (rv != 0) {
    int err = vtfs_http_to_errno(rv);
    return err ? err : -EIO;
  }

  b->root_id = (vtfs_id)vtfs_get_le64(resp);
  return 0;
}

static void vtfs_http_destroy(struct vtfs_backend *b)
{
  struct vtfs_http_state *st = (struct vtfs_http_state *)b->priv;
  if (!st) return;
  kfree(st->token);
  kfree(st);
  b->priv = NULL;
}

static int vtfs_http_getattr(struct vtfs_backend *b, vtfs_id id, struct vtfs_attr *out)
{
  struct vtfs_http_state *st = (struct vtfs_http_state *)b->priv;
  char id_s[32];
  char resp[64];
  int64_t rv;

  if (!st || !out) return -EINVAL;

  vtfs_put_u64(id_s, (u64)id);
  rv = vtfs_http_call(st->token, "getattr", resp, sizeof(resp), 1,
                      (char *)"id", (char *)id_s);
  if (rv != 0) return vtfs_http_to_errno(rv);

  out->id = id;
  out->type = (resp[0] == 0) ? VTFS_DIR : VTFS_REG;
  out->mode = (umode_t)(vtfs_get_le32(resp + 1) & 0777);
  out->size = vtfs_get_le64(resp + 5);
  out->nlink = vtfs_get_le32(resp + 13);
  out->atime_ns = out->mtime_ns = out->ctime_ns = 0;
  return 0;
}

static int vtfs_http_lookup(struct vtfs_backend *b, vtfs_id parent, const char *name, vtfs_id *out_id)
{
  struct vtfs_http_state *st = (struct vtfs_http_state *)b->priv;
  char parent_s[32];
  char resp[16];
  char *name_enc;
  int64_t rv;

  if (!st || !name || !out_id) return -EINVAL;

  name_enc = vtfs_urlenc_dup(name);
  if (!name_enc) return -ENOMEM;

  vtfs_put_u64(parent_s, (u64)parent);
  rv = vtfs_http_call(st->token, "lookup", resp, sizeof(resp), 2,
                      (char *)"parent", (char *)parent_s,
                      (char *)"name",   (char *)name_enc);
  kfree(name_enc);

  if (rv != 0) return vtfs_http_to_errno(rv);

  *out_id = (vtfs_id)vtfs_get_le64(resp);
  return 0;
}

static int vtfs_http_readdir(struct vtfs_backend *b, vtfs_id dir, u64 cursor,
                             struct vtfs_dirent *out, u32 out_cap,
                             u32 *out_n, u64 *out_next_cursor)
{
  struct vtfs_http_state *st = (struct vtfs_http_state *)b->priv;
  char dir_s[32], cur_s[32], lim_s[16];
  size_t resp_sz = 4 + (size_t)out_cap * 266;
  char *resp;
  int64_t rv;
  u32 n, i;
  size_t off;

  if (!st || !out || !out_n || !out_next_cursor) return -EINVAL;

  resp = kmalloc(resp_sz, GFP_KERNEL);
  if (!resp) return -ENOMEM;

  vtfs_put_u64(dir_s, (u64)dir);
  vtfs_put_u64(cur_s, (u64)cursor);
  vtfs_put_u32(lim_s, out_cap);

  rv = vtfs_http_call(st->token, "readdir", resp, resp_sz, 3,
                      (char *)"dir",    (char *)dir_s,
                      (char *)"cursor", (char *)cur_s,
                      (char *)"limit",  (char *)lim_s);
  if (rv != 0) {
    int err = vtfs_http_to_errno(rv);
    kfree(resp);
    return err;
  }

  n = vtfs_get_le32(resp);
  off = 4;

  if (n > out_cap) n = out_cap;

  for (i = 0; i < n; i++) {
    u64 id;
    u8 t;
    u16 nl;

    if (off + 8 + 1 + 2 > resp_sz) break;

    id = vtfs_get_le64(resp + off); off += 8;
    t  = *(u8 *)(resp + off);       off += 1;
    nl = vtfs_get_le16(resp + off); off += 2;

    if (nl >= sizeof(out[i].name)) nl = sizeof(out[i].name) - 1;
    if (off + nl > resp_sz) break;

    memcpy(out[i].name, resp + off, nl);
    out[i].name[nl] = '\0';
    off += nl;

    out[i].id = (vtfs_id)id;
    out[i].type = (t == 0) ? VTFS_DIR : VTFS_REG;
  }

  *out_n = i;
  *out_next_cursor = cursor + i;

  kfree(resp);
  return 0;
}

static int vtfs_http_create(struct vtfs_backend *b, vtfs_id parent, const char *name,
                            enum vtfs_type type, umode_t mode, vtfs_id *out_id)
{
  struct vtfs_http_state *st = (struct vtfs_http_state *)b->priv;
  char parent_s[32], mode_s[16];
  char resp[16];
  char *name_enc;
  const char *type_s = (type == VTFS_DIR) ? "dir" : "reg";
  int64_t rv;

  if (!st || !name || !out_id) return -EINVAL;

  name_enc = vtfs_urlenc_dup(name);
  if (!name_enc) return -ENOMEM;

  vtfs_put_u64(parent_s, (u64)parent);
  vtfs_put_u16_octal_mode(mode_s, mode);

  rv = vtfs_http_call(st->token, "create", resp, sizeof(resp), 4,
                      (char *)"parent", (char *)parent_s,
                      (char *)"name",   (char *)name_enc,
                      (char *)"type",   (char *)type_s,
                      (char *)"mode",   (char *)mode_s);
  kfree(name_enc);

  if (rv != 0) return vtfs_http_to_errno(rv);

  *out_id = (vtfs_id)vtfs_get_le64(resp);
  return 0;
}

static int vtfs_http_unlink(struct vtfs_backend *b, vtfs_id parent, const char *name)
{
  struct vtfs_http_state *st = (struct vtfs_http_state *)b->priv;
  char parent_s[32];
  char dummy[8];
  char *name_enc;
  int64_t rv;

  if (!st || !name) return -EINVAL;

  name_enc = vtfs_urlenc_dup(name);
  if (!name_enc) return -ENOMEM;

  vtfs_put_u64(parent_s, (u64)parent);

  rv = vtfs_http_call(st->token, "unlink", dummy, sizeof(dummy), 2,
                      (char *)"parent", (char *)parent_s,
                      (char *)"name",   (char *)name_enc);
  kfree(name_enc);

  if (rv != 0) return vtfs_http_to_errno(rv);
  return 0;
}

static int vtfs_http_link(struct vtfs_backend *b, vtfs_id old_id, vtfs_id new_parent, const char *new_name)
{
  struct vtfs_http_state *st = (struct vtfs_http_state *)b->priv;
  char old_s[32], par_s[32];
  char dummy[8];
  char *name_enc;
  int64_t rv;

  if (!st || !new_name) return -EINVAL;

  name_enc = vtfs_urlenc_dup(new_name);
  if (!name_enc) return -ENOMEM;

  vtfs_put_u64(old_s, (u64)old_id);
  vtfs_put_u64(par_s, (u64)new_parent);

  rv = vtfs_http_call(st->token, "link", dummy, sizeof(dummy), 3,
                      (char *)"old",    (char *)old_s,
                      (char *)"parent", (char *)par_s,
                      (char *)"name",   (char *)name_enc);
  kfree(name_enc);

  if (rv != 0) return vtfs_http_to_errno(rv);
  return 0;
}

static ssize_t vtfs_http_read(struct vtfs_backend *b, vtfs_id file, u64 off, void *buf, size_t len)
{
  struct vtfs_http_state *st = (struct vtfs_http_state *)b->priv;
  char id_s[32], off_s[32], len_s[32];
  int64_t rv;

  if (!st || (!buf && len)) return -EINVAL;

  vtfs_put_u64(id_s, (u64)file);
  vtfs_put_u64(off_s, (u64)off);
  vtfs_put_u64(len_s, (u64)len);

  rv = vtfs_http_call(st->token, "read", (char *)buf, len, 3,
                      (char *)"id",  (char *)id_s,
                      (char *)"off", (char *)off_s,
                      (char *)"len", (char *)len_s);
  if (rv != 0) return (ssize_t)vtfs_http_to_errno(rv);

  {
    u32 nread = vtfs_get_le32(buf);
    if (nread > len - 4) nread = len - 4;
    memmove(buf, (u8 *)buf + 4, nread);
    return (ssize_t)nread;
  }
}

static ssize_t vtfs_http_write(struct vtfs_backend *b, vtfs_id file, u64 off, const void *buf, size_t len)
{
  struct vtfs_http_state *st = (struct vtfs_http_state *)b->priv;
  char id_s[32], off_s[32], len_s[32];
  char dummy[8];
  char *b64;
  size_t b64len = 0;
  int64_t rv;

  if (!st || (!buf && len)) return -EINVAL;

  b64 = vtfs_b64url_encode((const u8 *)buf, len, &b64len);
  if (!b64) return -ENOMEM;

  vtfs_put_u64(id_s, (u64)file);
  vtfs_put_u64(off_s, (u64)off);
  vtfs_put_u64(len_s, (u64)len);

  rv = vtfs_http_call(st->token, "write", dummy, sizeof(dummy), 4,
                      (char *)"id",   (char *)id_s,
                      (char *)"off",  (char *)off_s,
                      (char *)"len",  (char *)len_s,
                      (char *)"data", (char *)b64);
  kfree(b64);

  if (rv != 0) return (ssize_t)vtfs_http_to_errno(rv);
  return (ssize_t)len;
}

static int vtfs_http_truncate(struct vtfs_backend *b, vtfs_id file, u64 new_size)
{
  struct vtfs_http_state *st = (struct vtfs_http_state *)b->priv;
  char id_s[32], sz_s[32];
  char dummy[8];
  int64_t rv;

  if (!st) return -EINVAL;

  vtfs_put_u64(id_s, (u64)file);
  vtfs_put_u64(sz_s, (u64)new_size);

  rv = vtfs_http_call(st->token, "truncate", dummy, sizeof(dummy), 2,
                      (char *)"id", (char *)id_s,
                      (char *)"sz", (char *)sz_s);
  if (rv != 0) return vtfs_http_to_errno(rv);
  return 0;
}

const struct vtfs_backend_ops vtfs_http_ops = {
  .init = vtfs_http_init,
  .destroy = vtfs_http_destroy,

  .getattr = vtfs_http_getattr,
  .lookup = vtfs_http_lookup,
  .readdir = vtfs_http_readdir,

  .create = vtfs_http_create,
  .unlink = vtfs_http_unlink,
  .link = vtfs_http_link,

  .read = vtfs_http_read,
  .write = vtfs_http_write,
  .truncate = vtfs_http_truncate,
};
