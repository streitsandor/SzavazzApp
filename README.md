# SzavazzApp

Online szavazási platform, ahol a felhasználók aktív szavazásokra voksolhatnak, megnézhetik az eredményeket, az admin pedig kezelheti a szavazásokat.

## Használt technológiák

Backend:

- Java 25
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- Maven
- MySQL

Frontend:

- Thymeleaf
- Bootstrap 5
- jQuery
- FontAwesome

Tesztelés:

- JUnit / Mockito
- Jest / jsdom

---

## Előfeltételek

A projekt indításához szükséges:

- Java 25
- Maven
- Node.js + npm
- XAMPP
- MySQL / MariaDB

---

## Program indítás (docker)

A projekt Dockerrel is futtatható. Ebben az esetben nem szükséges külön XAMPP MySQL-t indítani, mert a `docker-compose.yml` saját MySQL konténert hoz létre.

### Szükséges:
- Docker Desktop
- Maven
- Java 25

### Lépések:
1. Docker indítás
```
    - mvn clean package -DskipTests
    - docker compose up --build
```

2. Böngészőbe: http://localhost:8080/login

3. Belépés:
    - user / user123
    - admin / admin123

---

## Tesztek futtatása

```
Backend: 
    - mvn test

Frontend:
    - npm install (csak első alkalommal)
    - npm run test:frontend

Összes:
    - npm run test:all
```

---

## API dokumentációk

```text
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs
http://localhost:8080/v3/api-docs.yaml
```
*Csak bejelentkezés után futtathatóak!*

---

## Adatbázis előkészítése (XAMPP esetén)

1. Indítsd el a XAMPP Control Panelben a MySQL szolgáltatást.

2. Futtasd le a projektben található create_database.sql fájlt.

Xampp segítségével:
```bash
C:\xampp\mysql\bin\mysql.exe -u root < database\create_database.sql
```
*Karakter hiba előfordulhat, inkább SQL querry futtató környezetből (pl: phpmyadmin, dbvisualizer) érdemes futtatni!*
