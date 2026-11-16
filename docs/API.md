# REST API

The JSON API sits alongside the browser interface and uses the same services and PostgreSQL database. By default it runs at `http://127.0.0.1:8080`. There are no accounts or logins; see [Current Scope](../README.md#current-scope).

## Endpoints

### Applications

| Method | Path | Result |
| --- | --- | --- |
| `GET` | `/applications` | List applications, newest applied date first. Optional `status` and `company` query parameters. |
| `POST` | `/applications` | Create an application. Returns 201. |
| `GET` | `/applications/{id}` | Get one application. |
| `PUT` | `/applications/{id}` | Replace an application's editable details. |
| `DELETE` | `/applications/{id}` | Delete an application and its interview stages. Returns 204. |

### Interview stages

| Method | Path | Result |
| --- | --- | --- |
| `GET` | `/applications/{applicationId}/interviews` | List an application's stages in creation order. |
| `POST` | `/applications/{applicationId}/interviews` | Add a stage. Returns 201. |
| `PUT` | `/applications/{applicationId}/interviews/{interviewId}` | Replace a stage's editable details. |
| `DELETE` | `/applications/{applicationId}/interviews/{interviewId}` | Delete a stage. Returns 204. |

### Other

| Method | Path | Result |
| --- | --- | --- |
| `GET` | `/csrf` | Get a CSRF token for write requests. |
| `GET` | `/health` | Health check: `{"status":"UP","application":"job-app-tracker"}` |

## Request bodies

**Application** (`POST` and `PUT /applications`):

```json
{
  "company": "Example Corp",
  "position": "Software Engineer",
  "appliedDate": "2026-10-08",
  "status": "APPLIED",
  "jobUrl": "https://example.com/careers/123",
  "notes": "Referred by a former colleague."
}
```

- `company` and `position` are required, up to 200 characters each. `appliedDate` is required.
- `status` is one of `APPLIED`, `INTERVIEW`, `OFFER`, `REJECTED` or `WITHDRAWN`. It defaults to `APPLIED` on create, and an omitted status keeps the current value on update.
- `jobUrl` is optional and must be an `http` or `https` URL (up to 2,000 characters). `notes` is optional (up to 5,000 characters). On update, omitted optional fields are cleared.

**Interview stage** (`POST` and `PUT .../interviews`):

```json
{
  "stageName": "Technical interview",
  "scheduledAt": "2026-11-10T14:30:00-05:00",
  "outcome": "PENDING",
  "notes": "Discuss Java and Spring Boot experience."
}
```

- `stageName` is required (up to 200 characters). `scheduledAt` is optional but must include a UTC offset; it is stored as an instant and returned in UTC.
- `outcome` is one of `PENDING`, `PASSED`, `FAILED` or `CANCELLED`. It defaults to `PENDING` on create and keeps its current value when omitted on update. Stage outcomes don't change the application's status.

## Filtering

`GET /applications?status=INTERVIEW&company=acme` combines both filters with AND. Company matching is partial and case-insensitive. Blank filters are ignored, and an unknown status returns 400.

## Errors

Errors return JSON with a `message`. Validation errors also include an `errors` map of field names to messages.

| Situation | Status |
| --- | --- |
| Invalid body, unknown status or outcome, malformed date or time | 400 |
| Missing or invalid CSRF token on a write request | 403 |
| Application or stage not found, including a stage under the wrong application | 404 |

## CSRF tokens

`GET` requests need no token. For `POST`, `PUT`, `PATCH` and `DELETE`, first call `GET /csrf`. Keep the session cookie it sets, and send the returned token in the header it names (`X-CSRF-TOKEN`). Use the same host and port for both requests, and fetch a new token after the app restarts.

### Example with curl

```bash
base=http://127.0.0.1:8080
token=$(curl -s -c cookies.txt "$base/csrf" | sed -E 's/.*"token":"([^"]+)".*/\1/')

curl -s -b cookies.txt -H "X-CSRF-TOKEN: $token" -H 'Content-Type: application/json' \
  -d '{"company":"Example Corp","position":"Software Engineer","appliedDate":"2026-10-08"}' \
  "$base/applications"

curl -s "$base/applications?company=example"
```

## Walkthrough in PowerShell

These commands create a disposable demo application, update it and add an interview, then delete only that demo. Run the sections in order in the same terminal.

### Get a token and retain its cookie

```powershell
$base = 'http://127.0.0.1:8080'
$csrf = Invoke-RestMethod "$base/csrf" -SessionVariable demoSession
$headers = @{}
$headers[$csrf.headerName] = $csrf.token
```

Keep the same host/port and web session for every write. After restarting the app, fetch a fresh token before the next write.

### Create and retrieve an application

```powershell
$applicationBody = @{
    company = 'API Demo'
    position = 'Java Developer'
    appliedDate = '2026-10-08'
    notes = 'Disposable API example'
} | ConvertTo-Json
$app = Invoke-RestMethod -Method Post "$base/applications" `
    -WebSession $demoSession -Headers $headers `
    -ContentType 'application/json' -Body $applicationBody
$id = $app.id
Invoke-RestMethod "$base/applications/$id"
```

Creation returns HTTP 201 with a generated ID and default `APPLIED` status. Open `/tracker/{id}` in the browser to see the same record.

### Add and edit an interview

```powershell
$stageBody = @{
    stageName = 'Phone screen'
    scheduledAt = '2026-10-15T14:30:00-04:00'
    notes = 'Discuss Java experience'
} | ConvertTo-Json
$stage = Invoke-RestMethod -Method Post "$base/applications/$id/interviews" `
    -WebSession $demoSession -Headers $headers `
    -ContentType 'application/json' -Body $stageBody

$stageBody = @{
    stageName = 'Phone screen'
    scheduledAt = '2026-10-15T14:30:00-04:00'
    outcome = 'PASSED'
    notes = 'Technical interview next'
} | ConvertTo-Json
Invoke-RestMethod -Method Put "$base/applications/$id/interviews/$($stage.id)" `
    -WebSession $demoSession -Headers $headers `
    -ContentType 'application/json' -Body $stageBody
Invoke-RestMethod "$base/applications/$id/interviews"
```

Use null/omit `scheduledAt` for an unscheduled stage. Interview outcome and overall application status are separate.

### Update status and filter

```powershell
$applicationBody = @{
    company = 'API Demo'
    position = 'Java Developer'
    appliedDate = '2026-10-08'
    status = 'INTERVIEW'
    notes = 'Disposable API example'
} | ConvertTo-Json
Invoke-RestMethod -Method Put "$base/applications/$id" `
    -WebSession $demoSession -Headers $headers `
    -ContentType 'application/json' -Body $applicationBody
Invoke-RestMethod "$base/applications?status=INTERVIEW&company=API%20Demo"
```

PUT replaces editable details, so include values you want to retain. An omitted application status preserves its current value; omitted optional fields are cleared.

### Expected errors

PowerShell reports non-success responses as errors. These checks do not change data.

| Request | Expected result |
| --- | --- |
| GET `/applications/-1` | 404 JSON `message`. |
| GET `/applications?status=INVALID` | 400 JSON `message`. |
| POST `/applications` with `{}` and a valid token/cookie | 400 field-validation errors. |
| POST `/applications` without a token | 403 JSON `message`; nothing created. |
| GET `/tracker/-1` | Friendly HTML 404 page. |
| PUT/DELETE `/applications/{wrongParentId}/interviews/{stageId}` with a valid token (and valid JSON for PUT) | 404; the stage remains with its real parent. There is no GET-by-stage endpoint. |

### Delete only the demo application

```powershell
$demo = Invoke-RestMethod "$base/applications/$id"
if ($demo.company -ne 'API Demo' -or $demo.notes -ne 'Disposable API example') {
    throw 'This is not the demo record; deletion cancelled.'
}
Invoke-RestMethod -Method Delete "$base/applications/$id" `
    -WebSession $demoSession -Headers $headers
```

Deletion returns HTTP 204 without a body and also deletes this application's interviews. A subsequent GET for the ID returns 404. In the browser, deletion requires a separate confirmation form.
