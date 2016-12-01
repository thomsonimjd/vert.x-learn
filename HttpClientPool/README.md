# Vertx HttpClient Pooling issue

Vert.x has a problem managing the connection pool of httpClients, especially when the response hangs out. HttpClientPoolTest will give the result.


## Issue explanation

Mock server reponds the client with the delay of 3 seconds.
HttpClient having the below configuration.
```
IdleTimeout = 2 Secs
MaxPoolSize = (2)
MaxWaitQueueSize = (0)
ConnectTimeout = 2 Sec
```

When 4 http calls made to the server first two request will get connection refuced error and next 2 will get the ConnectionPoolTooBusyException.

Same request will get passed if there is no delay in the server.

## Testing

Run the unit test HttpClientPoolTest.
