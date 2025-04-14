import dataclasses
import datetime
from typing import TYPE_CHECKING
from django.shortcuts import get_object_or_404
from rest_framework import exceptions

from residentmanagement import services as residentmanagement_services
from . import models as publication_models

if TYPE_CHECKING:
    from models import Publication
    from residentmanagement.models import User
    

@dataclasses.dataclass
class PublicationDataClass:
    title: str
    content: str
    date_published: datetime.datetime = None
    user: residentmanagement_services.UserDataClass = None
    id: int = None


    @classmethod
    def from_instance(cls, publication_model: "Publication") -> "PublicationDataClass":
        return cls(
            title=publication_model.title,
            content=publication_model.content,
            date_published=publication_model.date_published,
            id=publication_model.id,
            user=publication_model.user
        )
    

def create_publication(user, publication: "PublicationDataClass") -> "PublicationDataClass":
    publication_create = publication_models.Publication.objects.create(
        title=publication.title,
        content=publication.content,
        user=user
    )

    return PublicationDataClass.from_instance(publication_model=publication_create)


def get_all_publications() -> list["PublicationDataClass"]:
    publications = publication_models.Publication.objects.all()

    return [PublicationDataClass.from_instance(publication) for publication in publications]



def get_user_specific_publication(publication_id: int) -> "PublicationDataClass":
    publication = get_object_or_404(publication_models.Publication, pk=publication_id)

    return PublicationDataClass.from_instance(publication_model=publication)


def delete_user_post(user: "User", publication_id: int) -> None:
    publication = get_object_or_404(publication_models.Publication, pk=publication_id)

    if user.id != publication.user.id and not user.is_staff:
        raise exceptions.PermissionDenied("Do not have permissions")

    publication.delete()    


def update_user_publication(user: "User", publication_id: int, publication_data: "PublicationDataClass"):
    publication = get_object_or_404(publication_models.Publication, pk=publication_id)

    if user.id != publication.user.id and not user.is_staff:
        raise exceptions.PermissionDenied("Do not have permissions")
    

    if (publication_data.title == ""):
        publication_data.title = publication.title
    else:
        publication.title = publication_data.title


    if (publication_data.content == ""):
        publication_data.content = publication.content
    else:
        publication.content = publication_data.content


    publication.save()

    return PublicationDataClass.from_instance(publication_model=publication)