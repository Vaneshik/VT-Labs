from sqlalchemy import Column, BigInteger, SmallInteger, Integer, String, LargeBinary, ForeignKey, UniqueConstraint, DateTime
from sqlalchemy.sql import func
from database import Base

class Inode(Base):
    __tablename__ = 'inodes'
    
    id = Column(BigInteger, primary_key=True)
    type = Column(SmallInteger, nullable=False)
    mode = Column(Integer, nullable=False, default=0o777)
    size = Column(BigInteger, nullable=False, default=0)
    nlink = Column(Integer, nullable=False, default=1)
    data = Column(LargeBinary, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())

class Dentry(Base):
    __tablename__ = 'dentries'
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    parent_id = Column(BigInteger, ForeignKey('inodes.id', ondelete='CASCADE'), nullable=False)
    name = Column(String(255), nullable=False)
    inode_id = Column(BigInteger, ForeignKey('inodes.id', ondelete='CASCADE'), nullable=False)
    
    __table_args__ = (UniqueConstraint('parent_id', 'name', name='_parent_name_uc'),)

class Token(Base):
    __tablename__ = 'tokens'
    
    token = Column(String(255), primary_key=True)
    root_id = Column(BigInteger, ForeignKey('inodes.id', ondelete='CASCADE'), nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
