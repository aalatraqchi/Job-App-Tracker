# Part 4 — Services, in-memory storage, and identity

Previous lessons: [[Job Apps Tracker/Learning/Part 2|Model and DTOs]] and [[Job Apps Tracker/Learning/Part 3|Git workflow]]. The DTO milestone was reviewed and merged in [PR #1](https://github.com/aalatraqchi/Job-App-Tracker/pull/1) with your approval.

## Current goal

Build the storage layer in small steps before connecting HTTP endpoints. Today's worked example is `ApplicationService.java`; it holds an empty collection and can list its contents. Creation, ID generation, DTO mapping, and application endpoints come after your model-ID exercise is reviewed.

## Why a service exists

The future controller will translate HTTP requests and responses. The service owns application operations, such as creating a tracked application, choosing a default status, and finding stored applications. Keeping those decisions here lets tests and future callers use them without constructing an HTTP request.

`@Service` tells Spring to discover this class and manage an instance. Our application package is under the main application's package, so normal component scanning finds it. By default, callers in the same Spring application context share one service instance. This is why its collection can retain applications between requests. Later, a controller will receive this instance through its constructor: dependency injection. There is no controller connection yet, and `@Service` creates no URL.

## Map: find an application by its ID

Actual worked example:

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

At present the map is empty, so `findAll()` returns an empty list. There is no method to populate it yet. Everything lives only in this running application instance: restarting discards the collection. PostgreSQL will later provide persistence across restarts.

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

- [ ] Explain why application operations belong in a service rather than the HTTP controller.
- [ ] Explain what `@Service` does and why it does not create a URL.
- [ ] Read `Map<Long, JobApplication>` as ID-to-model pairs.
- [ ] Explain why `final` does not stop entries being added to the map.
- [ ] Explain what the returned list protects and what it does not copy.
- [ ] Explain why restarting loses these applications.
- [x] Add the model ID using the constructor/getter pattern and have the attempt reviewed.

Verification of the worked example: Maven test passed with two existing health/about endpoint tests and zero failures/errors. The service compiles and the Spring application starts; storage behavior is not covered by those tests. These understanding and exercise boxes remain unchecked until demonstrated.

## Model-ID exercise reviewed

Your private long id, first constructor parameter, this.id = id assignment, and public long getId() are correct. Existing six fields were preserved; no ID setter or request-ID component was added. Your code was reviewed without replacement. Maven test passed: two existing endpoint tests, zero failures/errors. These tests do not assert the model ID or storage behavior; those will be exercised when creation is added. Other understanding checkboxes remain pending demonstration.

Before creation: explain why two applications for the same company and position can still need different IDs, and why a service field can retain stored applications across HTTP requests.
