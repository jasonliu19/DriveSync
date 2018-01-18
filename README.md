# About DriveSync
DrivSync sync files from your computer to Google Drive at the click of a button or the press of a hotkey. Intended as to act as Backup and Sync for Linux. This project implements Google's Drive REST API v3 in Java while using Gradle for build automation. Designed for any machine running Java, tested and verified as working on Windows and Linux.

# Installation
After downloading the repo, navigate to build/distributions and extract either the tar or zip file to the installation location of your choice. To start the program: 

Linux: Open shell in the folder and run ./drivesync
Windows: Open cmd or powershell in the folder and run ./drivesync.bat

It is highly recommended to create an alias/add the installation folder to your PATH to run DriveSync without having to navigate to the installation folder each time.

# Usage
All the following commands are for Unix based systems. For windows, simply add '.bat' to the end of the first part of the command:
./drivesync - Opens help
./drivesync -g - Starts DriveSync with GUI
./drivesync path/to/folder/that/you/want/to/upload - Does not start GUI, DriveSync will start to sync a folder to your Drive

# TODO
Transform characters which interfere with search (namely ") to - in file names, improve UI, add multistage uploading for files >10MB, improve command line arguments

