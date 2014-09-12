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


### Configuration

Blossom attempts to read a file named `blossom.properties` from its working directory. This file can alter the behavior
of Blossom. Currently it is limited to just excluding certain package names from the unified patch.

```
exclude=gnu.trove,com.google.common,com.mysql.jdbc # Command-separated list of package names to exclude from patch
```

### License

Blossom itself is licensed MIT. Blossom dependencies retain their original license and copyright.