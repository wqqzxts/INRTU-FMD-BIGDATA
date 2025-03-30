from django.urls import path
from . import apis

urlpatterns = [
    path("publications/", apis.PublicationListCreateApi.as_view(), name="Публикация"),
    path("publications/<int:publication_id>/", apis.PublicationSpecificRetrieveUpdateDelete.as_view(), name="Конкретная публикация")
]