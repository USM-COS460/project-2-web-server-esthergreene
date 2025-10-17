/**
 * Name: MimeTypes
 * Author: esthergreene
 * Project: Program 2
 * Version: 10/05/2025
 * Use: A lookup helper that matches file extensions to their correct content type values
 *      so browsers know what kind of file they are receiving. Keeps a list of file extensions and 
 *      their matching MIME types. 
 * Note: Sources have been cited and appear above respective code (e.g., "References:")
 */

 /**
  * Resources:
  * https://www.w3schools.com/java/java_hashmap.asp
  * https://www.geeksforgeeks.org/java/map-interface-in-java/
  */
 import java.util.HashMap;
 import java.util.Map;

 /**
  * MimeTypes
  * A lookup helper that matches file extensions to their correct content type values
  * so browsers know what kind of file they are receiving. Keeps a list of file extensions and their matching MIME types.
  * Resources: 
  * https://www.w3schools.com/java/java_hashmap.asp
  */
 public class MimeTypes  {
    private static final Map<String, String> types = new HashMap<>();

    /**
     * static class
     * Shared helper function. Lets the server quickly find the correct content type for a file. Hopefully
     * it should create a new MimeTypes object each time. 
     * Resources:
     * https://www.w3schools.com/java/ref_hashmap_put.asp
     * https://stackoverflow.com/questions/27402992/java-a-way-to-match-mime-content-type-to-file-extension-from-commonsmultipart
     * https://www.baeldung.com/java-mime-type-file-extension
     * https://www.reddit.com/r/html5/comments/1i8dvxj/guidance_on_using_mime_types_vs_file_extensions/
     * https://docs.oracle.com/cd/E19957-01/817-7306/mimetypes-0/index.html
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/MIME_types/Common_types
     * https://www.iana.org/assignments/media-types/media-types.xhtml
     * https://www.sitepoint.com/mime-types-complete-list/
     * 
     */
    static {
        types.put("html", "text/html");
        types.put("htm", "text/html");
        types.put("txt", "text/plain");
        types.put("css", "text/css");
        types.put("js", "application/javascript");
        types.put("png", "image/png");
        types.put("jpg", "image/jpeg");
        types.put("jpeg", "image/jpeg");
        types.put("gif", "image/gif");
        types.put("svg", "image/svg+xml");
        types.put("ico", "image/x-icon");
        types.put("mp3", "audio/mpeg");
        types.put("wav", "audio/wav");
        types.put("mp4", "video/mp4");
        types.put("json", "application/json");
        types.put("pdf", "application/pdf");
        types.put("zip", "application/zip");
        types.put("xml", "application/xml");
    }

    /**
     * getMimeType
     * Looks at a file's extension (.html, .jpg, etc.) and returns the correct MIME type. It takes the
     * file name, finds the file extension (looking for the ".") and then looks up the extension. It then 
     * returns the matching MIME type. If not found, default is a generic binary file.
     * Resources:
     * https://www.geeksforgeeks.org/java/java-lang-string-lastindexof-method/
     * https://www.w3schools.com/java/ref_string_length.asp
     * https://www.w3schools.com/java/ref_string_substring.asp
     * https://www.w3schools.com/java/ref_string_tolowercase.asp
     * https://stackoverflow.com/questions/20508788/do-i-need-content-type-application-octet-stream-for-file-download
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/MIME_types
     * @param filename
     * @return
     */
    public static String getMimeType(String filename) {
        String ext = "";
        int i = filename.lastIndexOf('.');

        if (i >= 0 && i < filename.length() - 1) {
            ext = filename.substring(i + 1).toLowerCase();
        }

        String t = types.get(ext);
    
        if (t != null) {
            return t;
        }

        if ("txt".equals(ext)) {
            return "text/plain";
        }

        return "application/octet-stream";
    }
 }