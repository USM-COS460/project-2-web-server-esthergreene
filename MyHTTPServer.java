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

 /**
  * References:
  * https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html
  */
 public class MyHTTPServer {
    private final int port;
    private final File docRoot;
    private final ExecutorService threadPool;
    private volatile boolean serverRunning = true;
    private final String serverName = "MyJavaHTTPServer/1.0";

    /**
     * MyHTTPSever
     * Use: Contructor used to create a new MyHTTPServer object. Stores the port number, doc root
     *      and creates a pool of worker threads so the server can handle many clients running.
     * References: 
     * https://www.tutorialspoint.com/java_concurrency/concurrency_newfixedthreadpool.htm
     * @param port
     * @param docRoot
     * @param numThreads
     */
    public MyHTTPServer(int port, File docRoot, int numThreads) {
        this.port = port;
        this.docRoot = docRoot;
        this.threadPool = Executors.newFixedThreadPool(numThreads);
    }

    /*
     * start()
     * Opens a door to the network for connection purposes. Waits for client to answer.
     * Should keep running until the server is exited. 
     * References:
     * https://www.geeksforgeeks.org/java/file-getabsolutepath-method-in-java-with-examples/
     * https://docs.oracle.com/javase/8/docs/api/java/net/ServerSocket.html
     * https://www.geeksforgeeks.org/java/java-net-serversocket-class-in-java/
     * https://docs.oracle.com/javase/8/docs/api/java/net/Socket.html
     * https://www.geeksforgeeks.org/java/difference-between-executorservice-execute-and-submit-method-in-java/
     * https://www.w3schools.com/java/ref_keyword_finally.asp
     */
    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started. Listening on port " + port + "...");
            System.out.println("Document Root: " + docRoot.getAbsolutePath());
            
            while (serverRunning) {
            Socket client = serverSocket.accept();
            client.setSoTimeout(30_000);
            threadPool.submit(new HTTPHandler(client, docRoot, serverName)); // won't work cuz doesn't exist yet. in theory.
            } 
        } finally {
            stop(); 
        }
    }

    /**
     * stop()
     * Stops the server loop for accepting new clients and shuts down all worker threads. In case of any active handlers.
     * References: 
     * https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html
     */
    public void stop() {
        serverRunning = false;
        threadPool.shutdownNow();
    }

    /**
     * usage()
    * Print statements on how to run the program from the command line. Shows the format and gives an example.
    */
    private static void usage() {
        System.out.println("Usage: java MyHTTPServer <port> <document_root> [threads]");
        System.out.println("Example: java MyHTTPServer 8080 ./www 50");
    } // mostly for me, so I can remember how I set up my code HAHA

    /**
     * main
     * The classic main method. 
     * Checks arguments to make sure the port and docRoot are given; calls usage() if not.
     * Parses the port number to make sure it's valid (between 1 and 65535 = 2^16).
     * Checks if docRoot exists. 
     * Parses thread count, defaulting to 50 if not explicity listed.
     * Creates the server, building an instance of an MyHTTPSerber object.
     * Starts the server by calling the start() method. 
     * References:
     * https://www.geeksforgeeks.org/java/system-exit-in-java/
     * https://www.w3schools.com/java/ref_string_length.asp
     * https://www.tutorialspoint.com/java/number_parseint.htm
     * https://www.geeksforgeeks.org/java/numberformatexception-in-java-with-examples/
     * https://www.geeksforgeeks.org/java/difference-between-system-out-println-and-system-err-println-in-java/
     * https://www.geeksforgeeks.org/java/file-exists-method-in-java-with-examples/
     * https://www.geeksforgeeks.org/java/file-isdirectory-method-in-java-with-examples/
     * https://docs.oracle.com/javase/8/docs/api/java/io/IOException.html
     * https://www.geeksforgeeks.org/java/throwable-getmessage-method-in-java-with-examples/
     * https://www.geeksforgeeks.org/java/throwable-printstacktrace-method-in-java-with-examples/
     * 
     * @param args
    */
    public static void main(String[] args) {
        if (args.length < 2) {
            usage();
            System.exit(1);
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
            if (port < 1 || port > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid port: " + args[0]);
            usage();
            System.exit(1);
            return;
        }

        File docRoot = new File(args[1]);
        if (!docRoot.exists() || !docRoot.isDirectory()) {
            System.err.println("Document root must be an existing directory: " + args[1]);
        }

        int threads = 50;
        if (args.length >= 3) {
            try {
                threads = Integer.parseInt(args[2]);
                if (threads <= 0) {
                    threads = 50;
                }
            } catch (NumberFormatException ignored) {}
        }

        MyHTTPServer server = new MyHTTPServer(port, docRoot, threads);
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Server failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
 }


