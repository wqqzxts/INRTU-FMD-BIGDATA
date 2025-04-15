from rest_framework import serializers

from . import services

class UserSerializer(serializers.Serializer):
    id = serializers.IntegerField(read_only = True)
    first_name = serializers.CharField(required=False)
    last_name = serializers.CharField(required=False)
    gender = serializers.CharField(required=False)
    apartments = serializers.IntegerField(required=False)
    email = serializers.EmailField(required=False)
    password = serializers.CharField(write_only = True, required=False, allow_blank=True, allow_null=True)

    def to_internal_value(self, data):
        data = super().to_internal_value(data)

        if 'gender' in data:
            if data['gender'] == 'M':
                data['gender'] = 'Male'
            if data['gender'] == 'F':
                data['gender'] = 'Female'
            if data['gender'] == 'Женский':
                data['gender'] = 'Female'
            elif data['gender'] == 'Мужской':
                data['gender'] = 'Male'
            elif data['gender'] not in ['Male', 'Female']:
                raise serializers.ValidationError({"gender": "Gender must be Male or Female"})

        
        if 'password' in data and not data['password']:
            del data['password']
        
        return services.UserDataClass(**data)