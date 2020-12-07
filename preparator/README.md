# Preparator

## Build

```
mvn clean install
```

## Generate Docker image

```
docker build -t ignishky/fma_preparator ./preparator
```

## Run

```
docker run --rm -v /tmp/files:/workspace -t ignishky/fma_preparator /workspace <yourToken> <version>
```

All archives would be keep, so you would need somme places.