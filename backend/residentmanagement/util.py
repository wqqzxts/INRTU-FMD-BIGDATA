from rest_framework import status, exceptions
from rest_framework.views import exception_handler

def custom_exception_handler(exc, context):
    response = exception_handler(exc, context)
    if isinstance(exc, exceptions.AuthenticationFailed):
        response.status_code = status.HTTP_401_UNAUTHORIZED

    return response