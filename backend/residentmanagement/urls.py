from django.urls import path

from . import apis

urlpatterns = [
    path("auth/register/", apis.RegisterApi.as_view(), name="Registration"),
    path("auth/login/", apis.LoginApi.as_view(), name="Login"),
    path("auth/logout/", apis.LogoutApi.as_view(), name="Logout"),
    path("auth/token/refresh/", apis.RefreshToken.as_view(), name="Refresh access token"),
    path("auth/token/validate/", apis.ValidateTokenApi.as_view(), name="Validate access token"),
    path("profile/", apis.UserApi.as_view(), name="Profile"),    
    path("profile/edit/", apis.UserApi.as_view(), name="Edit profile"),    
]