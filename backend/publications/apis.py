from rest_framework import views, response, permissions
from rest_framework import status

from residentmanagement import authentication
from . import serializers
from . import services
from . import publications_permissions

class PublicationListCreateApi(views.APIView):
    authentication_classes = (authentication.CustomUserAuthentication, )
    permission_classes = (publications_permissions.IsStaffOrReadonly, )

    def get(self, request):
        publication_list = services.get_all_publications()
        serializer = serializers.PublicationSerializer(publication_list, many=True)

        return response.Response(data=serializer.data)
    

    def post(self, request):
        serializer = serializers.PublicationSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)

        data = serializer.validated_data

        serializer.instance = services.create_publication(user=request.user, publication=data)

        
        return response.Response(data=serializer.data)    
    

class PublicationSpecificRetrieveUpdateDelete(views.APIView):
    authentication_classes = (authentication.CustomUserAuthentication, )
    permission_classes = (publications_permissions.IsStaffOrReadonly, )

    def get(self, request, publication_id):
        publication = services.get_user_specific_publication(publication_id=publication_id)
        serializer = serializers.PublicationSerializer(publication)

        return response.Response(data=serializer.data)
    
    def delete(self, request, publication_id):
        services.delete_user_post(user=request.user, publication_id=publication_id)

        return response.Response(status=status.HTTP_204_NO_CONTENT)
    

    def put(self, request, publication_id):
        serializer = serializers.PublicationSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        publication = serializer.validated_data
        serializer.instance = services.update_user_publication(user=request.user, publication_id=publication_id, publication_data=publication)

        return response.Response(data=serializer.data)