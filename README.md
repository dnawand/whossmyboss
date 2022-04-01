# Hierarchy API

## Running locally

With Java 11 installed locally, run the following:

```bash
./gradlew build -x text
java -jar build/libs/whosmyboss-1.0.0-all.jar
```

## Running on containers

With Docker and docker-compose installed, run:

```sh
docker-compose up --build
```

## Tests

```bash
./gradlew test
```

## API

The API runs on `http://localhost:8080` and requires a Basic Authententication. Also every used name must be unique. The works as an identifier.

Set these credentials during your requests:

- __user__: `user`
- __password__: `password`

### Resolve Hierarchy

Path: `/hierarchy`
Method: `POST`
Body: `Map<String, String>`
Example:
```json
{
  "Pete": "Nick",
  "Barbara": "Nick",
  "Nick": "Sophie",
  "Sophie": "Jonas"
}
```

### Get supervisors

Get the supervisors for a given name (case-sensitive).

Path: `/hierarchy/{name}` \
Method: `GET` \
Example: `/hierarchy/Sophie`

