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
    content: str
    date_published: datetime.datetime = None
    user: residentmanagement_services.UserDataClass = None
    id: int = None


    @classmethod
    def from_instance(cls, publication_model: "Publication") -> "PublicationDataClass":
        return cls(
            content=publication_model.content,
            date_published=publication_model.date_published,
            id=publication_model.id,
            user=publication_model.user
        )
    

def create_publication(user, publication: "PublicationDataClass") -> "PublicationDataClass":
    publication_create = publication_models.Publication.objects.create(
        content=publication.content,
        user=user
    )

    return PublicationDataClass.from_instance(publication_model=publication_create)


# deprecated as i change the logic of showing publication to users
# def get_user_posts(user: "User") -> list["PublicationDataClass"]:
#     user_publications = publication_models.Publication.objects.filter(user=user)

#     return [PublicationDataClass.from_instance(single_publication) for single_publication in user_publications]
def get_all_publications() -> list["PublicationDataClass"]:
    publications = publication_models.Publication.objects.all()

    return [PublicationDataClass.from_instance(publication) for publication in publications]



def get_user_specific_publication(publication_id: int) -> "PublicationDataClass":
    publication = get_object_or_404(publication_models.Publication, pk=publication_id)

    return PublicationDataClass.from_instance(publication_model=publication)


def delete_user_post(user: "User", publication_id: int) -> None:
    publication = get_object_or_404(publication_models.Publication, pk=publication_id)

    if user.id != publication.user.id and not user.is_staff:
        raise exceptions.PermissionDenied("Вы пытаетесь получить данные, доступ к которым не имеете!")

    publication.delete()    


def update_user_publication(user: "User", publication_id: int, publication_data: "PublicationDataClass"):
    publication = get_object_or_404(publication_models.Publication, pk=publication_id)

    if user.id != publication.user.id and not user.is_staff:
        raise exceptions.PermissionDenied("Вы пытаетесь получить данные, доступ к которым не имеете!")
    
    publication.content = publication_data.content
    publication.save()

    return PublicationDataClass.from_instance(publication_model=publication)