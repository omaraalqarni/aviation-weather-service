### Aviation Verticle Flow

1. Fetch json from aviation API
2. Get "data" array
3. go through the flights and collect iatas
4. bulk search and match the iatas to its lat long coordinates
5. return lat longs and match them with flights
3. go through each flight
4. While in each flight do the following
   - get the IATA (not async)
   - send a single IATA to the DataBaseVerticle through event bus (not async)
   - await the reply for its lang and lat (async in db verticle so it is future value)
   - send to the WeatherVerticle its lat long (not async)
   - await the reply of the weather data (async in weather verticle so it is future)
   - append the weather last in the arrival object (not async)
   - take all flights and put them in either today or yesterday (not async)
   - repeat for all flights (not async)


## Separating functionalities

### AvitaionService

 `Future?<JsonObject> processAllFlights`
- takes all flights in `data` JsonArray
- go through the objs in a loop
- calls `processSingleFlight`
- put all processed flights in a new JsonArray
- return Json Array

`Future<JsonObject> processSingleFlight`
- takes JsonObject `flight`
- get its IATA
- publish IATA to DB Verticle .
- receive lat long
- Publish lat long to Weather Verticle
----
### DataBaseService
`Future<JsonObject> GetLatLong(String)`
- calls DB to find the matching lat long for iata
- reply with lat long future
---
### WeatherVerticle
`Future<JsonObject>GetWeather(lat,long)`
- takes lat long from event bus
- passes them to `fetchWeatherData`
- Recieves a promise of the weather data
- reply to AviationVerticle with weather data
---
### WeatherApi

`Future<JsonObject> fetchWeatherData(lat,long)`
- fetches the data required and return the future obj




### how to optimize weather
1. get all icaos
2. seperate them by 6 for example
3. store each weather in JsonObject with `icao` as key
4. store all in JsonArray
5. return to attachweather
