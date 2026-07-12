from fastapi import FastAPI, Depends, Response
from fastapi.responses import Response as FastAPIResponse
from sqlalchemy.orm import Session
from sqlalchemy import and_
import base64
import struct
from database import get_db, engine, Base
from models import Inode, Dentry, Token

app = FastAPI()

VTFS_DIR = 0
VTFS_REG = 1

def pack_int64(value):
    return struct.pack('<q', value)

def pack_uint32(value):
    return struct.pack('<I', value)

def pack_uint64(value):
    return struct.pack('<Q', value)

def pack_uint16(value):
    return struct.pack('<H', value)

def pack_uint8(value):
    return struct.pack('<B', value)

def error_response(errno: int):
    return Response(content=pack_int64(errno), media_type="application/octet-stream")

def success_response(payload: bytes = b""):
    return Response(content=pack_int64(0) + payload, media_type="application/octet-stream")

@app.get("/api/init")
async def init(token: str, db: Session = Depends(get_db)):
    existing = db.query(Token).filter(Token.token == token).first()
    if existing:
        return success_response(pack_uint64(existing.root_id))
    
    # Create a NEW root inode for this token
    root = Inode(type=VTFS_DIR, mode=0o777, size=0, nlink=2)
    db.add(root)
    db.flush()  # Get the auto-generated ID
    
    new_token = Token(token=token, root_id=root.id)
    db.add(new_token)
    db.commit()
    
    return success_response(pack_uint64(root.id))

@app.get("/api/getattr")
async def getattr(token: str, id: int, db: Session = Depends(get_db)):
    token_obj = db.query(Token).filter(Token.token == token).first()
    if not token_obj:
        return error_response(2)
    
    inode = db.query(Inode).filter(Inode.id == id).first()
    if not inode:
        return error_response(2)
    
    payload = pack_uint8(inode.type) + pack_uint32(inode.mode) + pack_uint64(inode.size) + pack_uint32(inode.nlink)
    return success_response(payload)

@app.get("/api/lookup")
async def lookup(token: str, parent: int, name: str, db: Session = Depends(get_db)):
    token_obj = db.query(Token).filter(Token.token == token).first()
    if not token_obj:
        return error_response(2)
    
    dentry = db.query(Dentry).filter(and_(Dentry.parent_id == parent, Dentry.name == name)).first()
    if not dentry:
        return error_response(2)
    
    return success_response(pack_uint64(dentry.inode_id))

@app.get("/api/readdir")
async def readdir(token: str, dir: int, cursor: int, limit: int, db: Session = Depends(get_db)):
    token_obj = db.query(Token).filter(Token.token == token).first()
    if not token_obj:
        return error_response(2)
    
    dentries = db.query(Dentry).filter(Dentry.parent_id == dir).offset(cursor).limit(limit).all()
    
    payload = pack_uint32(len(dentries))
    for dentry in dentries:
        inode = db.query(Inode).filter(Inode.id == dentry.inode_id).first()
        name_bytes = dentry.name.encode('utf-8')
        payload += pack_uint64(dentry.inode_id)
        payload += pack_uint8(inode.type if inode else VTFS_REG)
        payload += pack_uint16(len(name_bytes))
        payload += name_bytes
    
    return success_response(payload)

@app.get("/api/create")
async def create(token: str, parent: int, name: str, type: str, mode: int, db: Session = Depends(get_db)):
    token_obj = db.query(Token).filter(Token.token == token).first()
    if not token_obj:
        return error_response(2)
    
    parent_inode = db.query(Inode).filter(Inode.id == parent).first()
    if not parent_inode or parent_inode.type != VTFS_DIR:
        return error_response(20)
    
    existing = db.query(Dentry).filter(and_(Dentry.parent_id == parent, Dentry.name == name)).first()
    if existing:
        return error_response(17)
    
    max_id = db.query(Inode.id).order_by(Inode.id.desc()).first()
    new_id = (max_id[0] + 1) if max_id else 1001
    
    inode_type = VTFS_DIR if type == "dir" else VTFS_REG
    new_inode = Inode(id=new_id, type=inode_type, mode=mode, size=0, nlink=1)
    db.add(new_inode)
    db.flush()
    
    new_dentry = Dentry(parent_id=parent, name=name, inode_id=new_id)
    db.add(new_dentry)
    
    db.commit()
    
    return success_response(pack_uint64(new_id))

@app.get("/api/unlink")
async def unlink(token: str, parent: int, name: str, db: Session = Depends(get_db)):
    token_obj = db.query(Token).filter(Token.token == token).first()
    if not token_obj:
        return error_response(2)
    
    dentry = db.query(Dentry).filter(and_(Dentry.parent_id == parent, Dentry.name == name)).first()
    if not dentry:
        return error_response(2)
    
    inode = db.query(Inode).filter(Inode.id == dentry.inode_id).first()
    if inode and inode.type == VTFS_DIR:
        children = db.query(Dentry).filter(Dentry.parent_id == inode.id).count()
        if children > 0:
            return error_response(39)
    
    db.delete(dentry)
    
    if inode:
        inode.nlink -= 1
        if inode.nlink == 0:
            db.delete(inode)
    
    db.commit()
    
    return success_response()

@app.get("/api/link")
async def link(token: str, old: int, parent: int, name: str, db: Session = Depends(get_db)):
    token_obj = db.query(Token).filter(Token.token == token).first()
    if not token_obj:
        return error_response(2)
    
    inode = db.query(Inode).filter(Inode.id == old).first()
    if not inode:
        return error_response(2)
    
    if inode.type == VTFS_DIR:
        return error_response(1)
    
    existing = db.query(Dentry).filter(and_(Dentry.parent_id == parent, Dentry.name == name)).first()
    if existing:
        return error_response(17)
    
    new_dentry = Dentry(parent_id=parent, name=name, inode_id=old)
    db.add(new_dentry)
    
    inode.nlink += 1
    
    db.commit()
    
    return success_response()

@app.get("/api/read")
async def read(token: str, id: int, off: int, len: int, db: Session = Depends(get_db)):
    import builtins
    _len = builtins.len
    token_obj = db.query(Token).filter(Token.token == token).first()
    if not token_obj:
        return error_response(2)
    
    inode = db.query(Inode).filter(Inode.id == id).first()
    if not inode:
        return error_response(2)
    
    if not inode.data:
        return success_response(pack_uint32(0))
    
    data = inode.data[off:off+len]
    return success_response(pack_uint32(_len(data)) + data)

@app.get("/api/write")
async def write(token: str, id: int, off: int, len: int, data: str, db: Session = Depends(get_db)):
    import builtins
    _len = builtins.len
    
    token_obj = db.query(Token).filter(Token.token == token).first()
    if not token_obj:
        return error_response(2)
    
    inode = db.query(Inode).filter(Inode.id == id).first()
    if not inode:
        return error_response(2)
    
    decoded_data = base64.urlsafe_b64decode(data + '==')
    
    if not inode.data:
        inode.data = b'\x00' * off + decoded_data
    else:
        current = bytearray(inode.data)
        if off > _len(current):
            current.extend(b'\x00' * (off - _len(current)))
        
        end = off + _len(decoded_data)
        if end > _len(current):
            current.extend(b'\x00' * (end - _len(current)))
        
        current[off:off+_len(decoded_data)] = decoded_data
        inode.data = bytes(current)
    
    inode.size = max(inode.size, off + _len(decoded_data))
    
    db.commit()
    
    return success_response()


@app.get("/api/truncate")
async def truncate(token: str, id: int, sz: int, db: Session = Depends(get_db)):
    token_obj = db.query(Token).filter(Token.token == token).first()
    if not token_obj:
        return error_response(2)
    
    inode = db.query(Inode).filter(Inode.id == id).first()
    if not inode:
        return error_response(2)
    
    if sz == 0:
        inode.data = None
        inode.size = 0
    else:
        if not inode.data:
            inode.data = b'\x00' * sz
        elif len(inode.data) > sz:
            inode.data = inode.data[:sz]
        else:
            inode.data = inode.data + b'\x00' * (sz - len(inode.data))
        inode.size = sz
    
    db.commit()
    
    return success_response()
