# Generator

## Build

```
mvn clean install
```

## Generate Docker image

```
docker build -t ignishky/fma_generator ./generator
```

## Run

```
docker run --rm -v /tmp/input:/input -v /tmp/output:/output -t ignishky/fma_generator /input /output
```
