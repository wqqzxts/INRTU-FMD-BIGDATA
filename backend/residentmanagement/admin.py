from django.contrib import admin

from . import models

class UserAdmin(admin.ModelAdmin):
    list_display = (
        "id",
        "email",
        "first_name",
        "last_name",
        "gender",        
        "apartments",        
    )


admin.site.register(models.User, UserAdmin)