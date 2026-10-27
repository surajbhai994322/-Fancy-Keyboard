# BW Fancy Keyboard

An Android custom keyboard (IME) with a **Fancy Styles** menu. It includes Monospace, Banana, Table Chair, Without Vowels, Double Letter, Alternative, Reverse Alternative, Reverse Text, and Dot Style.

## Build APK on GitHub Actions

This project includes `.github/workflows/build-apk.yml`.

1. Upload the **contents of this folder** to the root of your GitHub repository (do not upload only this ZIP file).
2. Open the repository's **Actions** tab.
3. Choose **Build BW Fancy Keyboard APK** and tap **Run workflow**. A push to `main` also starts a build.
4. When the run finishes successfully, open the run and download the artifact named `BW-Fancy-Keyboard-debug-apk`.
5. Extract the downloaded artifact ZIP to get `app-debug.apk`, then install it on your Android phone. Android may ask you to allow installs from that source.

The debug APK is for testing. Android keyboards require the user to enable the keyboard in system Settings before selecting it. The keyboard does not send text to a server.
