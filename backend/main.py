"""
MatMovies Backend Service
Built with FastAPI, SQLAlchemy, and PostgreSQL.
Provides robust REST APIs for movies, series, season/episode management,
watchlist synchronizations, and watch history progress tracking.
"""

import os
from typing import List, Optional
from datetime import datetime
from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from sqlalchemy import create_engine, Column, Integer, String, Boolean, Float, DateTime, ForeignKey
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, Session, relationship

# 1. Database Configuration
DATABASE_URL = os.getenv("DATABASE_URL", "postgresql://postgres:postgres@localhost:5432/matmovies")

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

# 2. SQLAlchemy ORM Models
class UserDB(Base):
    __tablename__ = "users"
    
    id = Column(String, primary_key=True, index=True)
    username = Column(String, unique=True, index=True, nullable=False)
    email = Column(String, unique=True, index=True, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow)
    
    watchlist = relationship("WatchlistDB", back_populates="user", cascade="all, delete-orphan")
    history = relationship("WatchHistoryDB", back_populates="user", cascade="all, delete-orphan")


class MovieDB(Base):
    __tablename__ = "movies"
    
    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    title = Column(String, index=True, nullable=False)
    synopsis = Column(String, nullable=True)
    genre = Column(String, nullable=False)
    rating = Column(Float, default=0.0)
    release_year = Column(Integer, nullable=False)
    video_url = Column(String, nullable=False)
    poster_drawable_name = Column(String, nullable=False)
    is_featured = Column(Boolean, default=False)
    is_series = Column(Boolean, default=False)
    seasons_count = Column(Integer, default=0)
    
    seasons = relationship("SeasonDB", back_populates="movie", cascade="all, delete-orphan")


class SeasonDB(Base):
    __tablename__ = "seasons"
    
    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    movie_id = Column(Integer, ForeignKey("movies.id", ondelete="CASCADE"), nullable=False)
    season_number = Column(Integer, nullable=False)
    
    movie = relationship("MovieDB", back_populates="seasons")
    episodes = relationship("EpisodeDB", back_populates="season", cascade="all, delete-orphan")


class EpisodeDB(Base):
    __tablename__ = "episodes"
    
    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    season_id = Column(Integer, ForeignKey("seasons.id", ondelete="CASCADE"), nullable=False)
    episode_number = Column(Integer, nullable=False)
    title = Column(String, nullable=False)
    duration_mins = Column(Integer, default=45)
    overview = Column(String, nullable=True)
    
    season = relationship("SeasonDB", back_populates="episodes")


class WatchlistDB(Base):
    __tablename__ = "watchlist"
    
    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    user_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    movie_id = Column(Integer, ForeignKey("movies.id", ondelete="CASCADE"), nullable=False)
    added_at = Column(DateTime, default=datetime.utcnow)
    
    user = relationship("UserDB", back_populates="watchlist")


class WatchHistoryDB(Base):
    __tablename__ = "watch_history"
    
    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    user_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    movie_id = Column(Integer, ForeignKey("movies.id", ondelete="CASCADE"), nullable=False)
    progress_ms = Column(Integer, default=0)
    duration_ms = Column(Integer, default=0)
    last_watched = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    user = relationship("UserDB", back_populates="history")


# Create Tables
Base.metadata.create_all(bind=engine)


# 3. Pydantic Schemas for Requests and Responses
class EpisodeSchema(BaseModel):
    id: int
    episode_number: int
    title: str
    duration_mins: int
    overview: Optional[str] = None

    class Config:
        from_attributes = True


class SeasonSchema(BaseModel):
    id: int
    season_number: int
    episodes: List[EpisodeSchema] = []

    class Config:
        from_attributes = True


class MovieSchema(BaseModel):
    id: int
    title: str
    synopsis: Optional[str] = None
    genre: str
    rating: float
    release_year: int
    video_url: str
    poster_drawable_name: str
    is_featured: bool
    is_series: bool
    seasons_count: int

    class Config:
        from_attributes = True


class MovieDetailSchema(MovieSchema):
    seasons: List[SeasonSchema] = []

    class Config:
        from_attributes = True


class UserSchema(BaseModel):
    id: str
    username: str
    email: str

    class Config:
        from_attributes = True


class WatchlistRequest(BaseModel):
    user_id: str
    movie_id: int


class WatchHistoryRequest(BaseModel):
    user_id: str
    movie_id: int
    progress_ms: int
    duration_ms: int


# 4. FastAPI Setup
app = FastAPI(
    title="MatMovies API",
    description="Backend service powered by FastAPI, SQLAlchemy, and PostgreSQL for MatMovies.",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# Dependency to get db session
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


# 5. REST Route Handlers
@app.get("/")
def read_root():
    return {"message": "Welcome to MatMovies API Service!"}


# --- USER ENDPOINTS ---
@app.post("/api/users", response_model=UserSchema, status_code=status.HTTP_201_CREATED)
def create_user(user: UserSchema, db: Session = Depends(get_db)):
    db_user = db.query(UserDB).filter(UserDB.id == user.id).first()
    if db_user:
        return db_user
    new_user = UserDB(id=user.id, username=user.username, email=user.email)
    db.add(new_user)
    db.commit()
    db.refresh(new_user)
    return new_user


# --- MOVIE & SERIES ENDPOINTS ---
@app.get("/api/movies", response_model=List[MovieSchema])
def get_movies(genre: Optional[str] = None, db: Session = Depends(get_db)):
    query = db.query(MovieDB)
    if genre:
        query = query.filter(MovieDB.genre.ilike(f"%{genre}%"))
    return query.all()


@app.get("/api/movies/{movie_id}", response_model=MovieDetailSchema)
def get_movie_detail(movie_id: int, db: Session = Depends(get_db)):
    movie = db.query(MovieDB).filter(MovieDB.id == movie_id).first()
    if not movie:
        raise HTTPException(status_code=404, detail="Movie or Series not found")
    return movie


# --- WATCHLIST ENDPOINTS ---
@app.post("/api/watchlist", status_code=status.HTTP_201_CREATED)
def add_to_watchlist(req: WatchlistRequest, db: Session = Depends(get_db)):
    existing = db.query(WatchlistDB).filter(
        WatchlistDB.user_id == req.user_id,
        WatchlistDB.movie_id == req.movie_id
    ).first()
    if existing:
        return {"message": "Already in watchlist"}
    
    item = WatchlistDB(user_id=req.user_id, movie_id=req.movie_id)
    db.add(item)
    db.commit()
    return {"message": "Successfully added to watchlist"}


@app.delete("/api/watchlist")
def remove_from_watchlist(user_id: str, movie_id: int, db: Session = Depends(get_db)):
    item = db.query(WatchlistDB).filter(
        WatchlistDB.user_id == user_id,
        WatchlistDB.movie_id == movie_id
    ).first()
    if not item:
        raise HTTPException(status_code=404, detail="Watchlist item not found")
    db.delete(item)
    db.commit()
    return {"message": "Successfully removed from watchlist"}


@app.get("/api/watchlist/{user_id}", response_model=List[MovieSchema])
def get_user_watchlist(user_id: str, db: Session = Depends(get_db)):
    items = db.query(WatchlistDB).filter(WatchlistDB.user_id == user_id).all()
    movie_ids = [item.movie_id for item in items]
    return db.query(MovieDB).filter(MovieDB.id.in_(movie_ids)).all() if movie_ids else []


# --- WATCH HISTORY & PROGRESS ENDPOINTS ---
@app.post("/api/history", status_code=status.HTTP_200_OK)
def update_watch_history(req: WatchHistoryRequest, db: Session = Depends(get_db)):
    record = db.query(WatchHistoryDB).filter(
        WatchHistoryDB.user_id == req.user_id,
        WatchHistoryDB.movie_id == req.movie_id
    ).first()
    
    if record:
        record.progress_ms = req.progress_ms
        record.duration_ms = req.duration_ms
        record.last_watched = datetime.utcnow()
    else:
        record = WatchHistoryDB(
            user_id=req.user_id,
            movie_id=req.movie_id,
            progress_ms=req.progress_ms,
            duration_ms=req.duration_ms
        )
        db.add(record)
        
    db.commit()
    return {"message": "Playback progress saved successfully"}


@app.get("/api/history/{user_id}")
def get_user_history(user_id: str, db: Session = Depends(get_db)):
    records = db.query(WatchHistoryDB).filter(WatchHistoryDB.user_id == user_id).order_by(WatchHistoryDB.last_watched.desc()).all()
    result = []
    for r in records:
        movie = db.query(MovieDB).filter(MovieDB.id == r.movie_id).first()
        if movie:
            result.append({
                "movie": MovieSchema.from_orm(movie),
                "progress_ms": r.progress_ms,
                "duration_ms": r.duration_ms,
                "last_watched": r.last_watched
            })
    return result


# --- DB SEED ROUTE (To populate default movies, series and episodes) ---
@app.post("/api/db/seed", status_code=status.HTTP_201_CREATED)
def seed_database(db: Session = Depends(get_db)):
    # Clear existing content if any to avoid duplication
    db.query(EpisodeDB).delete()
    db.query(SeasonDB).delete()
    db.query(MovieDB).delete()
    db.commit()

    # Seed Movies and Series
    movies_data = [
        # TV Series
        {
            "title": "Bachelor Point - Season 4",
            "synopsis": "The beloved comedy drama focusing on Bachelor lifestyles in Dhaka with Pasha, Kabila, Habu, and friends.",
            "genre": "bangla natok, Family Comedy, Trending",
            "rating": 4.9,
            "release_year": 2022,
            "video_url": "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            "poster_drawable_name": "img_natok_banner",
            "is_featured": True,
            "is_series": True,
            "seasons_count": 4
        },
        {
            "title": "Mirzapur - Season 3",
            "synopsis": "The throne of Mirzapur lies vacant. Guddu Pandit rules with an iron fist as alliances crumble and blood spills.",
            "genre": "Top web Series, Crime Drama, Action",
            "rating": 4.8,
            "release_year": 2024,
            "video_url": "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            "poster_drawable_name": "img_action_banner",
            "is_featured": False,
            "is_series": True,
            "seasons_count": 3
        },
        # Blockbuster Movies
        {
            "title": "Avatar: The Way of Water",
            "synopsis": "Jake Sully lives with his newfound family formed on the extrasolar moon Pandora.",
            "genre": "🔥Cinema, Hollywood, Sci-Fi",
            "rating": 4.7,
            "release_year": 2022,
            "video_url": "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            "poster_drawable_name": "img_hero_dune",
            "is_featured": False,
            "is_series": False,
            "seasons_count": 0
        },
        {
            "title": "KGF: Chapter 2",
            "synopsis": "In the blood-soaked Kolar Gold Fields, Rocky's name strikes fear into his enemies.",
            "genre": "Most trending, South Indian, Action",
            "rating": 4.9,
            "release_year": 2022,
            "video_url": "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            "poster_drawable_name": "img_poster_cyberpunk",
            "is_featured": False,
            "is_series": False,
            "seasons_count": 0
        }
    ]

    for m in movies_data:
        movie = MovieDB(**m)
        db.add(movie)
        db.commit()
        db.refresh(movie)

        # If it is a TV series, auto-populate seasons and episodes
        if movie.is_series:
            for s_num in range(1, movie.seasons_count + 1):
                season = SeasonDB(movie_id=movie.id, season_number=s_num)
                db.add(season)
                db.commit()
                db.refresh(season)

                # Add episodes
                ep_count = 12 if movie.title == "Bachelor Point - Season 4" else 8
                for ep_num in range(1, ep_count + 1):
                    episode = EpisodeDB(
                        season_id=season.id,
                        episode_number=ep_num,
                        title=f"The Story Unfolds: Part {ep_num}",
                        duration_mins=45,
                        overview=f"Episode {ep_num} of Season {s_num} for {movie.title} with breathtaking plots."
                    )
                    db.add(episode)
            db.commit()

    return {"message": "PostgreSQL Database populated successfully with default seed data!"}
