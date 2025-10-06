/**
 * Name: HTTPHandler
 * Author: esthergreene
 * Project: Program 2
 * Version: 10/05/2025
 * Use: Used in tandem with MyHTTPServer class. HTTPHandler reads the request from a browser and
 *      figures out what file the browser is asking fro inside the document root. It then sends a proper HTTP
 *      response back to the browser such as status line, required headers, and actual file contents (HTML, CSS, etc.)
 *      HTTPHandler then closes the connection when done (eventually).
 * Note: Sources have been cited and appear above respective code (e.g., "References:")
 */

/**
 * Resources:
 * https://www.tutorialspoint.com/java/io/index.htm
 * https://docs.oracle.com/javase/8/docs/api/java/net/Socket.html
 * https://www.geeksforgeeks.org/java/java-net-urldecoder-class-java/
 * https://www.geeksforgeeks.org/java/java-nio-charset-charset-class-in-java/
 * https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html
 * https://www.w3schools.com/java/java_packages.asp
*/
import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class HTTPHandler implements Runnable {
    private final Socket client;
    private final File docRoot;
    private final String serverName;
    private static final String CRLF = "\r\n";

    /**
     * HTTPHandler()
     * Creates a new HTTPHandler object and stores the 
     * below values inside the object for future use.
     * @param client
     * @param docRoot
     * @param serverName
    */
    public HTTPHandler(Socket client, File docRoot, String serverName) {
        this.client = client;
        this.docRoot = docRoot;
        this.serverName = serverName;
    }

    /**
     * run()
     * Listens for a request by opening an input stream (to read whatr the browser
     * is asking for) and an output stream (to send back a reply). It then reads the 
     * request, and locates the requested file. If the request is for a directory, it looks
     * for index.html. It checks if the requested file exists inside the docRoot. If the file
     * doesn't exist it prepares a "404 Not Found" error response. Then it writes back an HTTP 
     * response to the browser (status line, headers, body). Finally, ir finishes a session
     * by closing the file stream, flushing the output stream, and closing the socket. 
     * References:
     * https://www.tutorialspoint.com/java/lang/process_getinputstream.htm
     * https://www.geeksforgeeks.org/java/java-io-bufferedinputstream-class-java/
     * https://docs.oracle.com/javase/8/docs/api/java/io/OutputStream.html
     * https://www.tutorialspoint.com/java/java-socket-getoutputstream.htm
     * https://www.w3schools.com/java/ref_string_isempty.asp
     * https://www.w3schools.com/java/ref_string_trim.asp
     * https://www.w3schools.com/java/ref_string_split.asp
     * https://www.w3schools.com/java/ref_string_length.asp
     * https://www.w3schools.com/java/ref_string_equalsignorecase.asp
     * https://www.geeksforgeeks.org/java/file-exists-method-in-java-with-examples/
     * https://www.geeksforgeeks.org/java/file-isdirectory-method-in-java-with-examples/
     * https://www.geeksforgeeks.org/java/handle-an-ioexception-in-java/
     * https://www.w3schools.com/java/ref_keyword_finally.asp
     * https://www.w3schools.com/java/ref_scanner_close.asp
    */
    @Override
    public void run() {
        try (InputStream inRaw = client.getInputStream();
             BufferedInputStream in = new BufferedInputStream(inRaw);
             OutputStream out = client.getOutputStream();
             BufferedOutputStream bout = new BufferedOutputStream(out)) {

            List<String> lines = readRequestLines(in);
            if (lines.isEmpty()) {
                return;
            }

            String requestLine = lines.get(0).trim();
            String[] parts = requestLine.split("\\s+");
            if (parts.length < 3) { 
                sendSimpleResponse(bout, "400 Bad Request", "text/plain", "Malformed request");
                return;
            }

            String method = parts[0];
            String rawPath = parts[1]; 
            if (!"GET".equalsIgnoreCase(method)) { 
                sendSimpleResponse(bout, "501 Not Implemented", "text/plain", "Only GET supported");
                return;
            }

            String path = urlDecodePath(rawPath);
            File requestedFile = resolvePath(docRoot, path); 
            if (requestedFile == null || !requestedFile.exists() || requestedFile.isDirectory() && !tryServeIndex(requestedFile)) { 
                sendNotFound(bout);
            } else {
                if (requestedFile.isDirectory()) {
                    requestedFile = new File(requestedFile, "index.html");
                    if (!requestedFile.exists()) { 
                        sendNotFound(bout); 
                        return;
                    }
                }
                sendFile(bout, requestedFile); 
            }
        } catch (IOException e) { 
        } finally { 
            try { client.close(); } catch (IOException ignored) {}
        }
    }

    /**
     * readRequestLines()
     * Reads from the socket's input stream, looks atht he data the browser sent when connected.
     * Collects all the request lines until it reaches a blank line (\r\n).
     * Finally, it stores these lines in a list or array; the very first line is the request
     * line, which tells us the method, resource, and version- the headers are usually ignored, but 
     * still read in. 
     * References:
     * https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html
     * https://www.w3schools.com/java/java_bufferedreader.asp
     * https://www.geeksforgeeks.org/java/inputstreamreader-class-in-java/
     * https://docs.oracle.com/javase/8/docs/api/java/nio/charset/StandardCharsets.html
     * https://www.geeksforgeeks.org/java/console-readline-method-in-java-with-examples/
     * https://www.w3schools.com/java/ref_arraylist_add.asp
     * @param in
     * @return
     * @throws IOException
    */
    private static List<String> readRequestLines(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.ISO_8859_1)); 
        List<String> lines = new ArrayList<>();
        String line; 
        String requestLine = reader.readLine(); 
        if (requestLine == null) {
            return lines;
        } 
        lines.add(requestLine);
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) break; 
            lines.add(line);
        }
        return lines;
    }

    /**
     * urlDecodePath()
     * Turns a URL path into a normal file path for the server to use.
     * Strips query parameters, decodes URL-encoded characters and converts
     * these back to normal chararacter so the server can locate the actual file.
     * It then returns a clean path.
     * Resources:
     * https://www.w3schools.com/java/ref_string_indexof.asp
     * https://www.w3schools.com/java/ref_string_substring.asp
     * https://www.geeksforgeeks.org/java/java-long-decode-method-with-examples/
     * https://www.tutorialspoint.com/java/lang/enum_name.htm
     * https://stackoverflow.com/questions/6030059/url-decoding-unsupportedencodingexception-in-java
     * @param rawPath
     * @return
    */
    private static String urlDecodePath(String rawPath) {
        try {
            int q = rawPath.indexOf('?'); 
            String onlyPath = (q >= 0) ? rawPath.substring(0, q) : rawPath; 
            return URLDecoder.decode(onlyPath, StandardCharsets.UTF_8.name()); 
        } catch (UnsupportedEncodingException e) { 
            return rawPath;
        }
    }

    //

    /**
     * resolvePath()
     * 
     * References:
     * 
     * 
     * 
     * 
     * 
     * 
     * @param root
     * @param path
     * @return
     * @throws IOException
    */
    private static File resolvePath(File root, String path) throws IOException {
        if (path.startsWith("/")) path = path.substring(1);
        File f = new File(root, path);
        String canonicalRoot = root.getCanonicalPath();
        String canonicalTarget;
        try {
            canonicalTarget = f.getCanonicalPath();
        } catch (IOException e) {
            return null;
        }
        if (!canonicalTarget.startsWith(canonicalRoot)) {
            return null;
        }
        return new File(canonicalTarget);
    }

    private static boolean tryServeIndex(File dir) {
        if (!dir.exists() || !dir.isDirectory()) return false;
        File index = new File(dir, "index.html");
        return index.exists() && index.isFile();
    }

    private void sendNotFound(BufferedOutputStream out) throws IOException {
        String body = "<html><head><title>404 Not Found</title></head><body><h1>404 Not Found</h1></body></html>";
        sendResponseHeaders(out, "404 Not Found", "text/html; charset=utf-8", body.getBytes(StandardCharsets.UTF_8).length);
        out.write(body.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    private void sendSimpleResponse(BufferedOutputStream out, String status, String contentType, String message) throws IOException {
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        sendResponseHeaders(out, status, contentType + "; charset=utf-8", body.length);
        out.write(body);
        out.flush();
    }

    private void sendFile(BufferedOutputStream out, File file) throws IOException {
        String mime = MimeTypes.getMimeType(file.getName());
        long length = file.length();
        sendResponseHeaders(out, "200 OK", mime, length);

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            byte[] buffer = new byte[8192];
            int r;
            while ((r = bis.read(buffer)) != -1) {
                out.write(buffer, 0, r);
            }
            out.flush();
        }
    }

    private void sendResponseHeaders(BufferedOutputStream out, String status, String contentType, long contentLength) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(status).append(CRLF);
        sb.append("Date: ").append(rfc1123Date()).append(CRLF);
        sb.append("Server: ").append(serverName).append(CRLF);
        sb.append("Content-Type: ").append(contentType).append(CRLF);
        sb.append("Content-Length: ").append(contentLength).append(CRLF);
        sb.append("Connection: close").append(CRLF);
        sb.append(CRLF);
        out.write(sb.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    private static String rfc1123Date() {
        SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(new Date());
    }
}
