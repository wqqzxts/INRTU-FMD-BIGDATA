from django.urls import path
from . import apis

urlpatterns = [
    path("publication/", apis.PublicationListCreateApi.as_view(), name="Публикация"),
    path("publication/<int:publication_id>/", apis.PublicationSpecificRetrieveUpdateDelete.as_view(), name="Конкретная публикация")
]