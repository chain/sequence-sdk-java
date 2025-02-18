# Sequence Java SDK

## Usage

### Get the jar

The Sequence SDK is available
[via maven](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.seq%22).
Java 8,
9,
and 10 are supported.

Add the following to your `pom.xml`:

```xml
<dependency>
  <groupId>com.seq</groupId>
  <artifactId>sequence-sdk</artifactId>
  <version>[2.2,3)</version>
</dependency>
```

### In your code

```java
import com.seq.api.*;
import com.seq.http.*;
import com.seq.exception.*;
...

Client ledger =
    new Client.Builder()
        .setLedgerName("my-ledger")
        .setCredential("...")
        .build();
```

### Documentation

Comprehensive instructions and examples are available in the
[developer documentation](https://docs.chain.com/docs/sequence/get-started/introduction).
