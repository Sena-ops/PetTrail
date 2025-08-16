# PWA Icons

This directory should contain PWA icons in the following sizes:

- icon-72x72.png
- icon-96x96.png  
- icon-128x128.png
- icon-144x144.png
- icon-152x152.png
- icon-192x192.png
- icon-384x384.png
- icon-512x512.png

## Icon Requirements

- Format: PNG
- Purpose: "any maskable" (supports both regular and maskable icon modes)
- Design: Should represent the map application with a simple, recognizable design
- Background: Should work well on both light and dark backgrounds

## Temporary Solution

For development and testing, you can:

1. Use a simple colored square as a placeholder
2. Generate icons using online tools like:
   - https://realfavicongenerator.net/
   - https://www.pwabuilder.com/imageGenerator
   - https://favicon.io/

## Note

The manifest.webmanifest file references these icons. If icons are missing, the PWA will still work but may not display properly when installed on devices.
