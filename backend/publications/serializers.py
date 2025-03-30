from rest_framework import serializers

from residentmanagement.serializers import UserSerializer
from . import services

class PublicationSerializer(serializers.Serializer):
    id = serializers.IntegerField(read_only=True)
    content = serializers.CharField()
    date_published = serializers.DateTimeField(read_only=True)
    user = UserSerializer(read_only=True)


    def to_internal_value(self, data):
        data = super().to_internal_value(data)

        return services.PublicationDataClass(**data)