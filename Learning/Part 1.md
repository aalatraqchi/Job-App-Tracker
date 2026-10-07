# Learning

## How we will work
This is a working portfolio project and a Java/Spring Boot refresher. We will introduce only the concepts needed by the current feature, explain why they exist using our actual code, and build in small increments. When a task uses concepts already covered, I will attempt it first; feedback starts with hints before a complete solution. These notes stay organized by concept, while [[Job Apps Tracker/Changes]] records dated progress and [[Job Apps Tracker/Project Outline]] tracks milestones.

Starter learning checkpoint complete: capability checklists completed, /health mapping explained, random test ports understood, and /about plus its separate integration test implemented by the user and reviewed. The user reported two passing tests. Step 3 is next and has not started.

## 1. Project structure and essential tools

```text
E:\Codex\Job App Tracker\
├── pom.xml                         Maven build/dependency instructions
├── mvnw.cmd / mvnw                 Maven launchers (Windows / Unix)
├── .mvn/wrapper/                   Maven version/download configuration
├── src/main/java/com/ashraf/jobapptracker/
│   ├── JobAppTrackerApplication.java   Java entry point and Spring startup
│   └── HealthController.java          GET /health endpoint
├── src/main/resources/
│   └── application.properties      Runtime configuration
├── src/test/java/com/ashraf/jobapptracker/
│   └── JobAppTrackerApplicationTests.java
├── target/                         Generated classes, reports, executable JAR
├── README.md                       Setup and run instructions
└── AGENTS.md                       Our learning workflow for coding assistance
```

`src/main` contains application code and resources. `src/test` contains verification code, which does not become application functionality. Maven generates `target`; edit source files, not compiled output. The Markdown notes are documentation, not runtime input.

| Tool                         | Why this project needs it                                                                                              |
| ---------------------------- | ---------------------------------------------------------------------------------------------------------------------- |
| JDK 21                       | Includes the compiler (`javac`) and Java runtime. The JVM runs compiled Java bytecode.                                 |
| Maven Wrapper                | Runs a consistent Maven version, which downloads dependencies, compiles, tests, and packages the project.              |
| Spring/Spring Boot libraries | Provide the web framework and configure its infrastructure. They are dependencies, not a separate desktop application. |
| IntelliJ                     | Makes editing, importing Maven, running, and debugging easier. The terminal can build/run without it.                  |

Installed here: JDK 21.0.2 and global Maven 3.9.6. Our wrapper selects Maven 3.9.16; these are separate installations. `JAVA_HOME` points to the JDK folder, not its `bin` folder. No PostgreSQL, separately installed Tomcat, or frontend is needed for the current starter. Internet access is needed to download dependencies initially; cached dependencies usually allow later local work.

**I should be able to...**
- [x] Locate application code, configuration, tests, and build output.
- [x] Explain the difference between the JDK, Maven, Spring Boot, and my IDE.

## 2. Reading pom.xml: dependencies versus imports

The file name means Project Object Model. XML tags here describe a build; they are not Java methods or HTTP headers.

| Existing entry | Meaning and purpose |
| --- | --- |
| `modelVersion` = `4.0.0` | Version of Maven's POM format, not our Spring Boot version. |
| `parent` = `spring-boot-starter-parent:4.1.1` | Inherits dependency versions and build defaults so we do not configure every library separately. |
| `groupId`, `artifactId`, `version` | Project identity: `com.ashraf:job-app-tracker:0.0.1-SNAPSHOT`. SNAPSHOT here labels our unfinished app; the Spring Boot dependency itself is stable. |
| `java.version` = `21` | Sets the Java target for the build; it does not install a JDK. |
| `dependencies` | Libraries available to our code and their transitive dependencies (libraries those libraries need). |
| `scope` = `test` | That dependency is available for testing rather than the running application. |
| `spring-boot-maven-plugin` | Adds Spring Boot build/run tasks and creates the executable application JAR. |

Our Web MVC starter supplies request handling, JSON support, and an embedded Tomcat server. Validation is installed for later input checks, but our health method does not use it yet. The generated test starters supply the testing support used below. Empty entries such as `licenses` and `developers` are optional project metadata.

A **dependency makes a library available**. A Java **import lets this file refer to one of its types by a short name**. Importing `GetMapping` cannot download Spring or start the server.

`mvnw.cmd verify` executes Maven's default lifecycle through verification, including compilation, tests, and packaging with our configuration. `spring-boot:run` is a plugin goal that launches the app and compiles as needed; it is not the same as running the test suite. See [Maven's lifecycle reference](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html).

**I should be able to...**
- [x] Distinguish adding a dependency from adding an import.
- [x] Explain why build/test and run are separate commands.

## 3. Java file declarations: packages, imports, and methods

Our two application files begin with:

```java
package com.ashraf.jobapptracker;
```

This is the class's namespace. The directory under `src/main/java` mirrors it. Its full name is, for example, `com.ashraf.jobapptracker.HealthController`. The package does not create a website or require owning a domain.

In `HealthController.java`:

```java
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
```

`Map` comes with Java; the annotations come from Spring dependencies. Imports let us write `Map` instead of `java.util.Map`. Types in `java.lang`, such as `String`, are automatically available. Classes in our own package also need no import to refer to each other.

The method declaration `public Map<String, String> health()` means a publicly accessible method named `health`, taking no arguments and returning a map with string keys and string values. The two type arguments describe the Java data structure; they are not JSON syntax. `Map.of(...)` constructs an unmodifiable map, appropriate for this small fixed response. JSON property order is not guaranteed.

**I should be able to...**
- [x] Identify a class's package and where its imported types come from.
- [x] Read a method's name, parameters, and return type.

## 4. Annotations and Spring-managed objects

The `@...` lines are **annotations**: metadata interpreted by tools or frameworks. Their behavior depends on who reads them. These Spring annotations let us declare behavior instead of writing server wiring ourselves.

| In our code | Why it exists |
| --- | --- |
| `@SpringBootApplication` above the main class | Enables configuration, auto-configuration, and component scanning. |
| `@RestController` above `HealthController` | Lets Spring discover/manage this controller and write handler return values into the HTTP response body. |
| `@GetMapping("/health")` above `health()` | Registers this method as the handler for GET requests at `/health`. |

Spring keeps managed objects called **beans** in its application context. At startup, scanning from `com.ashraf.jobapptracker` finds the controller in that package (and would find components in its subpackages). Spring creates the controller object and registers its route. That is why the main class does not contain `new HealthController()` or call `health()` directly. Keep future feature packages below this root unless deliberately configuring scanning.

Auto-configuration uses the available dependencies and properties to configure infrastructure. Because the Web MVC starter is present, this app gets an embedded server and request handling. We will cover dependency injection when one project class actually needs another.

**I should be able to...**
- [x] Explain why merely importing an annotation is different from applying it.
- [x] Explain how Spring finds our controller without a manual constructor call.
- [x] Predict which method handles a given GET path.

## 5. How startup and a browser request connect

Our Java entry point is:

```java
public static void main(String[] args) {
    SpringApplication.run(JobAppTrackerApplication.class, args);
}
```

The Java runtime invokes `main`. It is `static`, so it can run without first constructing an application object. `args` holds command-line arguments. Passing `JobAppTrackerApplication.class` gives Spring the class to use as its configuration starting point; `run` creates the context, discovers components, and starts the server.

```text
Startup:
Java main → SpringApplication.run → context/configuration/scanning → Tomcat listens

Request:
browser GET /health → Tomcat → Spring MVC dispatcher → HealthController.health()
                   → returned Map → JSON message converter → HTTP response → browser
```

The current handler returns:

```java
return Map.of("status", "UP", "application", "job-app-tracker");
```

Our method returns a Java object; Spring's configured JSON support serializes it into response text. It is not writing an HTML page. The browser displays the backend's JSON because no UI exists yet. This controller and conversion pattern is described in [Spring's REST guide](https://spring.io/guides/gs/rest-service/).

An HTTP response has a status, headers, and body. Here the normal status is **200 OK**, `Content-Type: application/json` identifies the body format, and the body contains our map entries. These **HTTP headers** are different from Java imports and annotations. `/health` is a small reachability check; its hard-coded UP does not prove future database or application features work.

**I should be able to...**
- [x] Trace `/health` from the browser to the Java method and back.
- [x] Explain why the response becomes JSON without a hand-written JSON string.

## 6. Localhost: starting, viewing, editing, and stopping

`http://localhost:8080/health` means HTTP protocol, this computer, port 8080, and route `/health`. Our configuration binds to `127.0.0.1` (IPv4 loopback); use `http://127.0.0.1:8080/health` if localhost resolution causes trouble. Port 8080 is the default; no `server.port` property is required.

In `application.properties`, `spring.application.name` names the app, including in logs; it does not set a URL. `server.address=127.0.0.1` restricts this starter to local connections. Boot reads these properties on startup.

**Open in IntelliJ:** Open this folder's `pom.xml` as a project, select JDK 21, and allow Maven import to finish. Open `JobAppTrackerApplication.java`, then run `main` with the green triangle. Use IntelliJ's Stop button to stop that process. The README has the detailed Maven settings if needed.

**Or use PowerShell:**

```powershell
Set-Location 'E:\Codex\Job App Tracker'
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
.\mvnw.cmd spring-boot:run
```

Wait for the log saying the app started, then open `/health` in Chrome. Leave that terminal running. Stop it with **Ctrl+C in the same terminal**. Use one launch method at a time. Closing the browser tab does not stop the Java server. Opening a URL does not start it, either.

There is no DevTools hot reload configured: after a Java edit, stop, rerun, and refresh the browser. An already built JAR also keeps its old code until rebuilt. To run a packaged version instead:

```powershell
.\mvnw.cmd verify
java -jar .\target\job-app-tracker-0.0.1-SNAPSHOT.jar
```

| Symptom                    | What to check                                                                                                                 |
| -------------------------- | ----------------------------------------------------------------------------------------------------------------------------- |
| Connection refused         | No server is listening at that address/port, or startup failed. Read the launch logs.                                         |
| HTTP 404 at `/`            | Expected: we only mapped `/health`, not a home page.                                                                          |
| Port 8080 already in use   | An existing process owns it. Stop your previous app instance in its owning terminal/IDE; do not kill an unidentified process. |
| Old response after editing | Restart the app and refresh; rebuild if running the JAR.                                                                      |
| Imports red in IntelliJ    | Check Maven import and JDK selection; editing the code may not be the problem.                                                |

If you deliberately need another port:

```powershell
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--server.port=8081'
```

Then visit port 8081. `verify` finishes and exits; it does not leave a development server running.

**I should be able to...**
- [x] Start and stop the server myself, independently of the browser.
- [x] Explain the host, port, and route in the URL.
- [x] Restart after an edit and distinguish connection failure from HTTP 404.

## 7. What our integration test proves

`JobAppTrackerApplicationTests` starts the real web app on an available random port, rather than competing with development port 8080. The operating system chooses an available port; `@LocalServerPort` supplies it to the test URL. If development uses 8080 and the test uses 53124, the test request reaches 53124. This prevents an occupied development port from making correct tests fail.

- `@SpringBootTest(...RANDOM_PORT)` requests a running Spring test application and server.
- `@LocalServerPort` injects the chosen server port into the `port` field.
- `@Test` tells JUnit this method is a test.
- Java's `HttpClient`, `HttpRequest`, `HttpResponse`, and `URI` send a real GET and read its response.
- The `import static ...assertEquals/assertTrue` declarations let us call JUnit assertions without qualifying their class name.
- Assertions check HTTP 200, JSON content type, and the expected two fields. A mismatch fails the test/build.

There are now separate tests for `/health` and `/about`. Each checks its own path, HTTP 200, JSON content type, and expected response fields. Keeping both tests preserves coverage of both endpoints. The current string checks depend on compact JSON formatting; JSON-aware assertions can be introduced later when needed. This tests the wiring from server through controller to response, not just a direct Java method call. It does not yet verify any tracking feature, database, or authentication. The test runner closes this test server afterward. Use `.\mvnw.cmd test` for tests, or `verify` for tests plus packaging and any configured verification.

**I should be able to...**
- [x] Explain why a random test port is useful.
- [x] Describe what the current test checks and what it cannot prove.

## 8. My first practice checkpoint

Do these in order; no application-tracking implementation yet.

1. Open the two application classes and explain, in my own words, what starts the server and what handles `/health`.
2. Start the app, visit `/health`, then visit `/`. Observe the difference. Stop the app and reload `/health` to observe the difference between an HTTP response and having no server.
3. Restart and confirm `/health` works again.
4. **Small coding attempt:** In the existing `HealthController`, add a separate GET endpoint at `/about`. Return a map containing `project` = `Job Application Tracker` and `purpose` = `Relearning Java and Spring Boot`. Keep `/health` unchanged. Use the existing method as the pattern; no new class or dependency is needed.
5. Restart, check `/about`, then add a separate `/about` integration test using the existing test pattern. Keep the `/health` test. Run `.\mvnw.cmd test`; expected result: two tests, zero failures/errors. Completed and reported passing by the user.

If stuck, identify three decisions first: the URL annotation, a distinct Java method name, and the data to return. Share my attempted method and any error before asking for the finished implementation. Review starts with hints. This exercise and its separate test have now been reviewed; step 3 is the next learning milestone.

- [x] I have demonstrated startup/shutdown and explained the request path.
- [x] I have attempted `/about` and reviewed it with assistance.

Unchecked boxes reflect my own learning progress, not what the assistant already implemented.
