# Vertx HttpClient Pooling issue

Vert.x Http connection pool has an issue, when connection timeout happens. 
HttpClientPoolTest here reproduces the issue.


## Issue Detail

Mock server reponds the client with the delay of 3 seconds.
HttpClient having the below configuration.

```
IdleTimeout = 2 Secs
MaxPoolSize = (2)
MaxWaitQueueSize = (0)
ConnectTimeout = 2 Sec
```

When 4 http calls made to the server,  

First two request will get connection closed as client timeout is lower than server latency. 
2 seconds delay is provided for clients to close the connection to be closed and return to the pool 
However upon request of 3rd httpclient from pool we encounter - Connection pool reached max wait queue size of 0 



## Testing

Run the unit test HttpClientPoolTest.
