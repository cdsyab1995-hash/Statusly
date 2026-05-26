# Status Vault

Android MVP for saving viewed WhatsApp statuses.

## Current scope

- Direct entry to the `Status` screen
- WhatsApp / WA Business source switch
- SAF directory grant for WhatsApp status folders
- Recursive scan for viewed status files
- Save to gallery through `MediaStore`
- Saved history with `Room`

## Important behavior

- The app reads viewed statuses from WhatsApp's local status cache.
- A status only appears after the user has viewed it inside WhatsApp.
- Video statuses may not appear until playback has started and WhatsApp has fully cached the file.

## Directory access

The app uses Android's system file picker and needs a directory grant.

You can grant any of these levels:

- `Android/media`
- `Android/media/com.whatsapp`
- `Android/media/com.whatsapp.w4b`
- `Android/media/com.whatsapp/WhatsApp/Media/.Statuses`
- `Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses`

The scanner will recursively locate the real `.Statuses` folder from the granted tree.

## Typical test flow

1. Install the app on a real Android phone.
2. Open at least one WhatsApp status.
3. In the app, choose `WhatsApp` or `WA Business`.
4. Tap `Connect directory` and grant access to `Android/media` or the matching WhatsApp status folder.
5. Return to the app and let it auto-scan.
6. Save a result and verify it appears in Gallery and `Saved`.

## Known limits

- Thumbnail-rich grid UI is not finished yet.
- Some devices and ROMs handle SAF differently.
- Video detection still depends on whether WhatsApp has cached the file locally.
