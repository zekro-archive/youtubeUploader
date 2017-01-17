#JAVA YOUTUBE UPLOADER
####This is a simple java console youtube upload tool, primary for setting up an upload server with a raspberry pi.
---

###FIRST THINGS FIRST

First of all you need to enter your youtube API-ID and -Secrets in the **"client_secrets.json"** and define the video file location path in the **"stat_settings.xml"** and put them in the **same path like the java file**!
>If you dont have your API until yet, please jump to "CREATE YOUTUBE API-KEY"

You can now start the tool in the console with 
```bash
<p>$</p> java -jar ytUp.jar <video Title> <Privacy (private | public | unlisted)>
```
>*The two arguments are not required and will defaultly set to Title "Youtube Uplaod" and Privacy "private".*

---

###RUNNING ON RASPBERRY PI

####INSTALLATION

First you need to install unzip to unzip downloaded package:
```bash
$ sudo apt-get install unzip
```
Then download the package of the latest version (change version number to latest version):
```bash
$ wget "https://github.com/zekroTJA/youtubeUploader/releases/download/1.2.1/javayoutubeuploader_v1.2.1.zip"
```
Then create a folder where you want to install and unzip the package:
```shell
$ mkdir ytu/
$ cd ytu/
$ unzip javayoutubeuploader_v1.2.1.zip
$ rem javayoutubeuploader_v1.2.1.zip
```
Now enter your API-Key into the the *client_secrets.json* and insert the video location folder in the *stat_settings.xml*:
```bash
$ nano client_secrets.json
$ nano stat_settings.xml
```

####RUNNING THE TOOL

If you want to control the tool via SSH, I recommend to install the screen package:
```bash
$ sudo apt-get install screen
```
After that, put the video files in the installed folder and start the tool in the installed location:
```bash
$ screen java -jar ytUp.jar <video Title> <Privacy (private | public | unlisted)> 
```
>The two arguments are not required and will defaultly set to Title "Youtube Uplaod" and Privacy "private".

---

###CREATE YOUTUBE API-KEY

1. Open the [Google API Console](https://console.developers.google.com)
2. Click *"Project"* -> *"Create Project"*
3. Search for *"YouTube Data API v3"* and chose it
4. Press *"ENABLE API"*
5. Go to *"Credentials"* -> *"Create"* -> *"OAuth client ID"*
6. Chose *"Other"*, enter a name and klick *"Create"*
7. Then copy your client ID and your client secret and enter both in the "client_secrets.json" file.

---

###COPYRIGHT

Used code snippets by Google: https://code.google.com/archive/p/youtube-api-samples

Used YouTube API by Google: https://developers.google.com/youtube/v3/libraries
