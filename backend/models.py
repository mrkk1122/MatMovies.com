from sqlalchemy import Column, Integer, String, Float, Boolean, ForeignKey, DateTime
from sqlalchemy.orm import relationship
from database import Base
import datetime

class User(Base):
    __tablename__ = "users"

    id = Column(String, primary_key=True, index=True)
    name = Column(String, nullable=False)
    email = Column(String, unique=True, index=True, nullable=False)
    password = Column(String, nullable=False)
    subscription_status = Column(String, default="Free") # Free, Premium, VIP

class Category(Base):
    __tablename__ = "categories"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    name = Column(String, unique=True, nullable=False)
    description = Column(String, nullable=True)
    icon_name = Column(String, nullable=False)

    movies = relationship("Movie", back_populates="category", cascade="all, delete-orphan")

class Movie(Base):
    __tablename__ = "movies"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    title = Column(String, nullable=False, index=True)
    description = Column(String, nullable=False)
    year = Column(Integer, nullable=False)
    duration = Column(String, nullable=False)
    rating = Column(Float, nullable=False)
    genre = Column(String, nullable=False)
    video_url = Column(String, nullable=False)
    poster_drawable_name = Column(String, nullable=False)
    is_series = Column(Boolean, default=False)
    seasons_count = Column(Integer, default=0)
    category_id = Column(Integer, ForeignKey("categories.id"), nullable=True)

    category = relationship("Category", back_populates="movies")

class Watchlist(Base):
    __tablename__ = "watchlists"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    user_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    movie_id = Column(Integer, ForeignKey("movies.id", ondelete="CASCADE"), nullable=False)

class WatchHistory(Base):
    __tablename__ = "watch_histories"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    user_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    movie_id = Column(Integer, ForeignKey("movies.id", ondelete="CASCADE"), nullable=False)
    progress_ms = Column(Integer, default=0)
    duration_ms = Column(Integer, default=0)
    last_watched = Column(DateTime, default=datetime.datetime.utcnow)
