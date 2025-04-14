from django.urls import path
from . import apis

urlpatterns = [
    path("publications/", apis.PublicationListCreateApi.as_view(), name="Publications"),
    path("publications/<int:publication_id>/", apis.PublicationSpecificRetrieveUpdateDelete.as_view(), name="Specific publication")
]