from django.urls import path

from . import apis

urlpatterns = [
    path("register/", apis.RegisterApi.as_view(), name="Регистрация"),
    path("login/", apis.LoginApi.as_view(), name="Вход"),
    path("me/", apis.UserApi.as_view(), name="Я"),
    path("logout/", apis.LogoutApi.as_view(), name="Выход"),
]