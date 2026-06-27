from pydantic import BaseModel, EmailStr, Field
from typing import Optional
from uuid import UUID
from enum import Enum

class UserRole(str, Enum):
    USER = "User"
    ADMIN = "Admin"

class SubscriptionTier(str, Enum):
    FREE = "Free"
    PREMIUM = "Premium"
    VIP = "VIP"

class UserBase(BaseModel):
    email: EmailStr = Field(..., description="Unique email address of the user", examples=["user@moviesbox.com"])
    username: str = Field(..., min_length=3, max_length=50, description="Display username", examples=["Cinephile99"])

class UserRegister(UserBase):
    password: str = Field(..., min_length=6, max_length=128, description="Password (hashed automatically on server-side)", examples=["supersecretpwd"])

    class Config:
        json_schema_extra = {
            "example": {
                "email": "user@moviesbox.com",
                "username": "Cinephile99",
                "password": "supersecretpwd"
            }
        }

class UserLogin(BaseModel):
    username: EmailStr = Field(..., description="Email address used as username in OAuth2 Password Flow login", examples=["user@moviesbox.com"])
    password: str = Field(..., description="Plain-text password input", examples=["supersecretpwd"])

    class Config:
        json_schema_extra = {
            "example": {
                "username": "user@moviesbox.com",
                "password": "supersecretpwd"
            }
        }

class TokenResponse(BaseModel):
    access_token: str = Field(..., description="JWT Bearer access token string", examples=["eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOi..."])
    token_type: str = Field("bearer", description="Standard token header scheme type", examples=["bearer"])
    user: 'UserOut' = Field(..., description="The user profile associated with this authentication session")

    class Config:
        json_schema_extra = {
            "example": {
                "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOi...",
                "token_type": "bearer",
                "user": {
                    "id": "7b47b85e-dc81-4203-b565-d0c39fdf7f33",
                    "email": "user@moviesbox.com",
                    "username": "Cinephile99",
                    "role": "User",
                    "subscriptionStatus": "Free",
                    "createdAt": 1782494394000
                }
            }
        }

class UserOut(UserBase):
    id: UUID = Field(..., description="Unique system-generated UUID representing the user profile", examples=["7b47b85e-dc81-4203-b565-d0c39fdf7f33"])
    role: UserRole = Field(UserRole.USER, description="System access role which dictates control panel permissions", examples=[UserRole.USER])
    subscription_status: SubscriptionTier = Field(SubscriptionTier.FREE, alias="subscriptionStatus", description="Active billing plan tier", examples=[SubscriptionTier.FREE])
    created_at: int = Field(..., alias="createdAt", description="Epoch timestamp in milliseconds of user creation", examples=[1782494394000])

    class Config:
        populate_by_name = True
        allow_population_by_field_name = True
        # Exclude password fields completely from JSON rendering and parsing in output models
        json_encoders = {
            UUID: lambda v: str(v)
        }
        json_schema_extra = {
            "example": {
                "id": "7b47b85e-dc81-4203-b565-d0c39fdf7f33",
                "email": "user@moviesbox.com",
                "username": "Cinephile99",
                "role": "User",
                "subscriptionStatus": "Free",
                "createdAt": 1782494394000
            }
        }

class SubscriptionUpdate(BaseModel):
    tier: SubscriptionTier = Field(..., description="The upgraded/new subscription tier plan", examples=[SubscriptionTier.PREMIUM])

    class Config:
        json_schema_extra = {
            "example": {
                "tier": "Premium"
            }
        }

class RoleUpdate(BaseModel):
    role: UserRole = Field(..., description="The system role to promote/demote user into", examples=[UserRole.ADMIN])

    class Config:
        json_schema_extra = {
            "example": {
                "role": "Admin"
            }
        }

# Resolve circular reference for type annotation in Pydantic v1/v2 compatibility
TokenResponse.update_forward_refs()
