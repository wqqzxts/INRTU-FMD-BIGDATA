from rest_framework import serializers

from . import services

class UserSerializer(serializers.Serializer):
    id = serializers.IntegerField(read_only = True)
    first_name = serializers.CharField()
    last_name = serializers.CharField()
    gender = serializers.CharField()
    apartments = serializers.IntegerField()
    email = serializers.EmailField()
    password = serializers.CharField(write_only = True)

    def to_internal_value(self, data):
        data = super().to_internal_value(data)

        if data['gender'] == 'Женский':
            data['gender'] = 'Female'
        elif data['gender'] == 'Мужской':
            data['gender'] = 'Male'
        elif data['gender'] not in ['Male', 'Female']:
            raise serializers.ValidationError({"gender": "Gender must be Male or Female"})            
    
        return services.UserDataClass(**data)