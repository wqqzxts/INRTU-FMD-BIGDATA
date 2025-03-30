from django.contrib import admin
from django.contrib.auth.admin import UserAdmin

from . import models

# class UserAdmin(admin.ModelAdmin):
#     list_display = (
#         "id",
#         "email",
#         "first_name",
#         "last_name",
#         "gender",        
#         "apartments",        
#     )


# admin.site.register(models.User, UserAdmin)

class CustomUserAdmin(UserAdmin):
    fieldsets = (
        (None, {"fields": ("email", "password")}),
        ("Personal Info", {"fields": ("first_name", "last_name", "gender", "apartments")}),
        ("Permissions", {"fields": ("is_active", "is_staff", "is_superuser", "groups", "user_permissions")}),
    )
    
    add_fieldsets = (
        (None, {
            "classes": ("wide",),
            "fields": (
                "email",
                "password1",
                "password2",
                "first_name",
                "last_name",
                "gender",
                "apartments"
            ),
        }),
    )
    
    list_display = (
        "id",
        "email",
        "first_name",
        "last_name",
        "gender",
        "apartments",
        "is_staff",
    )
    
    search_fields = ("email", "first_name", "last_name")
    ordering = ("email",)
    filter_horizontal = ("groups", "user_permissions",)


admin.site.register(models.User, CustomUserAdmin)    