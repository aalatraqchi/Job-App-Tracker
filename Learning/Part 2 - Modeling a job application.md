## Goal and current boundary
Start outline step 3 by representing one job application as a Java object. We will extend this part as we learn the remaining concepts needed for in-memory create/list behavior. [[Job Apps Tracker/Learning/Part 1 - Project structure and running the app|Part 1]] covered startup and HTTP endpoints.

Current code: `src/main/java/com/ashraf/jobapptracker/application/JobApplication.java` in `E:\Codex\Job App Tracker`. Company was introduced as a worked example; the user has now correctly added position, extended the constructor, and added getPosition(). The attempt has been reviewed, and the user reported passing tests. ApplicationStatus is now defined as a worked enum example. The user has now added appliedDate (LocalDate) and status (ApplicationStatus), extended the constructor, and added matching getters; the code was reviewed and is correct. Remaining model fields, DTOs, storage, and application endpoints are still ahead. This class is not yet connected to a controller.

## A model gives application data a name and structure
Our `/about` method uses a `Map<String, String>` because its small response is just a few fixed text values. A job application needs a clearer structure: company, position, date, and status have different meanings, and some will have different types.

`JobApplication` names that concept. A **class** defines its structure and behavior; an **object** is one instance containing values for one application. Two objects can represent applications at different companies without overwriting one another's data.

This model starts as plain Java. It needs no Spring annotation: it does not handle requests or run a shared service. Later, the service will create a new object for each application. Spring will manage infrastructure such as the service, rather than treating each application record as a shared bean.

The `application` subpackage groups this feature's code. Its declaration is `package com.ashraf.jobapptracker.application;`. A class in the parent package would import `JobApplication` to refer to it by its short name; Java does not automatically import subpackages.

**I should be able to...**
- [x] Explain the difference between the `JobApplication` class and a particular application object.
- [x] Explain why a dedicated model will be easier to work with than arbitrary map keys.
- [x] Explain why this class has no `@RestController` annotation.

## Fields, constructors, and getters work together

The original company-only worked example was (the current constructor now takes company and position):

```java
public class JobApplication {
    private String company;

    public JobApplication(String company) {
        this.company = company;
    }

    public String getCompany() {
        return company;
    }
}
```

- **Field:** `company` stores data belonging to each instance. It is not `static`, so each object has its own company value.
- **Constructor:** `JobApplication(String company)` runs when creating an object with `new`. It has the same name as the class and no return type. Requiring an argument makes the intended creation pattern explicit; it does not yet validate that the argument is nonblank or non-null.
- **`this.company = company`:** the left side means this object's field; the right side means the constructor argument. It copies the supplied value into the object. Without `this`, `company = company` would assign the parameter to itself and leave the field unset.
- **Getter:** `getCompany()` returns the stored value. It is an ordinary Java method, with no HTTP route. `private` prevents outside code from accessing the field directly; a public getter lets other code read it through an intentional API. We have not added an editing operation yet.

For example, in a method or IntelliJ's Evaluate Expression when an appropriate debugging context is available:

```java
JobApplication application = new JobApplication("Acme", "Java Developer");
String companyName = application.getCompany(); // "Acme"
```

These are illustrative statements, not a new runnable program or endpoint. Creating a Java object does not save it to a database or make it appear at a URL. Those require storage and controller code later.

**I should be able to...**
- [x] Identify the field, constructor argument, and getter in our model.
- [x] Explain what `this` distinguishes in the constructor.
- [x] Create an object and read its company through the getter.
- [x] Explain why constructing an object does not persist it or expose it over HTTP.

## My first model exercise
Extend the existing `JobApplication` class with **position**, using company as the pattern:

1. Add a private string field named `position`.
2. Extend the existing constructor to accept company and position, and assign both to their respective fields. Keep one constructor for now.
3. Add a public `getPosition()` method that returns the position.
4. Keep `getCompany()` working. No annotations, setter, endpoint, or dependency is needed for this exercise.
5. Run `.\mvnw.cmd test` from the project folder. This compiles the new class and runs the existing two endpoint tests. Those tests do not verify model field assignments; we will review your code as well.

Expected usage after my change:

```java
JobApplication application = new JobApplication("Acme", "Java Developer");
// application.getCompany() returns "Acme"
// application.getPosition() returns "Java Developer"
```

Share the class or say when it is ready for review. If it fails, share the error and attempted code; feedback starts with hints rather than a replacement solution.

- [x] I have attempted the position field, constructor update, and getter.
- [x] My attempt has been reviewed and its behavior explained. Both constructor arguments are assigned to their matching fields; each getter returns its matching value. Existing tests passed according to the user; those tests cover endpoints, not model assignments.

Position review complete. Continue below with date and status types. New concept checkboxes and the next exercise remain unchecked until demonstrated.

## Dates: represent the meaning, not just the text
The model will store the date the user applied as `LocalDate`, from Java's built-in `java.time` package. It represents a calendar date with no time of day or time zone. That fits "applied on October 7"; an interview scheduled for a specific time will need a different type later.

```java
import java.time.LocalDate;

// Illustrative statements inside a method:
LocalDate appliedDate = LocalDate.of(2026, 10, 7);
LocalDate sameDate = LocalDate.parse("2026-10-07");
boolean sameDay = appliedDate.equals(sameDate); // true
```

`of` takes year, month, day. `parse` uses ISO year-month-day by default, avoiding the ambiguity of strings such as `10/07/2026`. A `LocalDate` supports date operations and rejects impossible dates, such as February 30, when constructed/parsed. A plain `String` can contain any text until we validate or interpret it.

`LocalDate` is immutable: operations such as `plusDays(1)` return a new date rather than changing the original. Use `.equals(...)` to compare date values; `==` compares object references. These are ordinary Java concepts and need no Spring annotation or new Maven dependency.

For our model, the field will be `private LocalDate appliedDate;`. Follow the existing constructor/getter pattern, changing the type to `LocalDate`. Accept the date as an argument: using `LocalDate.now()` automatically would make it awkward to record an application submitted last week. A valid date type does not decide business rules such as whether future dates are allowed; those rules belong in validation later.

**I should be able to...**
- [x] Explain why an application date uses LocalDate rather than String or a timestamp.
- [x] Create a LocalDate and recognize ISO year-month-day text.
- [x] Explain why a supplied application date may differ from today's date.

## Enums: name the permitted statuses
The new project file `application/ApplicationStatus.java` contains:

```java
package com.ashraf.jobapptracker.application;

public enum ApplicationStatus {
    APPLIED,
    INTERVIEW,
    OFFER,
    REJECTED,
    WITHDRAWN
}
```

An **enum** is a Java type with a fixed set of named instances. Instead of accepting arbitrary text such as `"Applied"`, `"applied"`, or `"INTERVEIW"` in Java code, we use the defined constant:

```java
ApplicationStatus status = ApplicationStatus.APPLIED;
boolean isApplied = status == ApplicationStatus.APPLIED;
```

Here `==` is appropriate: enum constants are unique instances. `ApplicationStatus.INTERVEIW` would not compile because it is not declared. Enum names are conventionally uppercase; the type name follows the class naming convention. The file and model share a package, so JobApplication needs no import for ApplicationStatus.

For the model, use an `ApplicationStatus` field named `status`. The enum defines the allowed values, not which transitions are allowed: it does not decide whether OFFER can change to REJECTED. It also does not prevent `null` or choose a default automatically. We'll handle the default APPLIED when we implement application creation; for this exercise, pass a status explicitly.

**I should be able to...**
- [x] Explain why a status enum is more useful than arbitrary status strings.
- [x] Declare a status using ApplicationStatus.APPLIED.
- [x] Distinguish a fixed set of values from defaults and transition rules.

## My date/status model exercise
Apply the same pattern you used for position. The enum is the worked example; extending JobApplication is your attempt.

1. Import `java.time.LocalDate` into JobApplication.java.
2. Add `appliedDate` of type LocalDate and `status` of type ApplicationStatus as private instance fields.
3. Extend the existing constructor to take company, position, appliedDate, and status (in that order). Assign each argument to its matching field.
4. Add `getAppliedDate()` and `getStatus()`, returning the appropriate types.
5. Keep company and position working. No setter, HTTP mapping, default status, or database annotation yet.
6. Run `.\mvnw.cmd test` and share the class or say when it is ready for review. The existing tests still cover endpoints, not the new field assignments; we will review those separately.

Expected usage **after** your change:

```java
JobApplication application = new JobApplication(
    "Acme",
    "Java Developer",
    LocalDate.of(2026, 10, 7),
    ApplicationStatus.APPLIED
);
// getAppliedDate() returns the October 7, 2026 date value.
// getStatus() returns ApplicationStatus.APPLIED.
```

These statements illustrate calling the future constructor; they are not an extra main method to paste into the model. Earlier two-argument examples document the previous exercise and will no longer match the constructor after this change.

- [x] I have attempted the appliedDate and status fields, constructor, and getters.
- [x] My attempt has been reviewed and its behavior explained. LocalDate is imported, both field types are correct, and constructor assignments/getters match their fields. The parameter named date correctly initializes this.appliedDate; parameter and field names do not have to match.

Before editing, explain in your own words: why is LocalDate a fit for appliedDate, and what does ApplicationStatus protect us from compared with String? Ask about either concept before attempting the code if it is unclear.


Review note: the current constructor is JobApplication(String company, String position, LocalDate date, ApplicationStatus status). The assignment this.appliedDate = date is valid: the right side is the supplied argument, and the left side is the instance field. Existing endpoint tests do not verify these assignments; they were reviewed directly. No application code was replaced.

## Optional text fields: job URL and notes
Our model already has company, position, appliedDate, and status. Add the remaining user-supplied data using the field/constructor/getter pattern you have practiced:

| Field | Type | Meaning |
| --- | --- | --- |
| jobUrl | String | Link to the job listing, if available |
| notes | String | Personal notes about the application, if any |

These fields are **optional in the application's data**, not optional Java method arguments. For now, the constructor will still require all six arguments, but the last two may be `null` when absent. An empty string (`""`) and `null` are different values; we will define normalization and validation when building input handling. A String does not prove that a URL is valid. No `Optional<String>` wrapper or extra dependency is needed for this exercise.

The planned ID is different: the service will generate it so each stored application can be identified independently, even when company and position match. We'll add ID when introducing that responsibility. Do not invent an ID field/value or ask a user to choose one in this exercise.

### My text-field exercise
1. Add private String fields named `jobUrl` and `notes` to JobApplication.
2. Extend the existing constructor by adding jobUrl and notes after status; assign them to the matching fields.
3. Add `getJobUrl()` and `getNotes()`. Keep the existing fields/getters intact.
4. Run `.\mvnw.cmd test` and share the class for review. The existing tests cover the two starter endpoints, so we will inspect these assignments too.

Expected usage after your change:

```java
JobApplication application = new JobApplication(
    "Acme", "Java Developer", LocalDate.of(2026, 10, 7),
    ApplicationStatus.APPLIED, "https://example.com/jobs/123", "Referred by a friend"
);

JobApplication withoutExtraDetails = new JobApplication(
    "Acme", "Java Developer", LocalDate.of(2026, 10, 7),
    ApplicationStatus.APPLIED, null, null
);
```

These illustrate the future constructor; older examples show earlier model versions. No setter or new endpoint is part of this exercise.

**I should be able to...**
- [x] Extend a model using the existing field/constructor/getter pattern without a full implementation example.
- [x] Distinguish optional application data from the arguments required by a Java constructor.
- [x] Explain why an application ID should be generated by the service rather than chosen by the client.

- [x] My jobUrl/notes implementation has been reviewed. Both fields are now private; constructor assignments and getters are correct.

### Coming next: separate input, stored data, and output
A **DTO (Data Transfer Object)** describes data crossing an API boundary. Our planned create-request DTO will contain the values the client may supply; the model represents a tracked application; the response DTO describes what we return, including its generated ID. Keeping those roles separate prevents future internal fields (such as ownership) from automatically becoming writable API input. We will work through a concrete DTO example after reviewing this exercise; CreateApplicationRequest is now implemented as the worked example below; ApplicationResponse is the next user exercise.


### Text-field review complete
The initial attempt used package-private fields. The user corrected jobUrl and notes to private and renamed the URL getter to getJobUrl. All six fields, constructor assignments, and getters are correct. The constructor parameter url validly initializes this.jobUrl; parameter and field names need not match. Optional data and field visibility are independent concepts. No user code was replaced. Next: introduce request/response DTOs.



## DTOs: separate API input, internal data, and output
Our six-field JobApplication model is reviewed. Next, define the API's data contract before implementing its HTTP handlers. **DTO** means Data Transfer Object: a type describing data crossing a boundary. It is a role, not a Java keyword or Spring annotation.

| Type                     | Responsibility                                                                                          |
| ------------------------ | ------------------------------------------------------------------------------------------------------- |
| CreateApplicationRequest | Values a client can supply when creating an application. No ID or owner supplied by the client.         |
| JobApplication           | Our internal representation of a tracked application. It will receive service-generated identity later. |
| ApplicationResponse      | Values returned to the client, including the generated ID. Your next exercise defines this type.        |

These types initially share many fields. Their separate roles matter as the project grows: adding internal ownership data should not silently make it accepted input or returned output. A DTO does not enforce those boundaries by itself; the future controller/service must use it and deliberately map values. It also does not automatically validate fields or reject every unknown JSON property.

Planned flow (not wired yet): **request JSON → CreateApplicationRequest → service → JobApplication → ApplicationResponse → response JSON**.

## Java records for small data carriers
The new project file `application/CreateApplicationRequest.java` is a worked example:

```java
package com.ashraf.jobapptracker.application;

import java.time.LocalDate;

public record CreateApplicationRequest(
    String company,
    String position,
    LocalDate appliedDate,
    ApplicationStatus status,
    String jobUrl,
    String notes
) {
}
```

A **record** is a Java class designed to carry a fixed group of values. Its header lists **components**. The compiler generates private final fields, a constructor taking those components in order, accessor methods, and value-based equals/hashCode/toString. This removes repetitive data-carrier boilerplate; it is standard Java, not Lombok or Spring magic.

The accessor names differ from the model getters:

```java
// Illustrative statements inside a method:
CreateApplicationRequest request = new CreateApplicationRequest(
    "Acme", "Java Developer", LocalDate.of(2026, 10, 7),
    ApplicationStatus.APPLIED, null, null
);
String company = request.company(); // not getCompany()
LocalDate date = request.appliedDate();
```

Records have no generated setters; their component references cannot be reassigned after construction. This is shallow immutability, not a guarantee that every referenced object's internals are immutable. Our components here are strings, LocalDate, and an enum, which are appropriate immutable values. We keep JobApplication as the explicit class you practiced; we are not rewriting the model.

This request record still permits null values and has no validation annotations yet. Omitted status will later be defaulted to APPLIED by creation logic; simply declaring the enum component does not choose a default. We will add required-field validation when that feature is introduced.

It needs no @RestController: it carries data rather than handling requests. Once we add a controller accepting a @RequestBody, Spring's JSON support can construct this record from input; that controller is not implemented yet. Constructor calls use Java values, while an HTTP client sends JSON text. The DTO does not open an endpoint on its own.

**I should be able to...**
- [x] Explain why create input should not accept a client-selected application ID.
- [x] Distinguish a DTO's role from the record language feature used to implement it.
- [x] Read a record component using company() rather than getCompany().
- [x] Explain why separate input/output DTOs help us control API changes.

## My response DTO exercise
Create `ApplicationResponse.java` in the same `application` package, using the request record as your pattern.

Its components, in order, should be:

| Component | Type |
| --- | --- |
| id | long |
| company | String |
| position | String |
| appliedDate | LocalDate |
| status | ApplicationStatus |
| jobUrl | String |
| notes | String |

Use a public record with an empty body. Import LocalDate; ApplicationStatus shares the package. Do not manually write fields, getters, constructors, Spring annotations, or mapping logic. The primitive long represents a numeric ID for a created application; the service will supply it. Defining this response type does not generate that ID or add it to the model yet.

Run `.\mvnw.cmd test`, then tell me when your record is ready for review. Existing endpoint tests do not cover these DTO contracts; we will inspect the components directly and verify JSON behavior when application endpoints exist.

- [x] I have attempted ApplicationResponse.
- [x] My response DTO has been reviewed and its purpose explained. ApplicationResponse correctly declares long id followed by the six client-supplied values, imports LocalDate, and uses generated record accessors. The user explained that the service generates the ID rather than receiving it from the requester.

Before editing: why does ApplicationResponse include an ID while CreateApplicationRequest does not?

The id will be generated by the service before the response and is not needed by the request.

After review, we will finish this DTO milestone, review its feature diff together, and merge only after your confirmation. The service and endpoints are separate following increments.


DTO milestone review: both record definitions are complete. Maven verify passed with two existing endpoint tests and successful JAR packaging. DTO field/type contracts were inspected directly; JSON binding and service mapping are not exercised by those tests and will be verified with the application endpoints. Feature is ready for joint PR review; merge requires the user's explicit confirmation. No ID generation, mapping, service, or application endpoints implemented yet.
