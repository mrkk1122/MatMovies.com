from fastapi import FastAPI, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List, Optional
import models
import schemas
from database import engine, get_db, Base

# Create tables in the PostgreSQL database automatically upon startup
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="MatMovies Backend Service",
    description="Production-grade REST APIs for MatMovies Android App integration.",
    version="1.0.0"
)

# Root endpoint
@app.get("/")
def read_root():
    return {"message": "Welcome to MatMovies FastAPI + PostgreSQL Server!", "status": "Running"}

# --- AUTHENTICATION ---
@app.post("/api/auth/register", response_model=schemas.UserResponse)
def register_user(user: schemas.UserCreate, db: Session = Depends(get_db)):
    db_user = db.query(models.User).filter(models.User.email == user.email).first()
    if db_user:
        raise HTTPException(status_code=400, detail="Email already registered")
    
    new_user = models.User(
        id=user.id,
        name=user.name,
        email=user.email,
        password=user.password, # For real production, use passlib/bcrypt to hash passwords
        subscription_status="Free"
    )
    db.add(new_user)
    db.commit()
    db.refresh(new_user)
    return new_user

@app.post("/api/auth/login", response_model=schemas.UserResponse)
def login_user(login_data: schemas.UserLogin, db: Session = Depends(get_db)):
    db_user = db.query(models.User).filter(models.User.email == login_data.email).first()
    if not db_user or db_user.password != login_data.password:
        raise HTTPException(status_code=401, detail="Invalid email or password")
    return db_user

@app.get("/api/users/{user_id}", response_model=schemas.UserResponse)
def get_user_profile(user_id: str, db: Session = Depends(get_db)):
    db_user = db.query(models.User).filter(models.User.id == user_id).first()
    if not db_user:
        raise HTTPException(status_code=404, detail="User not found")
    return db_user

@app.put("/api/users/{user_id}/subscription", response_model=schemas.UserResponse)
def upgrade_subscription(user_id: str, plan: str, db: Session = Depends(get_db)):
    db_user = db.query(models.User).filter(models.User.id == user_id).first()
    if not db_user:
        raise HTTPException(status_code=404, detail="User not found")
    db_user.subscription_status = plan
    db.commit()
    db.refresh(db_user)
    return db_user


# --- CATEGORIES ---
@app.get("/api/categories", response_model=List[schemas.CategoryResponse])
def get_categories(db: Session = Depends(get_db)):
    return db.query(models.Category).all()

@app.post("/api/categories", response_model=schemas.CategoryResponse)
def create_category(category: schemas.CategoryBase, db: Session = Depends(get_db)):
    db_cat = models.Category(name=category.name, description=category.description, icon_name=category.icon_name)
    db.add(db_cat)
    db.commit()
    db.refresh(db_cat)
    return db_cat


# --- MOVIES ---
@app.get("/api/movies", response_model=List[schemas.MovieResponse])
def get_movies(
    genre: Optional[str] = None, 
    search: Optional[str] = None, 
    db: Session = Depends(get_db)
):
    query = db.query(models.Movie)
    if genre:
        query = query.filter(models.Movie.genre.ilike(f"%{genre}%"))
    if search:
        query = query.filter(models.Movie.title.ilike(f"%{search}%"))
    return query.all()

@app.get("/api/movies/{movie_id}", response_model=schemas.MovieResponse)
def get_movie_detail(movie_id: int, db: Session = Depends(get_db)):
    db_movie = db.query(models.Movie).filter(models.Movie.id == movie_id).first()
    if not db_movie:
        raise HTTPException(status_code=404, detail="Movie not found")
    return db_movie

@app.post("/api/movies", response_model=schemas.MovieResponse)
def create_movie(movie: schemas.MovieCreate, db: Session = Depends(get_db)):
    db_movie = models.Movie(**movie.dict())
    db.add(db_movie)
    db.commit()
    db.refresh(db_movie)
    return db_movie

@app.delete("/api/movies/{movie_id}")
def delete_movie(movie_id: int, db: Session = Depends(get_db)):
    db_movie = db.query(models.Movie).filter(models.Movie.id == movie_id).first()
    if not db_movie:
        raise HTTPException(status_code=404, detail="Movie not found")
    db.delete(db_movie)
    db.commit()
    return {"message": "Movie successfully deleted"}


# --- WATCHLIST ---
@app.get("/api/users/{user_id}/watchlist", response_model=List[schemas.MovieResponse])
def get_user_watchlist(user_id: str, db: Session = Depends(get_db)):
    watchlist_items = db.query(models.Watchlist).filter(models.Watchlist.user_id == user_id).all()
    movie_ids = [item.movie_id for item in watchlist_items]
    return db.query(models.Movie).filter(models.Movie.id.in_(movie_ids)).all() if movie_ids else []

@app.post("/api/users/{user_id}/watchlist")
def add_to_watchlist(user_id: str, data: schemas.WatchlistCreate, db: Session = Depends(get_db)):
    exists = db.query(models.Watchlist).filter(
        models.Watchlist.user_id == user_id, 
        models.Watchlist.movie_id == data.movie_id
    ).first()
    if exists:
        return {"message": "Already in watchlist"}
    
    item = models.Watchlist(user_id=user_id, movie_id=data.movie_id)
    db.add(item)
    db.commit()
    return {"message": "Successfully added to watchlist"}

@app.delete("/api/users/{user_id}/watchlist/{movie_id}")
def remove_from_watchlist(user_id: str, movie_id: int, db: Session = Depends(get_db)):
    item = db.query(models.Watchlist).filter(
        models.Watchlist.user_id == user_id, 
        models.Watchlist.movie_id == movie_id
    ).first()
    if not item:
        raise HTTPException(status_code=404, detail="Item not found in watchlist")
    db.delete(item)
    db.commit()
    return {"message": "Successfully removed from watchlist"}


# --- WATCH HISTORY & PROGRESS ---
@app.get("/api/users/{user_id}/history", response_model=List[schemas.MovieResponse])
def get_user_history(user_id: str, db: Session = Depends(get_db)):
    history_items = db.query(models.WatchHistory).filter(models.WatchHistory.user_id == user_id).all()
    movie_ids = [item.movie_id for item in history_items]
    return db.query(models.Movie).filter(models.Movie.id.in_(movie_ids)).all() if movie_ids else []

@app.post("/api/users/{user_id}/progress")
def save_watch_progress(user_id: str, data: schemas.WatchProgressSave, db: Session = Depends(get_db)):
    history = db.query(models.WatchHistory).filter(
        models.WatchHistory.user_id == user_id,
        models.WatchHistory.movie_id == data.movie_id
    ).first()
    
    if history:
        history.progress_ms = data.progress_ms
        history.duration_ms = data.duration_ms
        history.last_watched = models.datetime.datetime.utcnow()
    else:
        history = models.WatchHistory(
            user_id=user_id,
            movie_id=data.movie_id,
            progress_ms=data.progress_ms,
            duration_ms=data.duration_ms
        )
        db.add(history)
    db.commit()
    return {"message": "Progress successfully saved"}
