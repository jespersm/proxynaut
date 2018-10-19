# Proxynaut - a proxy for Micronaut

A simple proxy for exposing other services through the main Micronaut server.
Just add the dependency and put in the configuration into ```application.yml```: 

```
# This will set up two proxied paths...
proxynaut:
    api:
        context: /api/
        uri: https://my-backend-api-service.some-cloud.com/
        methods: GET
        timeoutMs: 30000
    blobstorage:
        context: /blobs/
        uri: https://${my.bucket.name}.some-cloud.com/
        methods: *
        timeoutMs: 60000
```

See the example project in [proxynaut-example](proxynaut-example).

License: Apache 2.0
