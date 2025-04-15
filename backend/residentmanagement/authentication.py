from django.conf import settings
from rest_framework import authentication, exceptions, status
import jwt

import logging
logger = logging.getLogger(__name__)

from . import models

class CustomUserAuthentication(authentication.BaseAuthentication):
    def authenticate(self, request):
        auth_header = request.headers.get("Authorization")
        if not auth_header:
            return None
        
        try:
            token = auth_header.split(' ')[1]
            payload = jwt.decode(token, settings.JWT_SECRET, algorithms=["HS256"])

            if payload["token_type"] != "access":
                logger.error("Invalid token type")
                raise exceptions.AuthenticationFailed('Invalid token type')
            
            user = models.User.objects.filter(id=payload["id"]).first()

            if user is None:                
                raise exceptions.AuthenticationFailed('User not found')

            return (user, None)
        except jwt.ExpiredSignatureError:
            # logger.error("Validating access token ...")
            raise exceptions.AuthenticationFailed('Token expired', code=status.HTTP_401_UNAUTHORIZED)
        except jwt.InvalidTokenError:            
            # logger.error("Invalid token")
            raise exceptions.AuthenticationFailed('Invalid token')
        except IndexError:            
            # logger.error("Bearer token not found")
            raise exceptions.AuthenticationFailed('Bearer token not found')