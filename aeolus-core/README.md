## 🌀 Aeolus Core

Lightweight, Modular, and JSR-330 Compatible Dependency Injection Framework

Aeolus Core is a blazing-fast, zero-dependency, reflection-based dependency injection (DI) and lifecycle management framework.
It’s designed for framework builders, backend systems, game engines, and microservices that need the power of dependency injection — without the overhead of Spring or Guice.

### Key Features
| **Category**                     | **Capability**                                                                             |
| -------------------------------- | ------------------------------------------------------------------------------------------ |
| **Dependency Injection**         | Constructor, field, and setter injection via `@Inject`                                     |
| **JSR-330 & JSR-250 Compatible** | Works with `@Inject`, `@Named`, `@Singleton`, `@PostConstruct`, `@PreDestroy`, `@Resource` |
| **Scopes**                       | `@Scope("singleton")`, `@Scope("prototype")`, and `@Scope("thread")`                       |
| **Configuration Binding**        | `@Config(prefix="db")` binds strongly typed POJOs from `application.properties`            |
| **Resource Injection**           | Inject property values directly using `@Resource(name="key")`                              |
| **Bean Lifecycle Hooks**         | `@PostConstruct` and `@PreDestroy` for startup and cleanup logic                           |
| **Bean Processors**              | Register pre/post initialization interceptors via `BeanProcessor`                          |
| **Manual Creation**              | `container.create(Class<T>)` for on-demand injection of external objects                   |
| **Introspection**                | `container.stats()` for runtime diagnostics                                                |
| **Rich Exception Hierarchy**     | Clear, typed errors for creation, resource, and circular dependency issues                 |
| **Scopes Management**            | `ScopeManager` handles multi-context lifetimes                                             |
| **Zero External Dependencies**   | Lightweight, plain Java — perfect for embedded or serverless systems                       |

### 🧱 Core Components
| **Component**              | **Description**                                                       |
| -------------------------- | --------------------------------------------------------------------- |
| **Container**              | The DI runtime: creates, injects, and manages beans.                  |
| **ScopeManager**           | Manages object lifetimes for singleton, prototype, and thread scopes. |
| **BeanProcessor**          | Provides extension hooks for pre/post initialization.                 |
| **PropertyBinder**         | Binds `@Config` classes from property files.                          |
| **Logger / ConsoleLogger** | Simple pluggable logging abstraction.                                 |
| **exceptions.***           | Clear, typed exceptions for better debugging.                         |
| **annotations.***          | Lightweight annotations for components, scopes, and config mapping.   |

## 🚀 Quick Start
### 1️⃣ Add Dependencies
```xml
<dependencies>
    <dependency>
        <groupId>com.aeolus</groupId>
        <artifactId>aeolus-core</artifactId>
        <version>1.0.0</version>
    </dependency>
    <dependency>
        <groupId>jakarta.inject</groupId>
        <artifactId>jakarta.inject-api</artifactId>
        <version>2.0.1</version>
    </dependency>
    <dependency>
        <groupId>jakarta.annotation</groupId>
        <artifactId>jakarta.annotation-api</artifactId>
        <version>2.1.1</version>
    </dependency>
</dependencies>
```

### 2️⃣ Define Your Components
```java
package com.app.example;

import com.aeolus.core.di.annotations.Component;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

@Component
public class GreetingService {
    @Inject MessageFormatter formatter;

    @PostConstruct
    void init() { System.out.println("GreetingService initialized!"); }

    public void greet(String name) {
        System.out.println(formatter.format("Hello, " + name));
    }
}

@Component
class MessageFormatter {
    public String format(String msg) { return "[Formatted] " + msg; }
}
```

### 3️⃣ Build the Container
```java
package com.app;

import com.aeolus.core.di.Container;
import com.app.example.GreetingService;

public class App {
    public static void main(String[] args) {
        try (Container container = Container.builder()
                .scan("com.app.example")
                .loadProperties("application.properties")
                .build()) {

            GreetingService svc = container.get(GreetingService.class);
            svc.greet("Aeolus");

            System.out.println("Stats: " + container.stats());
        }
    }
}
```

### ✅ Output:
```
GreetingService initialized!
[Formatted] Hello, Aeolus
Stats: {bindings=2, beans=0, named=0, managed=2, properties=3, processors=0, memory.used.mb=22}
```

### 💾 Scopes Example
```java
@Component
@Scope("prototype")
public class PrototypeService {
    public void ping() { System.out.println("New instance: " + this.hashCode()); }
}
```

```java
PrototypeService a = container.get(PrototypeService.class);
PrototypeService b = container.get(PrototypeService.class);
a.ping();  // Different hashCode
b.ping();
```

### ⚙️ Config Binding Example
```properties
db.url=jdbc:mysql://localhost:3306/app
db.user=root
db.password=secret
```
**DbConfig.java**
```java
@Config(prefix = "db")
public class DbConfig {
    public String url;
    public String user;
    public String password;
}
```

**Usage**
```java
DbConfig cfg = container.get(DbConfig.class);
System.out.println(cfg.url); // jdbc:mysql://localhost:3306/app
```

### 🔧 Resource Injection Example**
```java
@Component
public class EnvInfo {
    @Resource(name = "env.mode")
    private String mode;

    public void print() {
        System.out.println("Running in " + mode + " mode");
    }
}
```
**application.properties**
```properties
env.mode=development
```

### 🧩 Bean Processor Example
```java
public class LoggingProcessor implements BeanProcessor {
    public Object postProcessBeforeInitialization(Object bean) {
        System.out.println("[BeforeInit] " + bean.getClass().getSimpleName());
        return bean;
    }
    public Object postProcessAfterInitialization(Object bean) {
        System.out.println("[AfterInit] " + bean.getClass().getSimpleName());
        return bean;
    }
}
```

**Register it:**
```java
Container c = Container.builder()
    .scan("com.app.example")
    .addProcessor(new LoggingProcessor())
    .build();
```

### 🧠 Exception Hierarchy
| Exception                     | Description                      |
| ----------------------------- | -------------------------------- |
| `AeolusException`             | Base type for all Aeolus errors  |
| `BeanCreationException`       | Failed to create or inject bean  |
| `CircularDependencyException` | Dependency loop detected         |
| `ResourceMissingException`    | Missing property for `@Resource` |

### 📊 Container Introspection
```java
System.out.println(container.stats());
```
**Example Output:**
```json
{
  "bindings": 14,
  "beans": 3,
  "named": 2,
  "managed": 25,
  "properties": 15,
  "processors": 1,
  "memory.used.mb": 26
}
```

### 🔩 Manual Bean Creation
For external objects or plugin systems:
```java
MyHandler handler = container.create(MyHandler.class);
```
Aeolus injects all dependencies without registering the class globally.


### 🧭 Design Philosophy
| Principle                         | Description                                      |
| --------------------------------- | ------------------------------------------------ |
| **Convention over configuration** | Smart defaults, minimal annotations              |
| **Framework-agnostic**            | Works standalone or embedded in any app          |
| **Lightweight**                   | Zero third-party runtime dependencies            |
| **Fast**                          | Scans packages once, minimal reflection overhead |
| **Transparent**                   | Explicit logs, no hidden magic                   |


## 🧩 Aeolus Core — Bean Lifecycle Diagram

```text
 ┌──────────────────────────────────────────────────────────┐
 │                     APPLICATION STARTS                   │
 └──────────────────────────────────────────────────────────┘
                   │
                   ▼
        ┌──────────────────────┐
        │ 1. CONTAINER BUILD   │
        └──────────────────────┘
                   │
                   │  Container.builder()
                   │     ├─ scan("package")
                   │     ├─ loadProperties("application.properties")
                   │     └─ build()
                   │
                   ▼
        ┌─────────────────────────────┐
        │ 2. COMPONENT DISCOVERY      │
        └─────────────────────────────┘
                   │
                   │  → Scan all packages for:
                   │      @Component, @Configuration, @Config
                   │  → Register bindings:
                   │      Interface → Implementation
                   │      Named beans (via @Named)
                   │
                   ▼
        ┌─────────────────────────────┐
        │ 3. BEAN RESOLUTION REQUEST  │
        └─────────────────────────────┘
                   │
                   │  container.get(MyService.class)
                   │
                   ▼
        ┌─────────────────────────────┐
        │ 4. CONSTRUCTOR INJECTION    │
        └─────────────────────────────┘
                   │
                   │  → Select @Inject constructor
                   │  → Resolve dependencies recursively
                   │  → Detect circular dependencies
                   │
                   ▼
        ┌─────────────────────────────┐
        │ 5. FIELD / SETTER INJECTION │
        └─────────────────────────────┘
                   │
                   │  → Inject @Inject fields & setters
                   │  → Inject @Resource(name="key") from properties
                   │  → Bind @Config(prefix) POJOs from property file
                   │
                   ▼
        ┌─────────────────────────────┐
        │ 6. BEAN PROCESSORS          │
        └─────────────────────────────┘
                   │
                   │  → Apply BeanProcessor.beforeInit(bean)
                   │  → Invoke @PostConstruct methods
                   │  → Apply BeanProcessor.afterInit(bean)
                   │
                   ▼
        ┌─────────────────────────────┐
        │ 7. SCOPE MANAGEMENT         │
        └─────────────────────────────┘
                   │
                   │  → Store in ScopeManager (singleton/prototype/thread)
                   │  → Return resolved instance
                   │
                   ▼
        ┌─────────────────────────────┐
        │ 8. RUNTIME USAGE            │
        └─────────────────────────────┘
                   │
                   │  Application code runs using injected beans
                   │
                   ▼
        ┌─────────────────────────────┐
        │ 9. CONTAINER SHUTDOWN       │
        └─────────────────────────────┘
                   │
                   │  → container.close()
                   │  → Invoke all @PreDestroy hooks
                   │  → Release resources
                   │
                   ▼
 ┌──────────────────────────────────────────────────────────┐
 │                      APPLICATION ENDS                    │
 └──────────────────────────────────────────────────────────┘
```
## 🧩 Aeolus Core — Architecture Overview

```text
 ┌───────────────────────────────────────────────────────────┐
 │                       Aeolus Core                         │
 │         (Lightweight Dependency Injection Kernel)          │
 └───────────────────────────────────────────────────────────┘
                  │
                  ▼
       ┌─────────────────────┐
       │     Container       │
       │─────────────────────│
       │ • Main DI runtime   │
       │ • Manages beans,    │
       │   scopes, lifecycle │
       │ • Entry point for   │
       │   .scan(), .get(),  │
       │   .create(), .close()│
       └────────┬────────────┘
                │
                │ uses
                ▼
 ┌──────────────────────────────┐
 │     ComponentScanner         │
 │──────────────────────────────│
 │ • Scans packages recursively │
 │   for @Component, @Config,   │
 │   @Configuration classes     │
 │ • Registers bindings         │
 └──────────────┬───────────────┘
                │
                │
                ▼
 ┌──────────────────────────────┐
 │       ScopeManager           │
 │──────────────────────────────│
 │ • Controls bean lifetimes    │
 │   (singleton/prototype/thread)│
 │ • Stores scoped instances    │
 │ • Prevents recursive creation│
 └──────────────┬───────────────┘
                │
                │
                ▼
 ┌──────────────────────────────┐
 │      PropertyBinder          │
 │──────────────────────────────│
 │ • Handles @Config(prefix)    │
 │   binding from properties    │
 │ • Converts property values   │
 │   into typed fields          │
 └──────────────┬───────────────┘
                │
                │
                ▼
 ┌──────────────────────────────┐
 │        BeanProcessor         │
 │──────────────────────────────│
 │ • Provides lifecycle hooks   │
 │   before/after init          │
 │ • Used for logging, AOP, etc.│
 │ • Plugged via builder.addProcessor() │
 └──────────────┬───────────────┘
                │
                │
                ▼
 ┌──────────────────────────────┐
 │         Logger API           │
 │──────────────────────────────│
 │ • ConsoleLogger by default   │
 │ • Pluggable custom logger    │
 │   for frameworks or tests    │
 └──────────────┬───────────────┘
                │
                │
                ▼
 ┌──────────────────────────────┐
 │      Exception Classes       │
 │──────────────────────────────│
 │ • AeolusException            │
 │ • BeanCreationException      │
 │ • CircularDependencyException│
 │ • ResourceMissingException   │
 │ • Clear stack traces & causes│
 └──────────────┬───────────────┘
                │
                │
                ▼
 ┌──────────────────────────────┐
 │        Annotations           │
 │──────────────────────────────│
 │ • @Component / @Configuration│
 │ • @Scope / @Config / @Resource│
 │ • @PostConstruct / @PreDestroy│
 │ • Used by scanner & binder   │
 └──────────────────────────────┘
```

### 📦 Module Roadmap
| Module              | Purpose                                                     |
| ------------------- | ----------------------------------------------------------- |
| **aeolus-core**     | Dependency injection and lifecycle kernel                   |
| **aeolus-context**  | Application context, events, async lifecycle                |
| **aeolus-web**      | Netty / WebSocket integration with DI                       |
| **aeolus-plugin**   | Hot-reloadable plugin system                                |
| **aeolus-compiler** | Compile-time component indexing for reflection-less startup |
