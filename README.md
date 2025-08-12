# README: Запуск проекта. Способ 1:

Этот проект состоит из двух микросервисов: **Auth-Service** (сервис аутентификации и авторизации) и **Notification-Service** (сервис для отправки email-уведомлений).

---

## Требования

Перед началом убедитесь, что у вас установлены следующие инструменты:
- **Java**: версия 21+.
- **Apache Maven**: версия 3.9.x или выше.
- **PostgreSQL**: версия 15.x или совместимая.

---

## Шаги запуска

### 1. Клонирование репозитория

Скачайте проект с Git-репозитория:
```bash
git clone https://github.com/YOUR_USERNAME/YOUR_REPOSITORY.git
```

Перейдите в директорию проекта:
```bash
cd YOUR_REPOSITORY
```

---

### 2. Настройка базы данных (PostgreSQL)

1. Убедитесь, что PostgreSQL установлен и запущен на вашем устройстве.
2. Подключитесь к вашей PostgreSQL.
3. Создайте базу данных и пользователя:
   ```sql
   CREATE DATABASE auth_service;
   CREATE USER user WITH PASSWORD 'your_password';
   ALTER ROLE user SET client_encoding TO 'utf8';
   ALTER ROLE user SET default_transaction_isolation TO 'read committed';
   ALTER ROLE user SET timezone TO 'UTC';
   GRANT ALL PRIVILEGES ON DATABASE auth_service TO katusha;
   ```

4. Создайте таблицы, выполнив следующий DDL код:

   Для таблицы **users**:
   ```sql
   CREATE TABLE users
   (
       id         BIGSERIAL PRIMARY KEY,
       username   VARCHAR(50) NOT NULL UNIQUE,
       email      VARCHAR(100) NOT NULL UNIQUE,
       first_name VARCHAR(100),
       last_name  VARCHAR(100),
       role       VARCHAR(20) DEFAULT 'USER' CHECK ((role)::TEXT = ANY (ARRAY['USER', 'ADMIN'])),
       created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
       updated    TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
   );

   ALTER TABLE users OWNER TO katusha;
   ```

   Для таблицы **security**:
   ```sql
   CREATE TABLE security
   (
       id       BIGSERIAL PRIMARY KEY,
       login    VARCHAR(50) NOT NULL UNIQUE,
       password VARCHAR(90) NOT NULL,
       role     VARCHAR(20) DEFAULT 'USER' NOT NULL,
       created  TIMESTAMP DEFAULT NOW() NOT NULL,
       updated  TIMESTAMP DEFAULT NOW(),
       user_id  BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE
   );

   ALTER TABLE security OWNER TO katusha;
   ```

---

### 3. Конфигурация Auth-Service и Notification-Service

В каждом проекте убедитесь, что файл `application.properties` корректно настроен.

#### Auth-Service:
Файл расположен по пути `authservice/src/main/resources/application.properties`.

Проверьте и внесите ваши значения:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/auth_service
spring.datasource.username=katusha
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.default_schema=public

jwt.secret=your_jwt_secret_key
jwt.expirationMs=3600000
```

#### Notification-Service:
Файл расположен по пути `notificationservice/src/main/resources/application.properties`.

Проверьте настройки для подключения к SMTP-серверу:
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_email_password

spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
server.port=8081
```

---

### 4. Сборка и запуск приложений

#### Auth-Service

1. Перейдите в директорию `auth-service`:
   ```bash
   cd auth-service
   ```

2. Скомпилируйте проект:
   ```bash
   mvn clean install
   ```

3. Запустите приложение:
   ```bash
   mvn spring-boot:run
   ```

Сейчас **Auth-Service** доступен по адресу:
```text
http://localhost:8080
```

---

#### Notification-Service

1. В новой командной строке перейдите в директорию `notification-service`:
   ```bash
   cd notification-service
   ```

2. Скомпилируйте проект:
   ```bash
   mvn clean install
   ```

3. Запустите приложение:
   ```bash
   mvn spring-boot:run
   ```

Теперь **Notification-Service** запущен и доступен по адресу:
```text
http://localhost:8081
```

---

### 5. Проверка микросервисов через Postman

В проекте есть готовая коллекция запросов для **Postman**, позволяющая протестировать оба сервиса и их взаимодействие. Импортируйте коллекцию через Postman.

## Запуск проекта.способ 2:
### Предварительные требования
Перед запуском убедитесь, что на вашей машине установлены следующие инструменты:
- **Docker** 
- **Docker Compose** 

### Конфигурация
Прежде чем запускать приложение, убедитесь, что вы добавили свои переменные окружения в **`docker-compose.yml` **.
#### Обязательные действия:
1. В файле `docker-compose.yml` замените пути к вашим проектам:
``` yaml
   auth-service:
     build:
       context: ./path-to-auth-project 

   notification-service:
     build:
       context: ./path-to-notification-project 
```
1. Укажите правильные учетные данные почтового сервиса и базы данных:
    - Переменные окружения для **Notification-Service**:
``` yaml
     environment:
       SPRING_MAIL_HOST: "smtp.gmail.com"
       SPRING_MAIL_USERNAME: "your_email@gmail.com"
       SPRING_MAIL_PASSWORD: "your_app_password" 
```
- Переменные окружения для базы данных PostgreSQL:
``` yaml
     environment:
       POSTGRES_USER: "testuser"
       POSTGRES_PASSWORD: "testpassword"
       POSTGRES_DB: "postgres"
```

### Шаги для запуска
#### 1. Сборка и запуск через Docker Compose
В корневом каталоге проекта выполните команду:
``` bash
docker-compose up --build
```

#### 2. Проверка состояния контейнеров
Для проверки запущенных контейнеров выполните:
``` bash
docker ps
```
Убедитесь, что контейнеры `auth-service`, `notification-service` и `auth-database` отображаются как запущенные.
### Эндпоинты сервисов
После успешного запуска приложений, они будут доступны по следующим URL:
1. **Auth-Service**:
    - Базовый URL: `http://localhost:8080`
    - Пример эндпоинтов:
        - Создание пользователя: `POST /users/register`
        - Авторизация пользователя: `POST /auth/login`
        - Получение всех пользователей (требует admin-прав): `GET /users`

2. **Notification-Service**:
    - Базовый URL: `http://localhost:8081`
    - Уведомления отправляются автоматически при совершении юзером действий.
   
