import dataclasses
import datetime
import jwt
from django.conf import settings
from typing import TYPE_CHECKING

from . import models

if TYPE_CHECKING:
    from .models import User

@dataclasses.dataclass
class UserDataClass:
    first_name: str
    last_name: str
    email: str
    gender: str
    apartments: int
    password: str = None
    id: int = None

    
    @classmethod
    def from_instance(cls, user: "User") -> "UserDataClass":
        return cls(
            first_name=user.first_name,
            last_name=user.last_name,
            email=user.email,
            gender=user.gender,
            apartments=user.apartments,
            id=user.id,
        )
    

def createuser(user_dc: "UserDataClass") -> "UserDataClass":
    instance = models.User(
        first_name=user_dc.first_name,
            last_name=user_dc.last_name,
            email=user_dc.email,
            gender=user_dc.gender,
            apartments=user_dc.apartments,
            id=user_dc.id,
    )
    if user_dc.password is not None:
        instance.set_password(user_dc.password)

    instance.save()

    return UserDataClass.from_instance(instance)


def user_email_selector(email: str) -> "User":
    user = models.User.objects.filter(email=email).first()

    return user


def create_tokens(user_id: int) -> str:
    access_payload = dict(
        id=user_id,
        exp=datetime.datetime.utcnow() + datetime.timedelta(minutes=15),
        iat=datetime.datetime.utcnow(),
        token_type="access"
    )
    access = jwt.encode(access_payload, settings.JWT_SECRET, algorithm="HS256")

    refresh_payload = dict(
        id=user_id,
        exp=datetime.datetime.utcnow() + datetime.timedelta(days=30),    
        iat=datetime.datetime.utcnow(),
        token_type="refresh"
    )
    refresh = jwt.encode(refresh_payload, settings.JWT_SECRET, algorithm="HS256")

    return {
        "access": access,
        "refresh": refresh
    }


def refresh_access_token(refresh_token: str) -> str:
    try:
        payload = jwt.decode(refresh_token, settings.JWT_SECRET, algorithms=["HS256"])
        if payload["token_type"] != "refresh":
            raise jwt.InvalidTokenError('Token not found')
        
        access_payload = dict(
            id=payload["id"],
            exp=datetime.datetime.utcnow() + datetime.timedelta(minutes=15),
            iat=datetime.datetime.utcnow(),
            token_type="access"
        )
        return jwt.encode(access_payload, settings.JWT_SECRET, algorithm="HS256")
    except jwt.ExpiredSignatureError:
        raise Exception('Token expired')
    except jwt.InvalidTokenError:
        raise Exception('Invalid token')