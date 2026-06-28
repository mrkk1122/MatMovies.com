https://d2fy7spvltzu3v.cloudfront.net/94hk6%2Fpreview%2F79248424%2Fmain_large.png?response-content-disposition=inline%3Bfilename%3D%22main_large.png%22%3B&response-content-type=image%2Fpng&Expires=1782626254&Signature=BWIQKovj-1GAlHvkIB399etQa2O7J0RAAgauBh98QvoIi0lQ5xXWx56wkKSSsCJTYapYx~Dtt6jyagdr45AtcKc~ZGlX8~v9mgC3eiFoKekflmcWDaRp7s1PAoMQAkdicKq7XZORNDCyM~nV6h4du7A9NwTLmUp98suj5xqEvZYDDgI0XScFA9Rz3G7w5fYt-d4uBX1XSAidbWcNteu54nZogKG0TpuC4Z4qjbRA6jnvFGrKlETalyso-wWsdrnmevfokiVArHwLM4whwe3Cmuf0uDT54MJv3fRCCrMwiraV4MUFCcHVqCgjqJX02UsUxJufHmAIXmvMpydbzm3h5w__&Key-Pair-Id=APKAJT5WQLLEOADKLHBQ
# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: 

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
