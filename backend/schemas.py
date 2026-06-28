from pydantic import BaseModel, EmailStr
from typing import List, Optional
import datetime

# User Schemas
class UserBase(BaseModel):
    name: str
    email: EmailStr

class UserCreate(UserBase):
    id: str
    password: str

class UserLogin(BaseModel):
    email: EmailStr
    password: str

class UserResponse(UserBase):
    id: str
    subscription_status: str

    class Config:
        from_attributes = True

# Movie Schemas
class MovieBase(BaseModel):
    title: str
    description: str
    year: int
    duration: str
    rating: float
    genre: str
    video_url: str
    poster_drawable_name: str
    is_series: bool
    seasons_count: int

class MovieCreate(MovieBase):
    category_id: Optional[int] = None

class MovieResponse(MovieBase):
    id: int
    category_id: Optional[int] = None

    class Config:
        from_attributes = True

# Category Schemas
class CategoryBase(BaseModel):
    name: str
    description: Optional[str] = None
    icon_name: str

class CategoryResponse(CategoryBase):
    id: int
    movies: List[MovieResponse] = []

    class Config:
        from_attributes = True

# Watchlist Schemas
class WatchlistCreate(BaseModel):
    movie_id: int

# Watch History Schemas
class WatchProgressSave(BaseModel):
    movie_id: int
    progress_ms: int
    duration_ms: int
