# HTMX Smoke Test Checklist

Use this checklist after UI or controller changes to validate the HTMX-based flows.

## Preconditions

- App is running and login works.
- User has a valid default workspace (or can open Change Workspace).
- Browser devtools network tab is open (optional, but useful).

## Dashboard Load

1. Open dashboard.
2. Verify initial content loads in `#contentArea`.
3. If no default workspace, verify Change Workspace content appears.
4. Confirm no JS errors in browser console.

## Sidebar HTMX Navigation

1. Click Events, Sources, Targets, Integrations, Consumers.
2. Verify each click updates only `#contentArea` (full page should not reload).
3. Verify active page content appears correctly each time.

## Events Card Tabs (HTMX)

1. In Events, click Manage Definitions.
2. Verify definitions list loads in `#cardContent`.
3. Click Events tab.
4. Verify event history loads in `#cardContent`.
5. Click Test tab.
6. Verify event tester form loads in `#cardContent`.
7. Confirm tab active state updates correctly while switching.

## Sources CRUD

1. Open Sources.
2. Add Source:
   - Open Add modal.
   - Submit valid data.
   - Verify list refresh in `#contentArea` and modal closes.
3. Edit Source:
   - Click row edit icon.
   - Verify modal fields prefill.
   - Save changes.
   - Verify list refresh and updated values visible.
4. Delete Source (row icon path):
   - Click row delete icon.
   - Confirm delete.
   - Verify row is removed.
5. Delete Source (checkbox fallback path):
   - Select a checkbox.
   - Open delete modal (row icon optional).
   - Confirm delete.
   - Verify selected row is removed.

## Definitions CRUD

1. Open Manage Definitions.
2. Add Definition:
   - Submit valid event name, source, and schema.
   - Verify list refresh.
3. Edit Definition:
   - Click edit icon.
   - Verify edit modal content is loaded via HTMX.
   - Update schema and save.
   - Verify list refresh and no modal/backdrop residue.
4. Delete Definition:
   - Click row delete icon and confirm.
   - Verify row removed.

## Targets CRUD (Add/Edit)

1. Open Targets.
2. Add Target:
   - Submit valid name, type, credentials.
   - Verify list refresh.
3. Edit Target:
   - Click edit icon.
   - Verify modal fields prefill.
   - Save.
   - Verify list refresh with updated values.

## Event Tester Actions (HTMX)

1. Open Events -> Test.
2. Validate action:
   - Choose an event, provide valid JSON payload.
   - Click Validate.
   - Verify success message appears in `#eventTesterStatus`.
3. Validate failure path:
   - Use invalid JSON/payload.
   - Click Validate.
   - Verify failure message appears.
4. Publish action:
   - Use valid event and payload.
   - Click Publish.
   - Verify success message appears.

## Modal/Backdrop Cleanup

1. Perform multiple add/edit/delete actions in sequence.
2. Ensure modal backdrop is removed after each successful HTMX swap.
3. Verify page does not get stuck in modal-open state.

## Regression Quick Checks

1. Reload dashboard and re-test one flow from each section:
   - Sources edit
   - Definitions edit
   - Targets edit
   - Event tester validate
2. Confirm there are no duplicate event binding side effects (single click should trigger one request).

## HTTP Verification (Optional)

- Confirm expected endpoints are called:
  - `/site/v1/sources`, `/site/v1/updateSource`, `/site/v1/deleteSource`
  - `/site/v1/definitions`, `/site/v1/editDefinition`, `/site/v1/updateDefinition`, `/site/v1/deleteDefinition`
  - `/site/v1/targets`, `/site/v1/createTarget`, `/site/v1/updateTarget`
  - `/site/v1/eventTester`, `/site/v1/validateEventData`, `/site/v1/publishEvent`

## Sign-off

- [ ] No blocking UI issues.
- [ ] No JS console errors.
- [ ] Core CRUD + tester flows pass.
- [ ] Ready for PR review.
