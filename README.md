# Car Care
## Introduction
Car Care collects diagnostic information from a car's OBD port and displays it on the user's smartphone with various graphics. The data can be viewed in real time or at a later date without requiring an active connection to the OBDII adapter. Alerts can be set up to notify the user when data received is lower or higher than a specified value.

Car Care is compatible with the ELM 327 OBDII adapter.

## Release notes for version 1.0.0
### New software features for this release
* Added Google Services integrated login
* Added Bluetooth pairing ability between the application and an OBDII bluetooth device
* Added settings page to customize data shown on Home page
* Added Vehicle Information page for storing vehicle information
* Added static data card on Home Activity that displays current live values received from the OBD adapter
* Added dynamic data card on Home Activity that displays a live history of values received from the OBD adapter
* Added Dynamic Statistics page to view a history of values from previous trips
* Added a trip filter to the Dynamic Statistics page to view data from specific previous trips
* Added date reminders that trigger an alert when the current date is after the date specified in the reminder
* Added value reminders that trigger an alert when the application receives a value for a specified type of data that sastisfies the  comparison specified in the reminder
* Added the ability to archive reminders, allowing for deactivation and history of reminders

### Bug fixes made since last release
* Fixed app crash when vehicles database was queried but no vehicles were present

### Known bugs and defects
* Diagnostic code viewing is not implemented
* Miles per gallon viewing is not implemente

## Install guide
### Pre-requisites
Car Care requires a smartphone running Android OS version 4.0 or higher. For communication between the vehicle's OBD and the smartphone, an OBDII capable bluetooth adapter is required. For installation of the Car Care application onto the smartphone a working installation of Android Studio is necessary.
### Dependent libraries that must be installed
Car Care uses [Paulo Pires](https://about.me/pires)'s [obd-java-api](https://github.com/pires/obd-java-api) library.
All dependent libraries required are included in the Github project. 
### Download instructions
The project will be downloaded automatically by Android Studio during the initial setup. It may also be downloaded manually from Github by clicking on the *Clone or Download* button on the Car Care Github main page, then clicking *Download Zip*.
### Build instructions
#### If the project was downloaded manually as a zip file
1. Locate and unzip the downloaded file.
2. Open Android Studio. On the welcome screen, choose *Open an existing Android Studio project* and navigate to the location of the folder containing the project, then click *OK*. The project will then perform an automatic setup.
#### If the project will be downloaded automatically through Github
1. Open Android Studio. On the welcome scren, choose *Check out project from Version Control*, and select *Github* from the drop down menu. 
2. In the Clone Repository dialog enter `https://github.com/LTT-Gatech/CarCareAndroid.git` in the Git Repository URL field, enter the preferred information for the other fields, then click *Clone*.
3. When prompted to create a Studio project for the sources you have checked out, select *Yes*.
4. Choose *Import project from external model* and ensure Gradle is selected below this. In the following dialog, select *Use default Gradle wrapper (recommended)*, then select *Finish*. 
5. When asked which modules/data to include in the project, ensure *CarCareAndroid (root module)* and *:app* are selected, then click *OK*.

#### After the project is added to Android Studio
Google services must be enabled for the application by generating a google-services.json file. 
1. Go to the [Google Services website](https://developers.google.com/mobile/add?platform=android&cntapi=signin&cnturl=https:%2F%2Fdevelopers.google.com%2Fidentity%2Fsign-in%2Fandroid%2Fsign-in%3Fconfigured%3Dtrue&cntlbl=Continue%20Adding%20Sign-In). 
2. Enter *CarCare* in the App name field, and *com.teamltt.carcare* in the Android package name field, then click on *Continue to Choose and configure services*. 
3. In the following screen, click on *Continue to Generate configuration files*. 
4. Click on *Download google-services.json*. After downloading the file, locate it and move it to the project's *app* folder. 
5. Build the project by going to Android Studio and clicking on *Build* then *Make Project*.
### Installation of actual application
To install the application on an Android smartphone, build the project then click *Run* then *Run 'app'*. When prompted for a deployment target, choose the desired device under *Connected Devices*, then click *OK*.
### Run instructions
To run the application first install it on an Android smartphone. After installation is complete find the app in the Android smartphone and click on it to run Car Care.
### Troubleshooting
During installation, after finishing importing project settings from Gradle, the SDK location may not be found. To resolve this navigate to the location of the project's folder and create a file called 'local.properties'.  Inside this file add the text `sdk.dir=C\:\\Users\\USERNAME\\AppData\\Local\\Android\\sdk`, replacing USERNAME with your Windows username. If using OS X add the text `sdk.dir=/Users/USERNAME/Library/Android/sdk`, replacing USERNAME with your username.
