package com.company;


import javax.sound.midi.Soundbank;
import java.net.*;
import java.io.*;
import java.util.Scanner;


public class Client {

    private Socket socket = null;
    private BufferedReader input = null;
    private DataOutputStream output = null;
    private String clientResponse = "";


    public Client(String address, int port) throws IOException, UnknownHostException{

        socket = new Socket(address, port);
        System.out.println("Connected");


        // takes input from terminal
        input  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        // sends output to the socket
        output = new DataOutputStream(socket.getOutputStream());
        // string to read input from input
        String line = "";


        try (Scanner sc = new Scanner(System.in)) {

            clientResponse = input.readLine();
            System.out.println(clientResponse);

            System.out.println("Type in command separated by spaces: ");
            line = sc.nextLine();

            // sents to client line of commands as string
            output.writeUTF(line);

            // gets client response
            clientResponse = input.readLine();
            System.out.println(clientResponse);
            System.out.println("Hit any button to continue...");
            line = sc.nextLine();
        }

        System.out.println("Closing connection");
        input.close();
        output.close();
        socket.close();


    }


    public static void main(String args[]) throws IOException, UnknownHostException {
        Client client = new Client("localhost", 5000);

    }

}
