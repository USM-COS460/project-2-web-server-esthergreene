/**
 * Name: MyHTTPServer
 * Author: esthergreene
 * Project: Program 2
 * Version: 10/03/2025
 * Use: The main class used to start the web server. Reads port number and doc root
 *      from the command line. Opens a ServerSocket to listen for incoming browser connections.
 *      When a client connects it will hand the socket off to an HTTPHandler (eventually).
 * Note: Sources have been cited and appear above respective code (e.g., "References:")
 */

/**
 * References:
 * https://www.w3schools.com/java/java_files.asp
 * https://docs.oracle.com/javase/8/docs/api/java/io/IOException.html
 * https://docs.oracle.com/javase/8/docs/api/java/net/ServerSocket.html
 * https://www.geeksforgeeks.org/java/java-net-socket-class-in-java/
 * https://www.geeksforgeeks.org/java/java-util-concurrent-executorservice-interface-with-examples/
 * https://www.geeksforgeeks.org/java/java-util-concurrent-executor-interface-with-examples/
 */
 import java.io.File;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;

 public class MyHTTPServer {
    private final int port;
    private final File docRoot;
    private final ExecutorService threadPool;
    private volatile boolean isItrunning = true;
    private final String serverName = "MyJavaHTTPServer/1.0";

    /**
     * MyHTTPSever
     * Use: Contructor used to create a new MyHTTPServer object. Stores the port number, doc root
     *      and creates a pool of worker threads so the server can handle many clients running.
     * References: 
     * 
     * @param port
     * @param docRoot
     * @param numThreads
     */
    public MyHTTPServer(int port, File docRoot, int numThreads) {
        this.port = port;
        this.docRoot = docRoot;
        this.threadPool = Executors.newFixedThreadPool(numThreads);
    }
 }
