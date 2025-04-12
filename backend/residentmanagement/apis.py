from rest_framework import views, response, exceptions, permissions, status

from .serializers import UserSerializer
from . import services, authentication

class RegisterApi(views.APIView):
    def post(self, request):
        serializer = UserSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)

        data = serializer.validated_data
        serializer.instance = services.createuser(user_dc=data)

        return response.Response(data=serializer.data)
    

class LoginApi(views.APIView):
    def post(self, request):
        email = request.data["email"]
        password = request.data["password"]

        user = services.user_email_selector(email=email)

        if user is None:
            raise exceptions.AuthenticationFailed("Неправильные данные")
        
        if not user.check_password(raw_password=password):
            raise exceptions.AuthenticationFailed("Неправильные данные")
        
        token = services.create_token(user_id=user.id)            

        if user.is_staff:
            resp = response.Response(status=status.HTTP_200_OK)
            resp.data = {
                "user": {
                    "is_staff": user.is_staff
                }
            }
            resp.set_cookie(key="jwt", value=token, httponly=True)
        else:
            resp = response.Response(status=status.HTTP_200_OK)
            resp.data = {
                "user": {
                    "is_staff": user.is_staff
                }
            }
            resp.set_cookie(key="jwt", value=token, httponly=True)

        return resp
    

class UserApi(views.APIView):
    """
    This endpoint can only be used
    if the user is authenticated
    """
    authentication_classes = (authentication.CustomUserAuthentication, )
    permission_classes = (permissions.IsAuthenticated, )

    def get(self, request):
        user = request.user

        serializer = UserSerializer(user)

        return response.Response(serializer.data)
    

class LogoutApi(views.APIView):
    authentication_classes = (authentication.CustomUserAuthentication, )
    permission_classes = (permissions.IsAuthenticated, )

    def post(self, request):
        resp = response.Response(status=status.HTTP_204_NO_CONTENT)
        resp.delete_cookie("jwt")
        resp.data={"Операция выполнена успешно!"}

        return resp