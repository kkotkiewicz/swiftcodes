# SWIFT Codes Service

A Spring Boot REST API for managing SWIFT codes, supporting retrieval, creation, and deletion of bank details.

---

## Running locally

```shell
git clone https://github.com/kkotkiewicz/swiftcodes.git
```

### Make sure that
- Your docker engine is running
- Your ports 8080 and 5432 are not in use

### To build and run application
```shell
docker compose up --build
```

or

```shell
docker-compose up --build
```

depending which system you are using

### To stop application

```shell 
docker compose down
```

or

```shell 
docker-compose down
```

### If you want to change the base csv file 
- if you have containers running run `docker compose down`
- go to `./src/main/resources`,
- delete `swift_codes.csv`,
- put in that place your own csv,
- change its name to `swift_codes.csv`
- make sure that all required columns are in the file with following names: `COUNTRY ISO2 CODE`, `NAME`, `SWIFT CODE`, `COUNTRY NAME`
- if any of the columns are missing or have a different name the data will not be put in the database
- start service again with `docker compose up`

### Running Tests

To run both Unit and Integration Tests use the following command:

```shell
./gradlew test
```

Make sure that you are using java 21

## Features
- Parse CSV with SWIFT code data and store it in the database
- Retrieve details of a SWIFT code (headquarters or branch).  
- Get all SWIFT codes for a specific country.  
- Add new SWIFT code entries.  
- Delete SWIFT code data.

---

## Technologies Used
- **Kotlin** + **Spring Boot**
- **Spring Data JPA**
- **PostgreSQL**
- **Gradle**
- **MockMvc** (for testing APIs)
- **H2 Database** (for testing)

---

## API Endpoints

### Get SWIFT Code Details

GET `/v1/swift-codes/{swift-code}`

Returns details for a SWIFT code (headquarters or branch).

#### Response for Headquarters SWIFT Code

```json
{
    "address": "123 Bank St",
    "bankName": "Bank ABC",
    "countryISO2": "US",
    "countryName": "United States",
    "isHeadquarter": true,
    "swiftCode": "ABCDEFXXX",
    "branches": [
        {
            "address": "456 Branch St",
            "bankName": "Bank ABC",
            "countryISO2": "US",
            "isHeadquarter": false,
            "swiftCode": "ABCDEF002"
        }
    ]
}
```

### Get SWIFT Codes by Country

GET `/v1/swift-codes/country/{countryISO2}`

Retrieves all SWIFT codes for a given country.

#### Example Response

```json
{
    "countryISO2": "US",
    "countryName": "United States",
    "swiftCodes": [
        {
            "address": "123 Bank St",
            "bankName": "Bank ABC",
            "countryISO2": "US",
            "isHeadquarter": true,
            "swiftCode": "ABCDEFXXX"
        }
    ]
}
```

### Add a New SWIFT Code

POST `/v1/swift-codes`

Adds a new SWIFT code entry.

#### Request

```json
{
    "address": "789 New St",
    "bankName": "Bank XYZ",
    "countryISO2": "GB",
    "countryName": "United Kingdom",
    "isHeadquarter": false,
    "swiftCode": "XYZ123XXX"
}
```

#### Response

```json
{
    "message": "Successfully saved bank information for SWIFT code XYZ123XXX"
}
```

### Delete a SWIFT Code

DELETE `/v1/swift-codes/{swift-code}`

Deletes SWIFT code data from the database

```json
{
    "message": "Successfully deleted SWIFT code XYZ123XXX"
}
```
