from django.urls import path

from . import apis

urlpatterns = [
    path("register/", apis.RegisterApi.as_view(), name="Registration"),
    path("login/", apis.LoginApi.as_view(), name="Login"),
    path("profile/", apis.UserApi.as_view(), name="Profile"),
    path("logout/", apis.LogoutApi.as_view(), name="Logout"),
    path("refresh/", apis.RefreshToken.as_view(), name="Refresh access token"),
]