# real-autorotate

An Android Application for choosing App-specific Auto Rotate setting. We generally use auto-rotate in some apps like Youtube, 
Photo Gallery etc. and we tend to forget turning it off when closing that application. It gets annoying when the UI accidentally 
auto-rotates to landscape mode in other applications. "Real-AutoRotate" automates this process by turning “Auto-Rotate” setting on/off 
based on the application being used in the foreground. 

Have you ever got annoyed when your phone's screen accidentally rotates to Landscape mode? "Real AutoRotate" is here to solve this simple problem. This app allows you to choose apps in which you want the auto-rotation setting enabled.

HOW TO USE:

Step 1: Download the App from Google Play

Step 2: When you open the App for the first time, it asks you for the necessary permissions. Grant the required permissions.

Step 3: Click on the "Select Apps" icon in the bottom right corner and select the Apps for which you want to have the auto-rotate setting ON. Then, Press "OKAY"

Step 4: Enable the Service using the toggle in the Top right corner (Grant the necessary permissions if you haven't done yet).

Step 5: If you ever wanted to remove some apps from the list in future just click in the check box and the app will be removed.

That's it! 

This app overrides existing auto-rotate setting. Therefore, make sure you disable the service within the app if you want to toggle the setting manually.

REQUIRED PERMISSIONS:

1. USAGE STATS - This Permssion is required to check which App is running in the foreground.

2. WRITE SETTINGS - This Permission is required to toggle auto rotation ON/OFF.

This App does NOT require a working internet connection to work. Therefore, No data is sent to any server.

NOTE:

Do NOT remove the app from "recent apps" screen. It causes the service to stop.
In Case, you accidentally removed "Real AutoRotate", just open the app to re-enable the service.

CREDITS:

1. App Icon designed by Freepik from www.flaticon.com
2. "Select Apps" button within the app is designed by Smashicons from www.flaticon.com
3. https://github.com/ricvalerio/foregroundappchecker (Library to retrieve foreground app info)
4. https://github.com/Angads25/android-toggle (Toggle Button)
