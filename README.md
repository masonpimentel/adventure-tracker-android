# Cpen391Android
Android App to complement Adventure Tracker

Setting up Google Maps API key:

If you go to res>values>google_maps_api.xml you'll see that an maps key is needed. It turns out that we can all use this key, I just need to add your SHA-1 fingerprint to the key. 

On Windows I had to find my keytool.exe, then run:

keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android

Please send me the SHA1 fingerprint you see.

I'm not sure how this will work on Linux and Mac, but here's the link to the instructions I followed: https://developers.google.com/maps/documentation/android-api/signup#release-cert
