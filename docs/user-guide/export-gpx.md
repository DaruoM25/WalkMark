# GPX Export (export infrastructure)

WalkMark can generate a standard **GPX 1.1** file for a locally saved walk and hand it to the
platform share/export mechanism.

> **Status: infrastructure only — no user-facing entry point yet.**
> GPX generation and platform sharing are implemented and unit-tested. The button that triggers
> an export will be added to the canonical **Walk Detail** UI, which is owned by a separate
> story. Until then there is nothing to tap, so this page documents the exported file format and
> the platform behaviour rather than a step-by-step user task.

## The exported file

The generated file is valid UTF-8 GPX 1.1 containing:

- `<metadata>` with the walk name, the walk summary when one exists, and the UTC start time.
- One `<trk>` / `<trkseg>` track with one `<trkpt>` per recorded GPS point, **in the exact order
  the points were recorded**.
- `<ele>` only for points that actually stored an altitude.
- `<time>` only for points that stored a usable timestamp.

The file is named `walkmark-<walk-name>-<walk-id>.gpx`. Characters that are not letters, digits,
`-`, or `_` are replaced, so the name is always safe to write to disk.

WalkMark never invents GPS data. If a point has no altitude or no usable timestamp, that element
is left out of the file rather than filled with a placeholder value.

## Offline and private

Export works **entirely offline**. It needs **no account**, **no cloud session**, and **no network
connection**, because it only reads walk data already stored on this device.

Exporting does not modify the walk, its route points, notes, photos, or any identifier. It only
reads.

## Files and permissions

The GPX file is written to a temporary, app-private location and handed to the platform share
mechanism.

- **Android:** written to the app-private cache under `walkmark-gpx/` and shared through
  `androidx.core.content.FileProvider` with `ACTION_SEND`, MIME type `application/gpx+xml`, and
  `FLAG_GRANT_READ_URI_PERMISSION`.
- **iOS:** written to `NSTemporaryDirectory()` and presented with `UIActivityViewController`.

WalkMark requests **no storage or file-system permission** for export, and the file is never
published to shared or external storage. The system may reclaim the temporary file later.

## Failure states

Export reports a closed set of user-facing reasons and never surfaces a file-system path or a
raw error message:

| Reason | Meaning |
| --- | --- |
| `WalkNotFound` | The walk no longer exists on this device; there is nothing to export. |
| `NoCompatibleApp` | No installed application accepts `application/gpx+xml`. |
| `ShareFailed` | The temporary file could not be written, or the share sheet could not open. |

An export already in progress suppresses duplicate export requests, so a single walk cannot
produce repeated share sheets.

## Not included

GPX **import** is not available. WalkMark does not create backups, archives, or bulk exports, and
exported routes are not re-imported by WalkMark.
