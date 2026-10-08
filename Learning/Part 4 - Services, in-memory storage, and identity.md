Previous lessons: [[Job Apps Tracker/Learning/Part 2 - Modeling a job application|Model and DTOs]] and [[Job Apps Tracker/Learning/Part 3 - Git and GitHub workflow|Git workflow]]. The DTO milestone was reviewed and merged in [PR #1](https://github.com/aalatraqchi/Job-App-Tracker/pull/1) with your approval.

## Current goal

Build the storage layer in small steps before connecting HTTP endpoints. ApplicationService now creates and lists stored models with generated IDs and an omitted-status default. The model-ID exercise and service/storage foundation concepts are complete and reviewed. Current exercise: test preservation of an explicitly supplied status. Response mapping and application HTTP endpoints follow later.

## Why a service exists

The future controller will translate HTTP requests and responses. The service owns application operations, such as creating a tracked application, choosing a default status, and finding stored applications. Keeping those decisions here lets tests and future callers use them without constructing an HTTP request.

`@Service` tells Spring to discover this class and manage an instance. Our application package is under the main application's package, so normal component scanning finds it. By default, callers in the same Spring application context share one service instance. This is why its collection can retain applications between requests. Later, a controller will receive this instance through its constructor: dependency injection. There is no controller connection yet, and `@Service` creates no URL.

## Map: find an application by its ID

Initial worked example (before creation was added):

```java
@Service
public class ApplicationService {
    private final Map<Long, JobApplication> applications = new ConcurrentHashMap<>();

    public List<JobApplication> findAll() {
        return List.copyOf(applications.values());
    }
}
```

`Map<Long, JobApplication>` describes pairs: each key is an application ID and each value is a model object. Java's collection type parameters use object types, so we write `Long` here even though our response record uses primitive `long`. Java can automatically convert between them when necessary.

`Map` is the interface describing available operations. `ConcurrentHashMap` is the implementation storing the entries. We choose it because web requests can run concurrently; its individual operations support concurrent access. It does not automatically make a sequence of operations or mutable model objects thread-safe. We will handle ID allocation separately when we add creation.

For example, the future creation method will use `applications.put(id, application)`. A later lookup can use `applications.get(id)` rather than searching company names. Two applications can have identical company and position values while retaining different IDs. An ID identifies a particular tracked record, independently of editable details.

`private final` keeps the map reference inside this service and prevents assigning a different map to that field. It still permits adding/removing entries in the map; `final` does not freeze its contents.

## Listing without exposing the storage collection

`applications.values()` provides the stored model values. `List.copyOf(...)` produces an unmodifiable list containing those values, so callers cannot add/remove stored entries through the returned list. It does not clone the model objects. With concurrent writes, this is not a transactional snapshot, and ordering is unspecified; neither is promised by this early milestone.

Initially the map was empty and findAll() returned an empty list. The creation example below now populates it. Everything lives only in this running application instance: restarting discards the collection. PostgreSQL will later provide persistence across restarts.

## Your exercise — add identity to the model

You already implemented private fields, constructor assignments, and getters in `JobApplication`. Apply that same pattern:

1. Add a private `long id` field to `JobApplication.java`.
2. Add `long id` as the first constructor parameter and assign it to the field.
3. Add a public `getId()` method returning that value.
4. Keep the existing six fields and their assignments/getters.
5. Run `.\mvnw.cmd test`, then ask for review.

Do not add the ID to `CreateApplicationRequest`. The service will supply it when constructing the model; the model constructor merely receives the ID and does not generate it. Keep `ApplicationResponse` as it is. Do not add a setter: the identity should remain stable when editable details change.

After review, we will add creation to the service, generate distinct IDs, default omitted status to `APPLIED`, and verify that creating two applications retains both. Response mapping and the HTTP endpoints will follow incrementally.

## I should be able to...

- [x] Explain why application operations belong in a service rather than the HTTP controller.
- [x] Explain what `@Service` does and why it does not create a URL.
- [x] Read `Map<Long, JobApplication>` as ID-to-model pairs.
- [x] Explain why `final` does not stop entries being added to the map.
- [x] Explain what the returned list protects and what it does not copy.
- [x] Explain why restarting loses these applications.
- [x] Add the model ID using the constructor/getter pattern and have the attempt reviewed.

1. **Service versus controller — The service owns application operations and business rules. The controller handles HTTP details. This lets us reuse and test those operations without sending HTTP requests.
2. **`@Service` — Spring discovers the class and manages a shared instance by default. The future **controller calls the service**; the HTTP client calls the controller’s endpoint. `@Service` itself defines no request mapping.
3. Id -> object mapping
4. `final` — it fixes the map reference, not its contents.
5. Callers cannot add or remove elements through that returned list. It contains references to the **same model objects**, rather than independent copies of those objects. If a model later has setters, changing that object would also change the object held in storage.
6. **Restarting** — The map exists in the running app’s memory. Restarting creates a new service with an empty map; we haven’t added persistent storage yet.

Verification of the worked example: Maven test passed with two existing health/about endpoint tests and zero failures/errors. The service compiles and the Spring application starts; storage behavior is not covered by those tests. The foundations checklist above has now been demonstrated and reviewed in conversation.

## Model-ID exercise reviewed

Your private long id, first constructor parameter, this.id = id assignment, and public long getId() are correct. Existing six fields were preserved; no ID setter or request-ID component was added. Your code was reviewed without replacement. Maven test passed: two existing endpoint tests, zero failures/errors. These tests do not assert the model ID or storage behavior; those will be exercised when creation is added. Foundation understanding was subsequently demonstrated in conversation; the checklist above is complete.

==Before creation: explain why two applications for the same company and position can still need different IDs, and why a service field can retain stored applications across HTTP requests.==

1. Each application is its own separate object, unique id's are the only way to distinguish them
2. Spring creates one shared instance by default and its fields remain attached to it during its lifetime

## Creating an application: request → model → storage

The service/storage foundations are now demonstrated: you explained ID-to-object pairs, `final`, shared service lifetime, unmodifiable lists with shared object references, and loss of memory on restart. The model-ID exercise is also reviewed.

The worked example now in `ApplicationService.java` is:

```java
private final AtomicLong nextId = new AtomicLong(1);

public JobApplication create(CreateApplicationRequest request) {
    ApplicationStatus status = request.status();
    if (status == null) {
        status = ApplicationStatus.APPLIED;
    }

    long id = nextId.getAndIncrement();
    JobApplication application = new JobApplication(
            id, request.company(), request.position(), request.appliedDate(),
            status, request.jobUrl(), request.notes()
    );
    applications.put(id, application);
    return application;
}
```

### Allocating identity

`AtomicLong` is a standard Java counter imported from `java.util.concurrent.atomic`. `getAndIncrement()` returns the current value and increments the counter as one atomic operation. Starting at 1 means sequential calls receive 1, then 2, then 3. Concurrent callers also receive distinct values within this service instance; a plain `nextId++` would not safely combine reading and incrementing across threads.

The counter and map solve separate problems: the counter allocates IDs, while the map stores objects under them. Atomic allocation does not make the whole method a database transaction. This local counter resets with a new service instance and is not a shared counter across multiple running app instances. Database-generated identity comes later.

### Defaults and mapping belong to creation

`request.status()` reads the record component. If it is null, the local status variable becomes `APPLIED`; otherwise it keeps the supplied enum value. We do not modify the request record. Choosing this default is a business rule, so it belongs in creation logic rather than the HTTP controller.

Calling the model constructor explicitly copies the request's six values into a new tracked object and adds the generated ID. This is **mapping**: converting between types with different responsibilities. `applications.put(id, application)` retains it in the service's map. Returning it lets the caller inspect the created object. `findAll()` now includes it. The service currently returns models; mapping these to `ApplicationResponse` will be introduced with the controller boundary.

This method is callable Java code, not an HTTP endpoint. There is still no `/applications` URL. Required-field validation remains a later milestone; these examples supply valid company, position, and date values.

### Testing a service without HTTP or Spring startup

`ApplicationServiceTests.java` calls the service directly. The first test creates a request with null status and checks copied details and the `APPLIED` default. The second creates twice from identical input and checks that both objects are stored with different IDs. These assertions verify observable behavior rather than relying on list order or exact counter values.

Each test follows **arrange → act → assert**:

1. Arrange a fresh service and request.
2. Act by calling `service.create(request)`.
3. Assert the returned/stored values with JUnit.

Here `new ApplicationService()` is intentional: each independent test needs isolated storage. Within each test, all calls use that same instance. The future running application will use Spring's shared instance across HTTP requests. These are unit tests; no `@SpringBootTest`, server, port, browser, or mocks are needed. The existing health/about tests remain HTTP integration tests.

### Your exercise — preserve an explicit status

Add a third `@Test` method to `ApplicationServiceTests.java`, using the first test as a pattern:

1. Create a fresh service.
2. Construct a request whose status is `ApplicationStatus.INTERVIEW`.
3. Call `create(...)`.
4. Assert that the created application's status is still `INTERVIEW`.
5. Run `.\mvnw.cmd test` and ask for review. Expect five tests total once your new test is added.

This covers the other branch of the default rule: default only when omitted, rather than always replacing the requested status. Try it yourself before asking for hints.

**I should be able to...**

- [x] Explain why ID allocation and map storage are separate operations.
- [x] Explain why `getAndIncrement()` is useful when requests run concurrently.
- [x] Trace a request's fields through `create()` into the stored model.
- [x] Explain why a plain unit test can create its own service while the app shares one.
- [x] Add the explicit-status test and have its implementation reviewed.

Worked-example verification on 10/8/26: four tests passed, zero failures/errors (two service tests and two existing HTTP tests). No application HTTP endpoints or response mapping yet. The user has self-checked the creation concept items; the explicit-status test was subsequently implemented and reviewed as recorded below.

### Explicit-status test review — 10/8/26

Your testingCustomStatusEnumPassedIn test correctly arranges a fresh service and INTERVIEW request, calls create(), and asserts INTERVIEW on the returned model. That assertion would fail if creation always replaced the status with APPLIED. The extra copied-detail assertions are valid, though the existing default-status test already checks those details. Your implementation was preserved. Maven test passed with five tests, zero failures/errors: three service unit tests and two HTTP integration tests. No HTTP application endpoints or response mapping added during review. The remaining concept boxes reflect your self-assessment, rather than a separate oral review.
