package com.google.api.services.samples.youtube.cmdline.youtube_cmdline_uploadvideo_sample;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.google.common.collect.Lists;

import com.sun.glass.ui.SystemClipboard;
import com.sun.imageio.plugins.common.InputStreamAdapter;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class UploadVideo {

  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();
  private static YouTube youtube;
  private static String VIDEO_FILE_FORMAT = "video/*";


  private static String videoTitle= "Video Upload"; //DEFAULT VIDEO TITLE
  private static String videoPrivacy= "private";
  private static String _videoTitle = "";

  private static void getArguments(String[] args) {

    try {

      videoTitle = args[0];
      videoPrivacy = args[1];

    } catch(Exception e) {
    }
  }

  private static void checkInputs() {
    File fSecrets = new File(System.getProperty("user.dir") + "/client_secrets.json");
    File fSettings = new File(System.getProperty("user.dir") + "/stat_settings.xml");

    if (!fSecrets.exists() || fSecrets.isDirectory()) {
      System.out.print("[ERROR] File 'client_secrets.json' does not exist in the program directory! Please download  the preset-file from the main GitHub-Page of this tool and enter your client secrets!");
      System.exit(1);
    }

    if (!fSettings.exists() || fSecrets.isDirectory()) {
      System.out.print("[ERROR] File 'stat_settings.xml' does not exist in the program directory! Please download  the preset-file from the main GitHub-Page of this tool and enter your settings!");
      System.exit(1);
    }
  }

  private static List<String> getSettings() {

    List<String> results = new ArrayList<String>();

    File xmlFile = new File(System.getProperty("user.dir") + "/stat_settings.xml");
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();


    try {

      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      org.w3c.dom.Document doc = documentBuilder.parse(xmlFile);
      doc.getDocumentElement().normalize();

      results.add(0, doc.getElementsByTagName("location").item(0).getTextContent());

    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }


    return results;
  }


  private static Credential authorize(List<String> scopes) throws Exception {

    /*GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
        JSON_FACTORY, UploadVideo.class.getResourceAsStream("/client_secrets.json"));*/

    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
        JSON_FACTORY, new FileInputStream(System.getProperty("user.dir") + "/client_secrets.json"));

    if (clientSecrets.getDetails().getClientId().startsWith("Enter")
        || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
      System.out.println(
          "Enter Client ID and Secret from https://code.google.com/apis/console/?api=youtube"
          + "into youtube-cmdline-uploadvideo-sample/src/main/resources/client_secrets.json");
      System.exit(1);
    }

    FileCredentialStore credentialStore = new FileCredentialStore(
        new File(System.getProperty("user.home"), ".credentials/youtube-api-uploadvideo.json"),
        JSON_FACTORY);

    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, scopes).setCredentialStore(credentialStore)
        .build();

    LocalServerReceiver localReceiver = new LocalServerReceiver.Builder().setPort(8080).build();

    return new AuthorizationCodeInstalledApp(flow, localReceiver).authorize("user");
  }

  public static void main(String[] args) {

    System.out.print(
            "#-----------------------------------# \n" +
            "| SIMPLE YOUTUBE UPLOADER V1.1      | \n" +
            "| (c)2017 by zekro                  | \n" +
            "| http://zekro.jimdo.com            | \n" +
            "#-----------------------------------# \n" +
            "| This tool is unsing the official  | \n" +
            "| YouTube API (c) by Google.        | \n" +
            "#-----------------------------------# \n\n");


    try {
      if (getLocalVideoFiles().length > 1) {

        long videoSize = 0;
        for (int i = 0; i <= getLocalVideoFiles().length -1; i++) {
          videoSize = videoSize + getLocalVideoFiles()[i].length();
        }
        float videoSizeMB = (videoSize/1048576);

        System.out.print("There are " + getLocalVideoFiles().length + " video files detected with the size of " + videoSizeMB + " MB to upload.\n\n" +
                "Do you want to uplaod these files? [Y/N]: ");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String userChoise = br.readLine();

        System.out.println(userChoise);

        if (userChoise == "n") {
          System.out.println("Programm will exit now...");
          System.exit(1);
        }

        for (int i = 0; i <= getLocalVideoFiles().length -1; i++) {

          checkInputs();
          getArguments(args);
          _videoTitle = videoTitle + " - BulkNbr " + i+1;

          try {
            uploadVideo(getLocalVideoFiles()[i]);
          } catch (IOException e) {
            e.printStackTrace();
          }

          System.out.println("Video " + i+1 + " uplaoded successfully!\n\n");

        }

        System.out.println("All files are uplaoded!");
        System.exit(1);
      }
    } catch(IOException e) {
      System.out.println("There are no video files in the location '" + getSettings().get(0) + "'!");
      System.exit(1);
    }

    checkInputs();
    getArguments(args);

    _videoTitle = videoTitle;
    try {
      uploadVideo(getLocalVideoFiles()[0]);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private static void uploadVideo(File _videoFile) {

    List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.upload");

    try {
      // Authorization.
      Credential credential = authorize(scopes);

      // YouTube object used to make all API requests.
      youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(
              "youtube-cmdline-uploadvideo-sample").build();

      // We get the user selected local video file to upload.

      File videoFile = _videoFile; //JUMP SECTION TO SELECT VIDEO FOR BULK VIDEO UPLAOD

      //File videoFile = getVideoFromUser();
      System.out.println("'" + videoFile + "' will upload now.");

      // Add extra information to the video before uploading.
      Video videoObjectDefiningMetadata = new Video();

      /*
       * Set the video to public, so it is available to everyone (what most people want). This is
       * actually the default, but I wanted you to see what it looked like in case you need to set
       * it to "unlisted" or "private" via API.
       */
      VideoStatus status = new VideoStatus();
      status.setPrivacyStatus(videoPrivacy);
      videoObjectDefiningMetadata.setStatus(status);

      // We set a majority of the metadata with the VideoSnippet object.
      VideoSnippet snippet = new VideoSnippet();

      /*
       * The Calendar instance is used to create a unique name and description for test purposes, so
       * you can see multiple files being uploaded. You will want to remove this from your project
       * and use your own standard names.
       */
      Calendar cal = Calendar.getInstance();
      snippet.setTitle(_videoTitle);
      snippet.setDescription(
              "Video uploaded via YouTube Data API V3 using the Java library " + "on " + cal.getTime());

      // Set your keywords.
      List<String> tags = new ArrayList<String>();
      tags.add("tags");
      snippet.setTags(tags);

      // Set completed snippet to the video object.
      videoObjectDefiningMetadata.setSnippet(snippet);

      InputStreamContent mediaContent = new InputStreamContent(
              VIDEO_FILE_FORMAT, new BufferedInputStream(new FileInputStream(videoFile)));
      mediaContent.setLength(videoFile.length());

      /*
       * The upload command includes: 1. Information we want returned after file is successfully
       * uploaded. 2. Metadata we want associated with the uploaded video. 3. Video file itself.
       */
      YouTube.Videos.Insert videoInsert = youtube.videos()
              .insert("snippet,statistics,status", videoObjectDefiningMetadata, mediaContent);

      // Set the upload type and add event listener.
      MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();

      /*
       * Sets whether direct media upload is enabled or disabled. True = whole media content is
       * uploaded in a single request. False (default) = resumable media upload protocol to upload
       * in data chunks.
       */
      uploader.setDirectUploadEnabled(false);

      MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
        public void progressChanged(MediaHttpUploader uploader) throws IOException {
          switch (uploader.getUploadState()) {
            case INITIATION_STARTED:
              System.out.println("Initiation Started");
              break;
            case INITIATION_COMPLETE:
              System.out.println("Initiation Completed");
              break;
            case MEDIA_IN_PROGRESS:
              System.out.println("Upload in progress");
              System.out.println("Upload percentage: " + uploader.getProgress());
              break;
            case MEDIA_COMPLETE:
              System.out.println("Upload Completed!");
              break;
            case NOT_STARTED:
              System.out.println("Upload Not Started!");
              break;
          }
        }
      };
      uploader.setProgressListener(progressListener);

      // Execute upload.
      Video returnedVideo = videoInsert.execute();

      // Print out returned results.
      System.out.println("\n================== Returned Video ==================\n");
      System.out.println("  - Id: " + returnedVideo.getId());
      System.out.println("  - Title: " + returnedVideo.getSnippet().getTitle());
      System.out.println("  - Tags: " + returnedVideo.getSnippet().getTags());
      System.out.println("  - Privacy Status: " + returnedVideo.getStatus().getPrivacyStatus());
      System.out.println("  - Video Count: " + returnedVideo.getStatistics().getViewCount());

    } catch (GoogleJsonResponseException e) {
      System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
              + e.getDetails().getMessage());
      e.printStackTrace();
    } catch (IOException e) {
      System.err.println("IOException: " + e.getMessage());
      e.printStackTrace();
    } catch (Throwable t) {
      System.err.println("Throwable: " + t.getMessage());
      t.printStackTrace();
    }
  }

  /**
   * Gets the user selected local video file to upload.
   */
  private static File getVideoFromUser() throws IOException {
    File[] listOfVideoFiles = getLocalVideoFiles();
    return getUserChoice(listOfVideoFiles);
  }

  /**
   * Gets an array of videos in the current directory.
   */
  private static File[] getLocalVideoFiles() throws IOException {

    String location = getSettings().get(0);

    File currentDirectory = new File(location + ".");
    //System.out.println("Video files from " + currentDirectory.getAbsolutePath() + ":");

    // Filters out video files. This list of video extensions is not comprehensive.
    FilenameFilter videoFilter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        String lowercaseName = name.toLowerCase();
        if (lowercaseName.endsWith(".webm") || lowercaseName.endsWith(".flv")
            || lowercaseName.endsWith(".f4v") || lowercaseName.endsWith(".mov")
            || lowercaseName.endsWith(".mp4")) {
          return true;
        } else {
          return false;
        }
      }
    };

    return currentDirectory.listFiles(videoFilter);
  }

  /**
   * Outputs video file options to the user, records user selection, and returns the video (File
   * object).
   *
   * @param videoFiles Array of video File objects
   */
  private static File getUserChoice(File videoFiles[]) throws IOException {

    if (videoFiles.length < 1) {
      throw new IllegalArgumentException("No video files in this directory.");
    }

    for (int i = 0; i < videoFiles.length; i++) {
      System.out.println(" " + i + " = " + videoFiles[i].getName());
    }

    BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
    String inputChoice;

    do {
      System.out.print("Choose the number of the video file you want to upload: ");
      inputChoice = bReader.readLine();
    } while (!isValidIntegerSelection(inputChoice, videoFiles.length));

    return videoFiles[Integer.parseInt(inputChoice)];
  }

  /**
   * Checks if string contains a valid, positive integer that is less than max. Please note, I am
   * not testing the upper limit of an integer (2,147,483,647). I just go up to 999,999,999.
   *
   * @param input String to test.
   * @param max Integer must be less then this Maximum number.
   */
  public static boolean isValidIntegerSelection(String input, int max) {
    if (input.length() > 9) return false;

    boolean validNumber = false;
    // Only accepts positive numbers of up to 9 numbers.
    Pattern intsOnly = Pattern.compile("^\\d{1,9}$");
    Matcher makeMatch = intsOnly.matcher(input);

    if (makeMatch.find()) {
      int number = Integer.parseInt(makeMatch.group());
      if ((number >= 0) && (number < max)) {
        validNumber = true;
      }
    }
    return validNumber;
  }
}