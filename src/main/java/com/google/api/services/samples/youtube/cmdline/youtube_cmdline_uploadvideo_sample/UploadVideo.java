package com.google.api.services.samples.youtube.cmdline.youtube_cmdline_uploadvideo_sample;

import java.awt.*;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
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
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

import java.util.logging.*;

import org.apache.commons.io.output.TeeOutputStream;
import org.xml.sax.SAXException;
import sun.java2d.pipe.SpanShapeRenderer;
import sun.rmi.server.Activation$ActivationSystemImpl_Stub;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class UploadVideo {

  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();
  private static YouTube youtube;
  private static String VIDEO_FILE_FORMAT = "video/*";
  private static String VERSION = "1.3.1";


  private static String videoTitle= "Video Upload"; //DEFAULT VIDEO TITLE
  private static String videoPrivacy= "private";
  private static String _videoTitle = "";

  /**
   * MAIN METHOD.
   * @param args
     */
  public static void main(String[] args) {

    try {
      if (args[0].contains("help")) {
        System.out.println("If you need help, please navigate to following page: \n" +
                "https://github.com/zekroTJA/youtubeUploader/blob/master/README.md");
        System.exit(1);
      }
    } catch (Exception e) {}

    if (getSettings().get(1).equals("true")) {
      try {
        FileOutputStream stream = new FileOutputStream("log.txt");
        TeeOutputStream output = new TeeOutputStream(System.out, stream);
        PrintStream ps = new PrintStream(output);
        System.setOut(ps);
      } catch (Exception e) {
        e.printStackTrace();
      }

    }

    System.out.println("[ " + getTime() + " ] STARTED SESSION\n");


    System.out.print(
                    "   #-----------------------------------# \n" +
                    "   | SIMPLE YOUTUBE UPLOADER V" + VERSION + "  | \n" +
                    "   | (c)2017 by zekro                  | \n" +
                    "   | http://zekro.jimdo.com            | \n" +
                    "   #-----------------------------------# \n" +
                    "   | This tool is unsing the official  | \n" +
                    "   | YouTube API (c) by Google.        | \n" +
                    "   #-----------------------------------# \n\n"
    );

    System.out.println(
                    "SETTINGS:\n" +
                    " ~ Video Title:        " + videoTitle + "\n" +
                    " ~ Video Privacy:      " + videoPrivacy + "\n" +
                    " ~ Video Path:         " + getSettings().get(0) + "\n" +
                    " ~ Write Logfile:      " + getSettings().get(1) + "\n" +
                    " ~ Resumable Upload:   " + getSettings().get(2) + "\n"
    );

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

          System.out.println("[ " + getTime() + " ] Video " + i+1 + " uplaoded successfully!\n\n");

        }

        System.out.println("[ " + getTime() + "] All files are uplaoded!");
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
      float videoSizeMB = getLocalVideoFiles()[0].length() / 1048576;
      System.out.print("Video file with the size of " + videoSizeMB + " MB detected to upload...\n\n");
      uploadVideo(getLocalVideoFiles()[0]);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  /**
   * Gets the arguments of command line and defines as static variables to use in further methods.
   * @param args
   */
  private static void getArguments(String[] args) {

    try {

      videoTitle = args[0];
      videoPrivacy = args[1];

    } catch(Exception e) {
    }
  }

  /**
   * Ckecks the client_secrets.json and the stat_settings.xml for existence.
   */
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

  /**
   * Gets the static script-settings out of the
   * @return
     */
  private static List<String> getSettings() {

    List<String> results = new ArrayList<String>();

    File xmlFile = new File(System.getProperty("user.dir") + "/stat_settings.xml");
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();


    try {

      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      org.w3c.dom.Document doc = documentBuilder.parse(xmlFile);
      doc.getDocumentElement().normalize();

      results.add(0, doc.getElementsByTagName("location").item(0).getTextContent());
      results.add(1, doc.getElementsByTagName("log").item(0).getTextContent());
      results.add(2, doc.getElementsByTagName("resumableUpload").item(0).getTextContent());


    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }


    return results;
  }

  /**
   * Transforms "getSettings().get(2)" to inversed boolean.
   * @return
     */
  private static boolean getresumableUpload(){
    if (getSettings().get(2).equals("false")) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Gets the current calender time and date and returns it as string (dd.MM.yyyy HH:mm:ss).
   * @return
   */
  private static String getTime() {
    Calendar cal = Calendar.getInstance();
    String timeDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(cal.getTime());
    return timeDate;
  }

  /**
   * Gets youtube API login credentials out of client_secrets.json.
   * @param scopes
   * @return
   * @throws Exception
   */
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

  /**
   * Upload method.
   * @param _videoFile
   */
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
      uploader.setDirectUploadEnabled(getresumableUpload());

      MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
        public void progressChanged(MediaHttpUploader uploader) throws IOException {
          switch (uploader.getUploadState()) {
            case INITIATION_STARTED:
              System.out.println("[ " + getTime() + " ] Initiation Started");
              break;
            case INITIATION_COMPLETE:
              System.out.println("[ " + getTime() + " ] Initiation Completed");
              break;
            case MEDIA_IN_PROGRESS:
              System.out.println("[ " + getTime() + " ] Upload percentage: " + new DecimalFormat("##.##").format(uploader.getProgress() * 100) + "%");
              break;
            case MEDIA_COMPLETE:
              System.out.println("[ " + getTime() + " ] Upload Completed!");
              break;
            case NOT_STARTED:
              System.out.println("[ " + getTime() + " ] Upload Not Started!");
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
   * Gets an array of videos in the video directory.
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

}