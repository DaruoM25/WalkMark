# Visual Acceptance Protocol

## Separation of Concerns
`SCREENSHOT_CAPTURE == PASS` and `VISUAL_ACCEPTANCE == PASS` are distinct statuses.
An artifact screenshot existing does NOT equal visual acceptance.

## Mandatory Visual Acceptance Checklist
1. **PNG Signature & Rendering**: File has valid PNG signature, non-zero dimensions, and opens cleanly.
2. **Target Window**: Target application package and activity are in active foreground focus.
3. **UI Elements Present**: Required buttons, labels, and map/list views are rendered with correct copy.
4. **No Error Dialogs**: No "Application Not Responding" (ANR), system crashes, or unhandled permission dialogs.
5. **Layout Integrity**: No text clipping, layout overlapping, or status bar / navigation bar occlusion.
6. **Contrast & Theme**: Content meets contrast guidelines and renders in intended dark/light mode.
7. **Display Mode**: Final visual sign-off must be performed on a visible (non-headless) emulator instance.
