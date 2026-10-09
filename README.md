# My Blog — бэкенд

REST-бэкенд приложения-блога на Spring Framework 6 (без Spring Boot), Java 21, PostgreSQL.

Требования: JDK 21, Maven 3.9+, Docker, Apache Tomcat 11 на Java 21.

## Сборка

```bash
mvn clean package
```

Результат — `target/ROOT.war`. Собрать без запуска тестов: `mvn clean package -DskipTests`.

## Запуск тестов

```bash
mvn test
```

Тесты используют in-memory H2, поэтому Docker и PostgreSQL для них не нужны.

## Деплой в Tomcat

Сначала запустите PostgreSQL из корня проекта:

```bash
docker compose up -d
```

Поднимется БД `blog` на `localhost:5432` (пользователь `admin`, пароль `admin`). Параметры подключения — в `src/main/resources/application.properties`. Таблицы создаются автоматически при старте приложения.

### Через IntelliJ IDEA

1. Скачайте и распакуйте [Apache Tomcat 11](https://tomcat.apache.org/download-11.cgi).
2. Откройте **Run → Edit Configurations…**, нажмите **+** и выберите **Tomcat Server → Local**.
3. На вкладке **Server** в поле **Application server** нажмите **Configure…** и укажите директорию распакованного Tomcat. Порт HTTP — `8080`.
4. На вкладке **Deployment** нажмите **+ → Artifact…**, выберите war-артефакт проекта (`my-blog-back-app:war` или `my-blog-back-app:war exploded`) и задайте **Application context**: `/`.
5. Запустите конфигурацию. IDEA соберёт артефакт и задеплоит его в Tomcat.

## Запуск и использование

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

> При редактировании комментария идентификатор поста берётся из URL; поле `postId` в теле запроса игнорируется, так как фронтенд передаёт его некорректно.
