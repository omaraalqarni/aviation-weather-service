# Flight-weather-service


![dd](https://img.shields.io/badge/vert.x-4.3.8-purple.svg)


This application was generated using http://start.vertx.io

### Tasks
- [x] Implement AviationVerticle.
  - [x] Implement `GET/flights`.
- [x] fetch data from AviationStack.
- [x] manipulate result to fit the required JSON file.
- [x] Implement Weather Verticle
- [x] fetch data from WeatherAPI
- [x] Implement Event Bus to send the result to Aviation Verticle
  - When weather data is ready, send it to 'address'
  - Aviation verticle waits to consume it.
  - Aviation verticle appends it in the response
- [ ] Implement Circuit Breaker
- [ ] make sure to handle all exceptions
- [x] implement Database Verticle
- [x] implement db functions with weather data
- [ ] save each weather data api call to db (x)
- [x] implement fallout api for weather api.
- [x] demultiplex flight request to multiple concurrent requests.


## Building

To launch your tests:

`./mvnw clean test`

To package your application:

`./mvnw clean package`


To run your application:

`./mvnw clean compile exec:java`

