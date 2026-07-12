from database import engine, Base, SessionLocal
from models import Inode, Dentry, Token

def init_db():
    Base.metadata.create_all(bind=engine)
    print("Database tables created successfully")

if __name__ == "__main__":
    init_db()
