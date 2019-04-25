import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class ProdCons2 {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        //The number of producers that will exist
        System.out.println("Enter the number of producers: ");
        int numberOfProducers = sc.nextInt();
        //Number of consumers
        System.out.println("Enter the number of consumers: ");
        int numberOfConsumers = sc.nextInt();
        //The maximum size of the buffer
        System.out.println("Enter the size of the buffer: ");
        int sizeOfBuffer = sc.nextInt();
        Buffer buffer = new Buffer(sizeOfBuffer);

        //Declare the number of Producers and Consumers that will exist
        Thread[] producers = new Thread[numberOfProducers];
        Thread[] consumers = new Thread[numberOfConsumers];

        System.out.println("Number of producers are " + numberOfProducers + ".");
        System.out.println("Number of consumers are " + numberOfConsumers + ".");

        //Initiate and start each Producer and Consumer
        for(int i = 0; i < numberOfProducers; i++){
            producers[i] = new Thread(new Producer(buffer));
            producers[i].start();
        }
        for(int i = 0; i < numberOfConsumers; i++){
            consumers[i] = new Thread(new Consumer(buffer));
            consumers[i].start();
        }

    }

}

class Buffer {

    //The buffer and it's size
    private LinkedList<String> buffer;
    private int sizeOfBuffer;

    //The serversocket along with the selected port number
    private ServerSocket serverSocket;
    private static final int PORT = 1234;
    //The thread handler is the one that listens for new producers and consumers clients
    private Thread serverHandler;

    public Buffer(int sizeOfBuffer) {
        //The linked list is used as the buffer
        this.sizeOfBuffer = sizeOfBuffer;
        buffer = new LinkedList<String>();
        try {
            //set up the serversocket
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e){
            System.out.println("Unable to set up a server");
            e.printStackTrace();
        }
        //Set up the server handler (as a new thread)
        serverHandler = new Thread(new ServerHandler(serverSocket, this));
        serverHandler.start();
    }

    //This is for the producers. buffer.put("10") adds "10" to the buffer
    public synchronized void put(String packet) {
        //While the buffer is full, wait()
        while(buffer.size() == sizeOfBuffer) {
            try {
                wait();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
        //Adds the content to the Linked List
        buffer.add(packet);
        //Notify that the list isn't empty anymore
        notifyAll();
    }

    //For consumers: buffer.get() returns the values as FIFO basis
    public synchronized String get() {
        //While the buffer is empty, wait() because you can't get anything
        while(buffer.size() == 0) {
            try {
                wait();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }

        //Get the first added value in the linked list
        String packet = buffer.removeFirst();
        //notify that the buffer is not full, and return the content
        notifyAll();
        return packet;
    }

}

class Producer implements Runnable {

    //The buffer used has a Linked List structure used as a queue
    private Buffer buffer;
    //To get the local host as well as the selected port number, and socket
    private InetAddress host;
    private final int PORT = 1234;
    private Socket socket;
    //To send requests to the server as well as receiving responses
    private BufferedReader in;
    private PrintWriter out;

    public Producer(Buffer buffer) {
        //buffer used is a Linked List used as a queue
        this.buffer = buffer;
        try {
            //to set up local server and in and out for sending and receiving requests
            host = InetAddress.getLocalHost();
            socket = new Socket(host, PORT);
            in = new BufferedReader(
                  new InputStreamReader(
                    socket.getInputStream()));
            out = new PrintWriter(
                    socket.getOutputStream(), true);
        } catch(UnknownHostException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            int i = 0;
            Random random = new Random();
            String message = "";
            String response = "";
            while(true) {
                //Thread.sleep() was at the end of the while loop
                //We found it better at the beginning because not all producers "spam" messages at the same time
                //The sleep is between 1 and 2 seconds
                Thread.sleep(random.nextInt(11) * 100 + 1000);
                //Message is "packet 0", "packet 1", "packet 2", etc...
                message = "Packet " + i;
                //Send a request to put this in the buffer
                out.println(message);
                //Wait for a response; the response would be the message sent
                response = in.readLine();
                System.out.println("Produced: " + response);
                i++;
            }
        } catch(IOException e) {
            e.printStackTrace();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}


/*
  This is the consumer class. Consumers request the values from the buffer.
*/
class Consumer implements Runnable {

    //The buffer used has a Linked List structure used as a queue
    private Buffer buffer;
    //To get the local host as well as the selected port number, and socket
    private InetAddress host;
    private final int PORT = 1234;
    private Socket socket;
    //To send requests to the server as well as receiving responses
    private BufferedReader in;
    private PrintWriter out;

    public Consumer(Buffer buffer) {
        //buffer used is a Linked List used as a queue
        this.buffer = buffer;
        try {
            //to set up local server and in and out for sending and receiving requests
            host = InetAddress.getLocalHost();
            socket = new Socket(host, PORT);
            in = new BufferedReader(
                  new InputStreamReader(
                    socket.getInputStream()));
            out = new PrintWriter(
                    socket.getOutputStream(), true);
        } catch(UnknownHostException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try{
            Random random = new Random();
            String request = "get";
            String response = "";
            while(true) {
                //Thread.sleep() was at the end of the while loop
                //We found it better at the beginning because not all consumers "spam" requests at the same time
                //The sleep is between 1 and 2 seconds
                Thread.sleep(random.nextInt(11) * 100 + 1000);
                //Request "get" from the local host
                out.println(request);
                //Wait for a response; the response would be the message requests
                response = in.readLine();
                System.out.println("Consumed: " + response);
            }
        } catch(IOException e) {
            e.printStackTrace();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class ServerHandler implements Runnable {

    //Buffer is a linked list used as a server
    private Buffer buffer;
    private Thread client;
    private Socket socket;
    private ServerSocket serverSocket;

    //Gets the local host server set up and the buffer used
    public ServerHandler(ServerSocket serverSocket, Buffer buffer) {
        this.serverSocket = serverSocket;
        this.buffer = buffer;
    }

    //Both producers and consumers are seen as clients.
    //Listens to sockets that will want to be clients
    //Creates them as new threads
    public void run() {
        try {
            while(true) {
                socket = serverSocket.accept();
                // System.out.println("New client accepted");
                client = new Thread(new ClientHandler(socket, buffer));
                client.start();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {

    //The buffer is brought in so that the client handler may use it
    private Buffer buffer;
    private Socket client;
    //Creates in and out to read requests and send requests
    private BufferedReader in;
    private PrintWriter out;

    //sets up the client socket and the buffer used as well as the methods of communication
    public ClientHandler(Socket client, Buffer buffer) {
        this.client = client;
        this.buffer = buffer;
        try {
            in = new BufferedReader(
                  new InputStreamReader(
                    client.getInputStream()));
            out = new PrintWriter(
                    client.getOutputStream(), true);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    //the main place where values are added to the buffer and requested from the buffer
    //When the message is "get", the consumer is requesting a value from the buffer
    //Otherwise it's treated as a value wanting to be added to the buffer
    public void run() {
        try {
            while(true) {
                String received = in.readLine();
                //Consumers .get()
                if(received.equals("get")) {
                    String message = buffer.get();
                    out.println(message);
                }
                //Producers .put()
                else {
                    buffer.put(received);
                    out.println(received);
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
