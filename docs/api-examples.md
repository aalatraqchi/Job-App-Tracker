# API examples

Run the app first. These PowerShell commands create a disposable demo application, update it and an interview, then delete only that demo. Browser and API changes use the same database. Run the sections in order, keeping the variables in the same terminal.

## Get a token and retain its cookie

```powershell
$base = 'http://127.0.0.1:8080'
$csrf = Invoke-RestMethod "$base/csrf" -SessionVariable demoSession
$headers = @{}
$headers[$csrf.headerName] = $csrf.token
```

Keep the same host/port and web session for every write. After restarting the app, fetch a fresh token before the next write.

## Create and retrieve an application

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

Creation returns HTTP 201 with a generated ID and default `APPLIED` status. Open `/tracker/{id}` in Chrome to see the same record.

## Add and edit an interview

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

## Update status and filter

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

## Expected errors

PowerShell reports non-success responses as errors. These checks do not change data.

| Request | Expected result |
| --- | --- |
| GET `/applications/-1` | 404 JSON `message`. |
| GET `/applications?status=INVALID` | 400 JSON `message`. |
| POST `/applications` with `{}` and a valid token/cookie | 400 field-validation errors. |
| POST `/applications` without a token | 403 JSON `message`; nothing created. |
| GET `/tracker/-1` | Friendly HTML 404 page. |
| PUT/DELETE `/applications/{wrongParentId}/interviews/{stageId}` with a valid token (and valid JSON for PUT) | 404; the stage remains with its real parent. There is no GET-by-stage endpoint. |

## Delete only the demo application

```powershell
$demo = Invoke-RestMethod "$base/applications/$id"
if ($demo.company -ne 'API Demo' -or $demo.notes -ne 'Disposable API example') {
    throw 'This is not the demo record; deletion cancelled.'
}
Invoke-RestMethod -Method Delete "$base/applications/$id" `
    -WebSession $demoSession -Headers $headers
```

Deletion returns HTTP 204 without a body and also deletes this application's interviews. A subsequent GET for the ID returns 404. In the browser, deletion requires a separate confirmation form.
