# Vert.x Microservice

REST API microservice built with Vert.x 4, MongoDB, and JWT authentication.

## Stack

- **Vert.x 4** — async/reactive HTTP server
- **MongoDB** — persistence via `vertx-mongo-client`
- **JWT (HS256)** — authorization via `vertx-auth-jwt`

## Data model

**users** collection
| field    | type   |
|----------|--------|
| `_id`    | UUID   |
| `login`  | string |
| `password` | SHA-256 hash (Base64) |

**items** collection
| field   | type          |
|---------|---------------|
| `_id`   | UUID          |
| `owner` | UUID (user id)|
| `name`  | string        |

## Endpoints

| Method | Path        | Auth | Description                  |
|--------|-------------|------|------------------------------|
| POST   | `/register` | No   | Create account               |
| POST   | `/login`    | No   | Authenticate, returns JWT    |
| POST   | `/items`    | Yes  | Create item for current user |
| GET    | `/items`    | Yes  | List items of current user   |

Authorization header format: `Authorization: Bearer <token>`

## Running

### 1. Start MongoDB

```bash
docker-compose up -d
```

### 2. Build and run

```bash
mvn package
java -jar target/vertx-microservice-1.0-SNAPSHOT.jar
```

Server starts on `http://localhost:3000`.

## Example requests

```bash
# Register
curl -X POST http://localhost:3000/register \
  -H "Content-Type: application/json" \
  -d '{"login":"user@example.com","password":"Secret123"}'

# Login
curl -X POST http://localhost:3000/login \
  -H "Content-Type: application/json" \
  -d '{"login":"user@example.com","password":"Secret123"}'
# => {"token":"eyJ..."}

# Create item
curl -X POST http://localhost:3000/items \
  -H "Authorization: Bearer eyJ..." \
  -H "Content-Type: application/json" \
  -d '{"name":"My item"}'

# List items
curl http://localhost:3000/items \
  -H "Authorization: Bearer eyJ..."
# => [{"id":"...","name":"My item"}]
```
## Configuration

Default values work out of the box. Override via Vert.x config:

| Key                      | Default                                        |
|--------------------------|------------------------------------------------|
| `mongo.connection_string`| `mongodb://localhost:27017`                    |
| `mongo.db_name`          | `microservice`                                 |
| `jwt.secret`             | `super-secret-key-must-be-at-least-32-chars!`  |


## TODO

- [ ] Zastąpić SHA-256 algorytmem BCrypt (odporny na rainbow tables, wbudowana sól)
- [ ] Dodać HTTPS — HTTP przesyła hasło plaintextem przez sieć
- [ ] Nie zwracać `err.getMessage()` klientowi w błędach 500 — generyczny komunikat, szczegóły tylko w logach
- [ ] Wymagać `jwt.secret` z konfiguracji zamiast mieć hardcoded default — fail fast przy starcie jeśli brak
- [ ] Dodać indeks unikalny na polu `login` w kolekcji `users` — teraz każdy login to full scan kolekcji
- [ ] Walidacja inputu — minimalna długość hasła, limit rozmiaru pól, format loginu
