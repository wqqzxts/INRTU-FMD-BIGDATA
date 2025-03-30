from django.db import models

from django.conf import settings

class Publication(models.Model):
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        verbose_name="Пользователь"
    )

    content = models.TextField(verbose_name="Содержание")
    date_published = models.DateTimeField(auto_now=True, verbose_name="Дата публикации")