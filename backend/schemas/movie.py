from pydantic import BaseModel, Field
from typing import Optional, List
from uuid import UUID

class MovieBase(BaseModel):
    title: str = Field(..., min_length=1, max_length=255, description="Movie or TV Show Title", examples=["Galactic Frontiers"])
    description: str = Field(..., min_length=1, description="Synopsis or description of the movie", examples=["An interstellar exploration ship gets dragged into a supermassive black hole, discovering a hidden dimension."])
    rating: float = Field(4.5, ge=0.0, le=5.0, description="Average viewer rating from 0.0 to 5.0", examples=[4.8])
    year: int = Field(..., ge=1888, le=2100, description="Release year of the content", examples=[2026])
    duration: str = Field(..., description="Duration of the movie (e.g., '2h 15m')", examples=["2h 15m"])
    genre: str = Field(..., description="Comma-separated genre names", examples=["Sci-Fi, Adventure"])
    poster_drawable_name: str = Field("img_poster_cyberpunk", alias="posterDrawableName", description="Resource name of the poster image asset", examples=["img_poster_cyberpunk"])
    video_url: str = Field(..., alias="videoUrl", description="Direct video stream HTTP URL link (MP4 / HLS)", examples=["https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"])
    language: str = Field("English", description="Primary spoken language of the content", examples=["English"])
    subtitles_url: str = Field("", alias="subtitlesUrl", description="URL/Type description of subtitle tracks available", examples=["English (SRT)"])
    is_featured: bool = Field(False, alias="isFeatured", description="Whether movie is promoted on the main home carousel", examples=[True])
    is_trending: bool = Field(False, alias="isTrending", description="Whether movie is popular and added to trending list", examples=[False])
    is_latest: bool = Field(False, alias="isLatest", description="Whether movie is in the latest releases list", examples=[True])

    class Config:
        populate_by_name = True
        allow_population_by_field_name = True
        json_schema_extra = {
            "example": {
                "title": "Galactic Frontiers",
                "description": "An interstellar exploration ship gets dragged into a supermassive black hole, discovering a hidden dimension.",
                "rating": 4.8,
                "year": 2026,
                "duration": "2h 15m",
                "genre": "Sci-Fi, Adventure",
                "posterDrawableName": "img_poster_cyberpunk",
                "videoUrl": "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                "language": "English",
                "subtitlesUrl": "English (SRT)",
                "isFeatured": True,
                "isTrending": False,
                "isLatest": True
            }
        }

class MovieCreate(MovieBase):
    """Schema for creating a new movie record."""
    pass

class MovieOut(MovieBase):
    """Schema for returning movie details."""
    id: int = Field(..., description="Unique auto-increment database ID of the movie", examples=[42])
    views_count: int = Field(0, alias="viewsCount", description="Total streaming views/plays count", examples=[12400])
    is_uploaded_by_user: bool = Field(False, alias="isUploadedByUser", description="Whether movie was uploaded by a custom user/admin action", examples=[False])

    class Config:
        populate_by_name = True
        allow_population_by_field_name = True
        json_schema_extra = {
            "example": {
                "id": 42,
                "title": "Dune Chronicles",
                "description": "In the far future, a noble family gets involved in a war for control over the galaxy's most valuable asset on a desert planet.",
                "rating": 4.9,
                "year": 2024,
                "duration": "2h 35m",
                "genre": "Sci-Fi, Fantasy",
                "posterDrawableName": "img_hero_dune",
                "videoUrl": "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                "language": "English",
                "subtitlesUrl": "English (SRT)",
                "isFeatured": True,
                "isTrending": True,
                "isLatest": False,
                "viewsCount": 12400,
                "isUploadedByUser": False
            }
        }

class CategoryBase(BaseModel):
    name: str = Field(..., min_length=1, max_length=100, description="Name of the category or genre", examples=["Documentaries"])
    description: str = Field(..., description="Brief description of the movie genre", examples=["True stories, nature profiles, and historical investigations"])
    icon_name: str = Field(..., alias="iconName", description="Material design icon vector identifier string", examples=["public"])

    class Config:
        populate_by_name = True
        allow_population_by_field_name = True
        json_schema_extra = {
            "example": {
                "name": "Documentaries",
                "description": "True stories, nature profiles, and historical investigations",
                "iconName": "public"
            }
        }

class CategoryCreate(CategoryBase):
    """Schema for creating a new category option."""
    pass

class CategoryOut(CategoryBase):
    """Schema for returning category details."""
    id: int = Field(..., description="Unique auto-increment category identifier ID", examples=[5])

    class Config:
        populate_by_name = True
        allow_population_by_field_name = True
        json_schema_extra = {
            "example": {
                "id": 5,
                "name": "Action",
                "description": "Thrilling fights and car chases",
                "iconName": "local_fire_department"
            }
        }

class WatchHistoryUpdate(BaseModel):
    """Schema for updating current watch progress on a video."""
    movie_id: int = Field(..., alias="movieId", description="Unique ID of the movie being watched", examples=[42])
    progress_ms: int = Field(..., alias="progressMs", ge=0, description="Current watching offset/progress in milliseconds", examples=[2400000])
    duration_ms: int = Field(..., alias="durationMs", ge=0, description="Total movie duration in milliseconds", examples=[7200000])

    class Config:
        populate_by_name = True
        allow_population_by_field_name = True
        json_schema_extra = {
            "example": {
                "movieId": 42,
                "progressMs": 2400000,
                "durationMs": 7200000
            }
        }

class WatchHistoryOut(BaseModel):
    """Schema for returning a watch history entry with movie info."""
    id: int = Field(..., description="Unique watch log database entry ID", examples=[120])
    user_id: UUID = Field(..., alias="userId", description="Unique identifier UUID of the user", examples=["7b47b85e-dc81-4203-b565-d0c39fdf7f33"])
    movie: MovieOut = Field(..., description="The full details of the movie associated with this watch log")
    progress_ms: int = Field(..., alias="progressMs", ge=0, description="Saved playback progress in milliseconds", examples=[2400000])
    duration_ms: int = Field(..., alias="durationMs", ge=0, description="Total movie duration in milliseconds", examples=[7200000])
    last_watched_timestamp: int = Field(..., alias="lastWatchedTimestamp", description="Epoch timestamp in milliseconds of the last active playback", examples=[1782494405000])

    class Config:
        populate_by_name = True
        allow_population_by_field_name = True
        json_schema_extra = {
            "example": {
                "id": 120,
                "userId": "7b47b85e-dc81-4203-b565-d0c39fdf7f33",
                "movie": {
                    "id": 42,
                    "title": "Dune Chronicles",
                    "description": "In the far future, a noble family gets involved in a war for control over the galaxy's most valuable asset on a desert planet.",
                    "rating": 4.9,
                    "year": 2024,
                    "duration": "2h 35m",
                    "genre": "Sci-Fi, Fantasy",
                    "posterDrawableName": "img_hero_dune",
                    "videoUrl": "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    "language": "English",
                    "subtitlesUrl": "English (SRT)",
                    "isFeatured": True,
                    "isTrending": True,
                    "isLatest": False,
                    "viewsCount": 12400,
                    "isUploadedByUser": False
                },
                "progressMs": 2400000,
                "durationMs": 7200000,
                "lastWatchedTimestamp": 1782494405000
            }
        }

class ReviewCreate(BaseModel):
    """Schema for submitting a movie comment/review with a rating."""
    rating: int = Field(..., ge=1, le=5, description="Star rating left by user (from 1 to 5 stars)", examples=[5])
    comment: str = Field(..., min_length=2, description="Text comment or detailed movie review content", examples=["This sci-fi movie is absolutely sensational! The acting and soundtrack are superb."])

    class Config:
        json_schema_extra = {
            "example": {
                "rating": 5,
                "comment": "This sci-fi movie is absolutely sensational! The acting and soundtrack are superb."
            }
        }

class ReviewOut(ReviewCreate):
    """Schema for returning detailed published review info."""
    id: int = Field(..., description="Unique published review entry ID", examples=[8])
    user_id: UUID = Field(..., alias="userId", description="ID of the user who left the review", examples=["7b47b85e-dc81-4203-b565-d0c39fdf7f33"])
    username: str = Field(..., description="Display name of the authoring user", examples=["Cinephile99"])
    movie_id: int = Field(..., alias="movieId", description="ID of the reviewed movie", examples=[42])
    timestamp: int = Field(..., description="Epoch timestamp in milliseconds of publication", examples=[1782494405615])

    class Config:
        populate_by_name = True
        allow_population_by_field_name = True
        json_schema_extra = {
            "example": {
                "id": 8,
                "userId": "7b47b85e-dc81-4203-b565-d0c39fdf7f33",
                "username": "Cinephile99",
                "movieId": 42,
                "rating": 5,
                "comment": "This sci-fi movie is absolutely sensational! The acting and soundtrack are superb.",
                "timestamp": 1782494405615
            }
        }
