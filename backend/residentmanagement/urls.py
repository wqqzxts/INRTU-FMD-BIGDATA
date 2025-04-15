from django.urls import path

from . import apis

urlpatterns = [
    path("register/", apis.RegisterApi.as_view(), name="Registration"),
    path("login/", apis.LoginApi.as_view(), name="Login"),
    path("logout/", apis.LogoutApi.as_view(), name="Logout"),
    path("profile/", apis.UserApi.as_view(), name="Profile"),    
    path("profile/edit/", apis.UserApi.as_view(), name="Edit profile"),
    path("refresh/", apis.RefreshToken.as_view(), name="Refresh access token"),
]