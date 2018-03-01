# Sequence Java SDK

## Usage

### Get the jar

The Sequence SDK is available [via
maven](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.seq%22).
This library requires Java 8 or newer.

Add the following to your `pom.xml`:

```xml
<dependency>
  <groupId>com.seq</groupId>
  <artifactId>sequence-sdk</artifactId>
  <version>[1.3,2)</version>
</dependency>
```

### In your code

```java
import com.seq.api.*;
import com.seq.http.*;

...

Client ledger =
    new Client.Builder()
        .setLedgerName("my-ledger")
        .setCredential("...")
        .build();
```

### Documentation

Comprehensive instructions and examples are available in the
[developer documentation](https://dashboard.seq.com/docs).
