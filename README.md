# Enrichable

Java exceptions were starting to drive me a little crazy.

A message, a stack trace, maybe a cause... and when an error gets a little more complicated, things can get messy pretty quickly.

So I decided to make them a bit more organized.

**Enrichable** is a small Java library for creating exceptions that can carry more useful information, metadata, error levels, and configuration without turning the code into a mess.

It's still a work in progress, but it's slowly becoming something useful.

---

## Project Structure

```text
src/main/java/com/enrichable/
│   EnrichableException.java        ← Core exception — carries errors, metadata, config and cause
│   Main.java                       ← Entry point / usage examples
│
├── annotation/
│   ├── AnnotationProcessor.java    ← Reads @EnrichableHandler and @EnrichableCode via reflection
│   ├── EnrichableCode.java         ← Annotation for custom exception classes (code + level)
│   └── EnrichableHandler.java      ← Annotation for service classes (context + defaultLevel)
│
├── config/
│   ├── ConsoleConfig.java          ← Controls console output formatting
│   ├── ErrorLevel.java             ← Enum — INFO / WARNING / ERROR / CRITICAL
│   └── LogConfig.java              ← Controls file logging behavior and formatting
│
├── formatter/
│   ├── EnrichFormatter.java        ← Interface for formatting exception output
│   └── DefaultEnrichFormatter.java ← Default implementation of EnrichFormatter
│
├── logging/
│   └── FileEnrichLogger.java       ← Thread-safe file logger
│
├── model/
│   └── EnrichInformation.java      ← Model for a single error entry
│
├── registry/
│   └── ErrorRegistry.java          ← Generates and stores unique error codes
│
└── validation/
    └── EnrichValidator.java        ← Validates and normalizes input values
```

---

## Features

* Multiple error information entries
* Builder API for readable exception creation
* Error levels
* Custom metadata
* Configurable console output
* Configurable file logging
* Optional timestamps
* Optional error count
* Optional metadata output
* Exception cause preservation
* Thread-safe file logging
* Error-level log filtering
* Custom log file paths
* Optional log file clearing
* Input validation
* JUnit 5 tests
* Annotation-based exception creation
* Thread-safe exception building
* Unique error code generation per exception session
* Error code registry with lookup support

---

## Requirements

* Java 21+
* Maven 3+

---

## Installation

Clone the repository:

```bash
git clone https://github.com/arsam4waffels/enrichable.git

cd enrichable
```

Run the tests:

```bash
mvn test
```

---

## Quick Start

Create an `EnrichableException` using the Builder API:

```java
EnrichableException exception =
        new EnrichableException.Builder(
                "DATABASE",
                "Database connection failed"
        )
        .code("DB-001")
        .level(ErrorLevel.CRITICAL)
        .cause(new IllegalStateException("Connection refused."))
        .build();

System.out.println(exception);
```

The Builder requires:

```text
context
message
```

The following options are optional:

```text
code
level
cause
```

If no error level is specified, `ErrorLevel.ERROR` is used by default.

For example, a minimal exception can be created with only the required fields:

```java
EnrichableException exception =
        new EnrichableException.Builder(
                "DATABASE",
                "Database connection failed"
        )
        .build();
```

By default, the exception output includes the available timestamp, error level, error count, and metadata information.

---

## Builder API

The Builder API provides a readable way to create `EnrichableException` instances without relying on a long constructor.

```java
EnrichableException exception =
        new EnrichableException.Builder(
                "DATABASE",
                "Database connection failed"
        )
        .code("DB-001")
        .level(ErrorLevel.CRITICAL)
        .cause(new IllegalStateException("Connection refused."))
        .build();
```

The Builder supports the following fields:

| Field     | Required | Default |
| --------- | -------- | ------- |
| `context` | Yes      | —       |
| `message` | Yes      | —       |
| `code`    | No       | `null`  |
| `level`   | No       | `ERROR` |
| `cause`   | No       | `null`  |

Each Builder method returns the same Builder instance, allowing method chaining.

For example:

```java
EnrichableException exception =
        new EnrichableException.Builder(
                "PAYMENT",
                "Payment processing failed"
        )
        .code("PAY-001")
        .level(ErrorLevel.ERROR)
        .build();
```

---

## Adding Information

Sometimes one error isn't enough.

You can attach additional information to the same exception:

```java
exception.addInformation(
        "AUTH_SERVICE",
        "AUTH-001",
        "Authentication failed",
        ErrorLevel.WARNING
);
```

This can be useful when several related things go wrong during the same operation.

Each `EnrichInformation` entry keeps its own context, code, message, level, timestamp, and metadata.

---

## Metadata

You can attach extra key-value information to the latest error entry:

```java
exception
        .addMetadata("userId", "1042")
        .addMetadata("query", "SELECT * FROM users")
        .addMetadata("retryCount", "3");
```

Metadata belongs to the specific error information it was added to.

For example, if an exception contains multiple error entries, metadata added after one entry does not leak into the others.

Whether metadata appears in the console output is controlled by `ConsoleConfig`.

```java
exception.setConsoleConfig(
        new ConsoleConfig()
                .showMetadata(true)
);
```

---

## Console Configuration

`ConsoleConfig` controls what information is included in the formatted console representation of the exception.

```java
ConsoleConfig configuration =
        new ConsoleConfig()
                .showTimestamp(true)
                .showErrorLevel(true)
                .showErrorCount(true)
                .showMetadata(true);

exception.setConsoleConfig(configuration);
```

Currently available options:

| Option           | What it does                           | Default |
| ---------------- | -------------------------------------- | ------- |
| `showTimestamp`  | Shows timestamps for error information | `true`  |
| `showErrorLevel` | Shows the error level                  | `true`  |
| `showErrorCount` | Shows the total number of errors       | `true`  |
| `showMetadata`   | Shows metadata attached to errors      | `true`  |

The console output is intentionally detailed by default.

---

## Logging

Sometimes printing an exception to the console isn't enough.

`EnrichableException` can write a formatted exception report to a file using:

```java
exception.writeLog();
```

By default, the log file is:

```text
enrichable.log
```

The generated report keeps each error and its metadata together, so things don't turn into a wall of random error messages.

Example:

```text
════════════════════════════════════════════════════
  ENRICHABLE EXCEPTION REPORT
  Total Errors : 2
  Thrown At    : 2026-08-27 11:58:37
════════════════════════════════════════════════════

  [ERROR-1] [CRITICAL] [DATABASE:DB-001]
  Failed to execute query: table 'users' not found
    └─ Time : 2026-08-27T11:58:37
    └─ retryCount : 3
    └─ query : SELECT * FROM users
    └─ userId : 1042

  [ERROR-2] [WARNING] [DATABASE-SIZE:DB-002]
  Failed to execute query: table 'users-info' not found
    └─ Time : 2026-08-27T11:58:37
```

Each call to `writeLog()` writes another report to the configured log file.

File logging is thread-safe, so concurrent exceptions can safely write to the shared log file without interleaving their reports.

---

## Log Configuration

File logging can be configured independently from console output using `LogConfig`.

```java
LogConfig logConfig =
        new LogConfig()
                .showTimestamp(true)
                .showErrorLevel(true)
                .showMetadata(true)
                .filePath("application.log");

exception.setLogConfig(logConfig);
exception.writeLog();
```

`LogConfig` currently provides the following options:

| Option             | What it does                            | Default                   |
|--------------------|-----------------------------------------|---------------------------|
| `showTimestamp`    | Shows timestamps in the log report      | `true`                    |
| `showErrorLevel`   | Shows error levels                      | `true`                    |
| `showMetadata`     | Shows metadata                          | `true`                    |
| `filePath`         | Changes the log file path               | `enrichable.log`          |
| `clearBeforeWrite` | Clears the existing file before writing | `false`                   |
| `generateCode`     | Generate a unique code for each session | `false`                   |
| `clearBeforeWrite` | Change the registery file path          | `enrichable-registry.log` |

Console and logging configuration are independent.

For example:

```java
exception.setConsoleConfig(
        new ConsoleConfig()
                .showMetadata(false)
);

exception.setLogConfig(
        new LogConfig()
                .showMetadata(true)
);
```

This hides metadata from console output while keeping it in the log file.

---

## Error Code Registry

Sometimes you need more than a log file.

When `generateCode` is enabled, each call to `writeLog()` generates a unique
6-character code for that exception session and stores the full report in a
separate registry file.

```java
exception.setLogConfig(
        new LogConfig()
                .generateCode(true)
);

String code = exception.writeLog();
System.out.println("Error code: " + code); // af45cb
```

The code is derived from the exception content and timestamp using SHA-256,
so each run produces a unique code even when the errors are identical.

---

### Registry File

By default, the registry is written to:

```text
enrichable-registry.log
```

You can change this with `registryPath()`:

```java
exception.setLogConfig(
        new LogConfig()
                .generateCode(true)
                .registryPath("logs/registry.log")
);
```

---

### Registry File Structure

Each entry in the registry starts with a `[CODE: xxxxxx]` marker followed
by the full exception report:

```text
[CODE: af45cb]
════════════════════════════════════════════════════
  ENRICHABLE EXCEPTION REPORT
  Total Errors : 3
  Thrown At    : 2026-09-06 14:23:01
════════════════════════════════════════════════════

  [ERROR-1] [CRITICAL] [DATABASE:DB-001]
  Connection failed
    └─ Time : 2026-09-06 14:23:01

[CODE: 3d9f12]
════════════════════════════════════════════════════
...
```

---

### Lookup

You can retrieve any previously logged report by its code:

```java
LogConfig config = new LogConfig()
        .generateCode(true);

ErrorRegistry.getInstance()
        .lookup("af45cb", config)
        .ifPresent(System.out::println);
```

`lookup()` returns an `Optional<String>` — empty if the code does not exist
or the registry file cannot be read.

---

### How Codes Are Generated

Each code is the first 6 characters of a SHA-256 hash computed from:

* The content of all error entries (context, code, message, level)
* The exception timestamp

The timestamp is included so that two runs with identical errors still produce
different codes. 6 hex characters yield ~16 million possible values, which is
sufficient for a local error registry.

This is a readability feature, not a security guarantee.

---

## Log-Level Filtering

`LogConfig` can filter which errors are written to the log file.

### `onlyLevel()`

Use `onlyLevel()` when you want to log only one specific error level:

```java
exception.setLogConfig(
        new LogConfig()
                .onlyLevel(ErrorLevel.CRITICAL)
);

exception.writeLog();
```

For example, with:

```java
.onlyLevel(ErrorLevel.ERROR)
```

only `ERROR` entries are written to the log.

---

### `minimumLevel()`

Use `minimumLevel()` when you want to log a level and everything more severe than it.

```java
exception.setLogConfig(
        new LogConfig()
                .minimumLevel(ErrorLevel.ERROR)
);

exception.writeLog();
```

With the current error-level ordering:

```text
INFO
WARNING
ERROR
CRITICAL
```

the configuration:

```java
.minimumLevel(ErrorLevel.ERROR)
```

logs:

```text
ERROR      ✓
CRITICAL   ✓
WARNING    ✗
INFO       ✗
```

`onlyLevel()` and `minimumLevel()` are mutually exclusive. Setting one clears the other.

---

## Custom Log File

You can change the destination of the log file:

```java
exception.setLogConfig(
        new LogConfig()
                .filePath("application.log")
);

exception.writeLog();
```

This allows different applications or environments to use their own log file names.

---

## Clearing Previous Logs

By default, writing to a log file appends the new report:

```java
new LogConfig()
        .clearBeforeWrite(false);
```

If you want the existing file to be cleared before writing:

```java
exception.setLogConfig(
        new LogConfig()
                .clearBeforeWrite(true)
);

exception.writeLog();
```

This is useful when a fresh report is preferred instead of an accumulated log file.

---

## Error Levels

There are currently four levels:

```java
ErrorLevel.INFO
ErrorLevel.WARNING
ErrorLevel.ERROR
ErrorLevel.CRITICAL
```

For example:

```java
EnrichableException exception =
        new EnrichableException.Builder(
                "PAYMENT",
                "Payment processing failed"
        )
        .code("PAY-001")
        .level(ErrorLevel.ERROR)
        .build();
```

If no level is explicitly specified, the Builder uses:

```java
ErrorLevel.ERROR
```

---

## Exception Cause

You can pass the original exception as the cause using the Builder API:

```java
IllegalStateException cause =
        new IllegalStateException("Connection refused.");

EnrichableException exception =
        new EnrichableException.Builder(
                "DATABASE",
                "Database operation failed"
        )
        .code("DB-001")
        .level(ErrorLevel.CRITICAL)
        .cause(cause)
        .build();
```

The original cause is preserved and can still be retrieved normally:

```java
exception.getCause();
```

---

## Annotations

Instead of repeating context, codes, and levels every time, you can define them once on the class itself.

`@EnrichableHandler` goes on service classes:

```java
@EnrichableHandler(
        context = "Database",
        defaultLevel = ErrorLevel.CRITICAL
)
public class DatabaseService {

    public void connect() {
        throw AnnotationProcessor.processHandler(
                DatabaseService.class,
                "DB-001",
                "Connection failed"
        );
    }
}
```

`@EnrichableCode` goes on custom exception classes:

```java
@EnrichableCode(
        code = "DB-001",
        level = ErrorLevel.CRITICAL
)
public class DatabaseConnectionException
        extends EnrichableException {
    // ...
}

throw AnnotationProcessor.processCode(
        DatabaseConnectionException.class,
        "Database",
        "Connection failed"
);
```

The annotated values are picked up automatically.

---

## Validation

The library performs basic validation so invalid information doesn't quietly make its way into an exception.

Required text values such as `context` and `message` cannot be `null` or blank.

The optional `code`, when provided, also cannot be blank.

Metadata also has a few rules:

* `null` keys and values are rejected.
* Blank keys and values are normalized to `BLANK`.

For example:

```java
exception.addMetadata("", "user-6969");
```

becomes:

```text
[BLANK=user-6969]
```

And:

```java
exception.addMetadata("userId", "");
```

becomes:

```text
[userId=BLANK]
```

Configuration values are also validated. For example, null error levels and invalid log file paths are rejected.

---

## Testing

The project uses JUnit 5.

Run all tests with:

```bash
mvn test
```

The current test suite covers:

* Builder API
* Builder defaults and optional fields
* Adding information
* Input validation
* Metadata
* Console configuration
* Log configuration
* Exception causes
* Multiple error information
* Output behavior
* File logging
* Log-level filtering
* Custom log file paths
* Clearing previous logs
* Thread-safe file logging
* Concurrent exception building
* Concurrent metadata writing
* Simultaneous read and write operations
* Concurrent file logging
* Error code generation
* Registry file writing
* Registry lookup by code
* Registry path configuration

---

## Example

Here's a more complete example:

```java
EnrichableException databaseError =
        new EnrichableException.Builder(
                "DATABASE",
                "Failed to execute query: table 'users' not found"
        )
        .code("DB-001")
        .level(ErrorLevel.CRITICAL)
        .cause(new IllegalStateException(
                "Table 'users' does not exist."
        ))
        .build();

databaseError
        .addMetadata("userId", "1042")
        .addMetadata("query", "SELECT * FROM users")
        .addMetadata("retryCount", "3");

databaseError.setConsoleConfig(
        new ConsoleConfig()
                .showTimestamp(true)
                .showErrorLevel(true)
                .showErrorCount(true)
                .showMetadata(true)
);

databaseError.setLogConfig(
        new LogConfig()
                .minimumLevel(ErrorLevel.ERROR)
                .showTimestamp(true)
                .showErrorLevel(true)
                .showMetadata(true)
                .filePath("enrichable.log")
);

System.out.println(databaseError);

databaseError.writeLog();
```

---

## Backward Compatibility

Some older methods are still present for compatibility with earlier versions of the library.

For example:

```java
exception
        .onlyLog(ErrorLevel.CRITICAL)
        .writeLog();
```

is still supported, but `onlyLog()` is deprecated.

The recommended API is now:

```java
exception.setLogConfig(
        new LogConfig()
                .onlyLevel(ErrorLevel.CRITICAL)
);

exception.writeLog();
```

Similarly, the older metadata API remains available while the library evolves.

The Builder API is now the recommended way to create new `EnrichableException` instances because it provides a more readable alternative to the previous long constructor.

Older construction APIs may remain available for compatibility while the library evolves.

---

## Why EnrichableException?

Java already provides `Throwable.addSuppressed()` for attaching additional exceptions to a throwable. That's useful, but it solves a different problem.

| Feature                                 | `Throwable.addSuppressed()`      | `EnrichableException`            |
|-----------------------------------------| -------------------------------- | -------------------------------- |
| Attach another `Throwable`              | Yes                              | Yes, through the exception cause |
| Add structured error information        | No                               | Yes                              |
| Error context                           | No                               | Yes                              |
| Error code                              | No                               | Yes                              |
| Error level                             | No                               | Yes                              |
| Timestamp per error                     | No                               | Yes                              |
| Custom metadata                         | No                               | Yes                              |
| Multiple related error entries          | Limited to suppressed exceptions | Yes                              |
| Configurable output                     | No                               | Yes                              |
| Formatted error report                  | No                               | Yes                              |
| Designed for structured error reporting | No                               | Yes                              |
| Annotation-based exception creation     | No                               | Yes                              |
| Unique error code per session           | No                               | Yes                              |
| Error code registry with lookup         | No                               | Yes                              |

`addSuppressed()` is mainly useful when one operation encounters additional exceptions that should not replace the original exception.

`EnrichableException` is designed for a different job: **making errors carry structured, human-readable context that can be logged and inspected later.**

So this isn't really:

> "Java's way vs. our way."

It's more like:

> `addSuppressed()` tells you what other exceptions happened.
> `EnrichableException` tells you what happened, where, why, how severe it was, and gives you extra context to investigate it.

---

## Project Status

This project is actively evolving as I continue exploring better ways to design and manage exceptions in Java.

It is still a work in progress, and the API and design may change as the project grows.
