# Export a Walk as GPX

WalkMark can export one saved walk as a standard **GPX 1.1** file, so you keep a portable
copy of the route or open it in another mapping application.

## How to export

1. Open **Saved Walks** from the top-right of the main screen, next to **Help & Support**.
2. Find the walk you want, then tap **Export / Share GPX** on that walk.
3. The system share sheet opens with the `.gpx` file attached.

The file is named `walkmark-<walk-name>-<walk-id>.gpx`. Characters that are not letters,
digits, `-`, or `_` are replaced, so the name is always safe to write to disk.

## What the file contains

The exported file is valid UTF-8 GPX 1.1 containing:

- `<metadata>` with the walk name, the walk summary when one exists, and the UTC start time.
- One `<trk>` / `<trkseg>` track with one `<trkpt>` per recorded GPS point, **in the exact order
  the points were recorded**.
- `<ele>` only for points that actually stored an altitude.
- `<time>` only for points that stored a usable timestamp.

WalkMark never invents GPS data. If a point has no altitude or no usable timestamp, that
element is simply left out of the file rather than filled with a placeholder value.

## Offline and private

Export works **entirely offline**. It needs **no account**, **no cloud session**, and
**no network connection**, because it only reads walk data already stored on this device.

Exporting does not modify the walk, its route points, notes, photos, or any identifier.
It only reads.

## Files and permissions

The GPX file is written to a temporary, app-private location and handed to the platform share
mechanism. WalkMark requests **no storage or file-system permission** for export, and the
file is never published to shared or external storage. The system may reclaim the temporary
file later.

## If export does not work

- **"That walk is no longer available on this device."** The walk was deleted, so there is
  nothing left to export.
- **"No application is available to receive the GPX file."** No installed app accepts
  `application/gpx+xml`. Install a mapping app that reads GPX, then try again.
- **"The GPX file could not be shared."** The temporary file could not be written or the
  share sheet could not open. Free up device storage and try again.

WalkMark never shows a file-system path or a raw error message for these failures.

## Not included

GPX **import** is not available, and WalkMark does not create backups, archives, or bulk
exports. Exported routes are not re-imported by WalkMark.
