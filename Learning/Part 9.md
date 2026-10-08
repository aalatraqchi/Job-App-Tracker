# Part 9 — Updating an application

## Goal
Change an existing application's details without creating another application or changing its ID. Start with the service; the PUT endpoint and edit page come after your attempt is reviewed.

## What you need
- `UpdateApplicationRequest`: a record with the same six fields as `CreateApplicationRequest`. No ID field: the method receives the ID separately. A separate DTO lets create and update rules differ later.
- `new JobApplication(...)`: construct the replacement using the existing ID and the submitted details. We can replace the stored object without adding setters to the model.
- `applications.replace(id, updated)`: replace the value only if that key exists. It returns the previous object, or `null` if there was no entry. Unlike `put`, it cannot accidentally insert a missing application.

Creating allocates a new ID. Updating keeps an existing ID. Do not call `create()` or `nextId.getAndIncrement()` when updating.

For this increment, submitted company, position, date, job URL, and notes replace their old values. A null status preserves the existing status. This is not a general partial-update method; required-field validation will follow separately.

## Your exercise
1. Create `UpdateApplicationRequest.java` in the `application` package. Follow the existing request record's field names and types.
2. Add this method to `ApplicationService`:

```java
public JobApplication update(long id, UpdateApplicationRequest request) {
    // Your implementation
}
```

3. Call `findById(id)` to retrieve the current application. This already handles a missing ID.
4. Select the requested status, or the current status if the request status is null.
5. Construct the replacement with the same ID and the request's other fields.
6. Store it with `applications.replace`. If that returns null, throw `new ApplicationNotFoundException(id)`; otherwise return the replacement. This also handles an entry disappearing between lookup and replacement.

Leave the controller and templates alone for now. Run the existing tests, then ask for review. Existing tests check for regressions; they do not yet prove the new update behavior.

## I should be able to...
- [x] Explain why updating must keep the ID and avoid allocating a new one.
- [x] Explain why `replace` is useful for updating an existing map entry.
- [x] Implement the request record and service method using existing patterns.

After review, we will verify changed fields, unchanged ID/count, preserved status when omitted, and missing-ID behavior before adding HTTP/UI access.

## Review checkpoint
Your DTO and corrected service method are reviewed and preserved. Three update tests pass: replacement details/identity/count/ID allocation, omitted-status preservation and optional-field clearing, and missing-ID rejection. Full Maven verify passes 21 tests and packaging. Concept boxes remain your self-assessment. Next: PUT endpoint, then edit page, when ready.
