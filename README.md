# Blossom

## Source level comparison of two jars

Blossom is a tool for comparing differences between two jars at the source level. It achieves this by decompiling
classes within each jar.

Blossom generates a single patch as the result of two jars being compared. The patch is in unified format.

### Build Jar

```
mvn clean package
```

### Usage

```
java -jar blossom.jar original.jar revised.jar
```

Everything Blossom saves to disk will be output into a directory named `blossom-output` within its working directory.

### License

Blossom itself is licensed MIT. Blossom dependencies retain their original license and copyright.