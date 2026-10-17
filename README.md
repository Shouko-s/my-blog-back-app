# My Blog — бэкенд

REST-бэкенд приложения-блога на Spring Boot 4, Java 21, PostgreSQL.

Требования: JDK 21, Docker. Gradle ставить не нужно — используется Gradle Wrapper (`./gradlew`).

## Сборка

```bash
./gradlew clean build
```

Результат — исполняемый jar `build/libs/My-blog-back-app-0.0.1-SNAPSHOT.jar`. Собрать без запуска тестов: `./gradlew clean build -x test`.

## Запуск тестов

```bash
./gradlew test
```

Тесты используют in-memory H2, поэтому Docker и PostgreSQL для них не нужны.

## Запуск приложения

Сначала запустите PostgreSQL из корня проекта:

```bash
docker compose up -d
```

Поднимется БД `blog` на `localhost:5432` (пользователь `admin`, пароль `admin`). Параметры подключения и настройки пула соединений HikariCP — в `src/main/resources/application.properties`. Пароль можно переопределить переменной окружения `DB_PASSWORD`. Таблицы создаются автоматически при старте приложения.

Затем запустите приложение одним из способов:

```bash
./gradlew bootRun
```

или из собранного jar:

```bash
java -jar build/libs/My-blog-back-app-0.0.1-SNAPSHOT.jar
```

В IntelliJ IDEA достаточно запустить класс `MyBlogBackAppApplication`. Приложение стартует на встроенном Tomcat, порт `8080`.

## Использование

Проверка, что бэкенд работает:

```bash
curl "http://localhost:8080/api/posts?search=&pageNumber=1&pageSize=5"
```

Работа через фронтенд:

1. Распакуйте архив с фронтендом, перейдите в директорию с его `docker-compose.yaml` и выполните `docker compose up -d`.
2. Откройте в браузере `http://localhost`.

Эндпоинты:

| Метод    | Путь                                       | Описание                                     |
|----------|--------------------------------------------|----------------------------------------------|
| `GET`    | `/api/posts?search=&pageNumber=&pageSize=` | лента постов с поиском и пагинацией          |
| `GET`    | `/api/posts/{id}`                          | пост по id                                   |
| `POST`   | `/api/posts`                               | создание поста                               |
| `PUT`    | `/api/posts/{id}`                          | редактирование поста                         |
| `DELETE` | `/api/posts/{id}`                          | удаление поста вместе с комментариями        |
| `POST`   | `/api/posts/{id}/likes`                    | +1 лайк, возвращает новое число лайков       |
| `PUT`    | `/api/posts/{id}/image`                    | загрузка картинки (`multipart/form-data`, поле `image`) |
| `GET`    | `/api/posts/{id}/image`                    | получение картинки                           |
| `GET`    | `/api/posts/{id}/comments`                 | комментарии поста                            |
| `GET`    | `/api/posts/{id}/comments/{commentId}`     | комментарий по id                            |
| `POST`   | `/api/posts/{id}/comments`                 | добавление комментария                       |
| `PUT`    | `/api/posts/{id}/comments/{commentId}`     | редактирование комментария                   |
| `DELETE` | `/api/posts/{id}/comments/{commentId}`     | удаление комментария                         |

Картинка принимается только с типом `image/jpeg`, `image/png`, `image/gif` или `image/webp` и не должна быть пустой — иначе вернётся `400`.

## Ошибки

Все ошибки возвращаются в едином формате:

```json
{
  "code": 404,
  "status": "NOT_FOUND",
  "message": "Post with id 42 not found",
  "timestamp": "2026-10-16T12:00:00"
}
```

| Код   | Когда                                                              |
|-------|--------------------------------------------------------------------|
| `400` | некорректные параметры (пагинация, недопустимая или пустая картинка) |
| `404` | пост, комментарий или картинка не найдены                          |
| `409` | нарушение целостности данных в БД                                  |
| `500` | непредвиденная ошибка (подробности пишутся в лог)                  |

> При редактировании комментария идентификатор поста берётся из URL; поле `postId` в теле запроса игнорируется, так как фронтенд передаёт его некорректно.
