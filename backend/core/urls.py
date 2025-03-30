from django.contrib import admin
from django.urls import path, include

urlpatterns = [
    path('admin/', admin.site.urls),
    path("api/", include("residentmanagement.urls")),
    path("api/", include("publications.urls")),
]
