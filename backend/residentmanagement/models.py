from django.db import models
from django.contrib.auth import models as auth_models

class UserManager(auth_models.BaseUserManager):
    def create_user(self, first_name: str, last_name: str, gender: str, apartments: int, email: str, password: str = None, is_staff=False, is_superuser=False) -> "User":
        if not email:
            raise ValueError("Пользователь должен указать электронную почту")
        if not first_name:
            raise ValueError("Пользователь должен указать имя")
        if not last_name:
            raise ValueError("Пользователь должен указать фамилию")
        if gender not in ['Male', 'Female']:
            raise ValueError("Пол должен быть 'Male' или 'Female'")

        user = self.model(email=self.normalize_email(email))
        user.first_name = first_name
        user.last_name = last_name
        user.gender = gender
        user.apartments = apartments
        user.set_password(password)
        user.is_active = True
        user.is_staff = is_staff
        user.is_superuser = is_superuser
        user.save()

        return user

    def create_superuser(self, first_name: str, last_name: str, email: str, gender: str = None, apartments: int = None, password: str = None) -> "User":
        user = self.create_user(
            first_name=first_name,
            last_name=last_name,
            email=email,
            gender=gender,
            apartments=apartments,
            password=password,
            is_staff=True,
            is_superuser=True,
        )
        user.save()
        
        return user

class User(auth_models.AbstractUser):
    GENDER_CHOICES = [
        ('Male', 'Мужской'),
        ('Female', 'Женский'),
    ]

    first_name = models.CharField(verbose_name="Имя", max_length=255)
    last_name = models.CharField(verbose_name="Фамилия", max_length=255)
    gender = models.CharField(verbose_name="Пол", max_length=6, choices=GENDER_CHOICES)
    apartments = models.IntegerField(verbose_name="Номер квартиры", null=True)
    email = models.EmailField(verbose_name="Электронная почта", max_length=255, unique=True)
    password = models.CharField(verbose_name="Пароль", max_length=255)
    username = None

    objects = UserManager()

    USERNAME_FIELD = "email"
    REQUIRED_FIELDS = ["first_name", "last_name"]

    class Meta:
        verbose_name = "Пользователь"
        verbose_name_plural = "Пользователи"
