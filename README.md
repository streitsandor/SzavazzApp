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

## Adatbázis előkészítése

1. Indítsd el a XAMPP Control Panelben a MySQL szolgáltatást.

2. Futtasd le a projektben található create_database.sql fájlt.

Xampp segítségével: (karakter hiba előfordul!)
```bash
C:\xampp\mysql\bin\mysql.exe -u root < database\create_database.sql
```

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