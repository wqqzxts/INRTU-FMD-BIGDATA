from rest_framework import views, response, exceptions, permissions, status
from django.conf import settings
import jwt

from .serializers import UserSerializer
from . import services, authentication, models

import logging
logger = logging.getLogger(__name__)

class RegisterApi(views.APIView):
    def post(self, request):
        serializer = UserSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)

        data = serializer.validated_data
        serializer.instance = services.create_user(user_dc=data)

        return response.Response(status=status.HTTP_201_CREATED)
    

class LoginApi(views.APIView):
    def post(self, request):
        email = request.data["email"]
        password = request.data["password"]

        user = services.user_email_selector(email=email)

        if user is None:
            raise exceptions.AuthenticationFailed("Invalid credentials")
        
        if not user.check_password(raw_password=password):
            raise exceptions.AuthenticationFailed("Invalid credentials")
                   
        tokens = services.create_tokens(user_id=user.id)

        resp = response.Response(status=status.HTTP_200_OK)

        resp.data = {
            "user": {"is_staff": user.is_staff},
            "access": tokens["access"]
        }

        resp['Authorization'] = f"Bearer {tokens['access']}"

        resp.set_cookie(
            key="refresh",
            value=tokens["refresh"],
            httponly=True,
            max_age=604800
        )

        return resp
    

class RefreshToken(views.APIView):
    authentication_classes = ()
    permission_classes = (permissions.AllowAny, )

    def post(self, request):
        refresh_token = request.COOKIES.get("refresh")

        if not refresh_token:
            auth_header = request.headers.get("Authorization", "")
            if auth_header.startswith("Bearer "):
                refresh_token = auth_header.split(" ")[1]        

        if not refresh_token:            
            raise exceptions.AuthenticationFailed('Token not found')
        
        try:
            payload = jwt.decode(refresh_token, settings.JWT_SECRET, algorithms=["HS256"])
            if payload["token_type"] != "refresh":                
                raise exceptions.AuthenticationFailed('Invalid token type')
            
            user = models.User.objects.filter(id=payload["id"]).first()
            if user is None:
                raise exceptions.AuthenticationFailed("User not found")
            
            accessToken = services.refresh_access_token(refresh_token)

            return response.Response({"access": accessToken})
        except jwt.ExpiredSignatureError:    
            raise exceptions.AuthenticationFailed('Token expired')
        except jwt.InvalidTokenError:    
            raise exceptions.AuthenticationFailed('Invalid token type')


class LogoutApi(views.APIView):
    authentication_classes = (authentication.CustomUserAuthentication, )
    permission_classes = (permissions.IsAuthenticated, )

    def post(self, request):
        resp = response.Response(status=status.HTTP_204_NO_CONTENT)
        resp.delete_cookie("refresh")        

        return resp
    

class UserApi(views.APIView):
    authentication_classes = (authentication.CustomUserAuthentication, )
    permission_classes = (permissions.IsAuthenticated, )

    def get(self, request):
        user = request.user
        serializer = UserSerializer(user)

        return response.Response(serializer.data)
    
    def patch(self, request):        
        serializer = UserSerializer(
            data=request.data,
            partial=True)
        serializer.is_valid(raise_exception=True)
        user = serializer.validated_data
        services.update_user(request.user, user_data=user)        

        return response.Response(status=status.HTTP_200_OK)
    

class ValidateTokenApi(views.APIView):
    authentication_classes = (authentication.CustomUserAuthentication, )
    permission_classes = (permissions.IsAuthenticated, )

    def get(self, request):
        return response.Response(status=status.HTTP_200_OK)