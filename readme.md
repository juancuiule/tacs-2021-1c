# TACS 1c-2021

### Pre-requisitos 📋

* [Docker](https://docs.docker.com/engine/install/)
* [Docker Compose](https://docs.docker.com/compose/install/)

## Ejecutar la aplicación 📦

* Para ejecutar la aplicación, es necesario tener [Docker](https://www.docker.com/) instalado
* Crear un archivo `.env` en el root del proeycto que tenga la key de la api de superheros (hay un ejemplo en `.env.example`)
* En el root del proyecto, ejecutar el comando `docker-compose up` 
* Esperar hasta que se genere el build y corra el container
* Cuando veas un mensaje similar a `http4s v0.21.16 on blaze v0.14.14 started at http://0.0.0.0:8080/`, el server está corriendo y podés acceder a la API con la URL [http://localhost:8080/](http://localhost:8080/)

## Endpoints

## Cards

---

`GET /cards/` - Trae todas las cartas en el sistema
```json
{
  "cards": [
    {
      "id": 69,
      "name": "Batman",
      "stats": {
          "height": 178,
          "weight": 77,
          "intelligence": 81,
          "speed": 29,
          "power": 63,
          "combat": 90,
          "strength": 40
      },
      "image": "https://www.superherodb.com/pictures2/portraits/10/100/10441.jpg",
      "biography": {
          "fullName": "Terry McGinnis",
          "publisher": "DC Comics"
      }
    },
    ...
  ]
}
```

---

`GET /cards/:id` - Trae una carta en particular
```json
{
  "id": 69,
  "name": "Batman",
  "stats": {
      "height": 178,
      "weight": 77,
      "intelligence": 81,
      "speed": 29,
      "power": 63,
      "combat": 90,
      "strength": 40
  },
  "image": "https://www.superherodb.com/pictures2/portraits/10/100/10441.jpg",
  "biography": {
      "fullName": "Terry McGinnis",
      "publisher": "DC Comics"
  }
}
```

---

`GET /cards/publishers` - Trae una lista de todos los publishers que tienen las cartas del sistema

```json
{
  "publishers": [
    "DC Comics",
    "Marvel"
  ]
}
```

---

`POST /cards` - Agrega una carta al sistema buscando al superheroe con ese id.

Es solo para admins, va en un header "Authorization" el token del administrador.

`Authorization: Bearer <token>`

body:
```json
{ "id": 69 }
```

response:
```json
{
  "id": 69,
  "name": "Batman",
  "stats": {
      "height": 178,
      "weight": 77,
      "intelligence": 81,
      "speed": 29,
      "power": 63,
      "combat": 90,
      "strength": 40
  },
  "image": "https://www.superherodb.com/pictures2/portraits/10/100/10441.jpg",
  "biography": {
      "fullName": "Terry McGinnis",
      "publisher": "DC Comics"
  }
}
```




## Superheros
---
`GET /superheros/name/:searchName` - Trae superheroes que tienen ese searchName dentro de su nombre

`GET /superheros/name/man`
repsonse:
```json
{
  "superheros": [
    {
      "id": 641,
      "name": "Superboy",
      "stats": {
        "height": 170,
        "weight": 68,
        "intelligence": 75,
        "speed": 83,
        "power": 95,
        "combat": 60,
        "strength": 95
      },
      "image": "https://www.superherodb.com/pictures2/portraits/10/100/789.jpg",
      "biography": {
        "fullName": "Kon-El / Conner Kent",
        "publisher": "DC Comics"
      }
    },
    ...
  ]
}
```
---
`GET /superheros/:id` - Trae superheroe con ese id

```json
{
  "id": 641,
  "name": "Superboy",
  "stats": {
    "height": 170,
    "weight": 68,
    "intelligence": 75,
    "speed": 83,
    "power": 95,
    "combat": 60,
    "strength": 95
  },
  "image": "https://www.superherodb.com/pictures2/portraits/10/100/789.jpg",
  "biography": {
    "fullName": "Kon-El / Conner Kent",
    "publisher": "DC Comics"
  }
}
```


## Decks
---
`GET /decks/:id` - Trae el mazo con ese id

```json
{
  "id": 1,
  "name": "Marvel Deck",
  "cards": [
    ...
  ]
}
```
---
`GET /decks/` - Trae todos los mazos

```json
{
  "decks": [
    { "id": 1, "name": "Marvel Deck", "cards": [...] },
    ...
  ]
}
```
---
`PATCH /decks/:id` - Agrega una carta al mazo

body:
```json
{ "cardId": 1 }
```

response:
```json
{
  "id": 1,
  "name": "Marvel Deck",
  "cards": [
    ...
  ]
}
```
---
`DELETE /decks/:id` - Borra el mazo con ese id

## Users / Auth
---
`POST /users/` - Crea un usuario con determinado rol

body:
```json
{
  "userName": "usuarioDePrueba",
  "password": "passwordDePrueba",
  "role": "Admin" | "Player" // esto cambiaría
}
```

response:
```json
{
    "userName": "usuarioDePrueba",
    "id": 5118330560133248253,
    "accessToken": "<token>"
}
```
---
`POST /users/login` - Devuelve un token que identifica al usuario para que pueda operar en el sistema.

body:
```json
{
  "userName": "usuarioDePrueba",
  "password": "passwordDePrueba",
}
```

response:
```json
{
    "userName": "usuarioDePrueba",
    "id": 5118330560133248253,
    "accessToken": "<token>"
}
```

