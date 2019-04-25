package com.company;

import java.net.*;
import java.io.*;
import java.util.ArrayList;


public class Server {

    private Socket socket = null;
    private ServerSocket server = null;
    private ArrayList<Author> book1Authors = new ArrayList<Author>();
    private ArrayList<Book> bookShelf = new ArrayList<>();
    private Publisher p1 = new Publisher("Tor Books");



    public Server(int port) throws IOException{
        //deserialize in
        try {
            FileInputStream fileIn = new FileInputStream("books.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);

            bookShelf = (ArrayList<Book>) in.readObject();
        }
        catch (IOException i){
            i.printStackTrace();
        }
        catch (ClassNotFoundException c){
            System.out.println("Class not found");

        }
        //make some authors for validation
        Author author1 = new Author("Jordan");
        Author author2 = new Author("Sanderson");
        Author author3 = new Author("Lu");
        Author author4 = new Author("Herbert");
        book1Authors.add(author1);
        book1Authors.add(author2);
        book1Authors.add(author3);


        server = new ServerSocket(port);
        System.out.println("Server Started");
        System.out.println("Waiting for client...");

        while(true) {

            try{
                Socket socket = server.accept();
                System.out.println("Client Connected");

                new ServerThread(socket).start();

            }catch(Exception e){
                e.printStackTrace();
                System.out.println("Connection Error");
            }

            //serverThreadStart would happen here. everything here will go into that function

        }

    }


    class ServerThread extends Thread{
        private Socket socket;
        public ServerThread(Socket socket) throws IOException{
            this.socket = socket;
        }

        public void run(){

            DataInputStream input = null;
            PrintWriter output = null;

            try {
                input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);

                output.println("Add [title] [year] [location] [Authors last name] - to add book; List - get list of all book titles; Get [title] - get information about a specific book; Disconnect - to exit"
                );

                //input form user
                String line = input.readUTF();

                String[] commandArray = line.split(" ");
                // print out array as a test
                for (int i = 0; i < commandArray.length; i++) {
                    System.out.println(commandArray[i]);
                }
                //check first arg
                String firstArgument = commandArray[0];
                firstArgument = firstArgument.toUpperCase();
                if (!firstArgument.equals("DISCONNECT")) {
                    String title;
                    switch (firstArgument) {

                        case "ADD":
                            //check to make sure things are okay
                            if (commandArray.length < 5) {
                                output.println("Error: Some data is incorrect");
                                break;
                            }

                            //get title
                            title = commandArray[1];
                            String yr = commandArray[2];
                            int year = Integer.parseInt(yr);
                            String location = commandArray[3];

                            //add authors
                            ArrayList<Author> bookAuthors = new ArrayList<>();
                            for (int i = 4; i < commandArray.length; i++) {
                                Author author = new Author(commandArray[i]);
                                boolean exist = false;
                                for (Author a : book1Authors) {
                                    if (author.getName().equals(a.getName())) {
                                        exist = true;
                                    }
                                }
                                if (!exist) {
                                    output.println("Err: Some data is incorrect");
                                    break;
                                }

                                bookAuthors.add(author);
                            }


                            PrintedBook newBook = new PrintedBook(title, location, year, bookAuthors, p1, 123, "paperback");
                            bookShelf.add(newBook);
                            output.println("Ok: Added Book");

                            System.out.println("Printing bookshelf");
                            System.out.println(bookShelf);

                            break;

                        case "LIST":

                            String bookList = "";
                            for (Book b : bookShelf) {
                                bookList = bookList + b.getTitle() + ", ";
                            }
                            output.println(bookList);
                            break;
                        case "GET":

                            for (Book b : bookShelf) {
                                if (b.getTitle().equals(commandArray[1])) {
                                    output.println(b);
                                    //System.out.println(b);
                                    break;
                                }

                            }
                            output.println("ERR: No book with that title");
                            break;

                        default:

                            break;

                    }
                } else {

                    System.out.println("Closing Connection");
                    socket.close();
                    input.close();

                }

                //serialize books
                try {
                    FileOutputStream fileOut = new FileOutputStream("books.ser");
                    ObjectOutputStream out = new ObjectOutputStream(fileOut);
                    out.writeObject(bookShelf);


                } catch (IOException i) {
                    i.printStackTrace();
                }
            }catch(IOException i){
                i.printStackTrace();
            }
        }
    }

    public static void main (String[] args) throws IOException{

        Server server = new Server(5000);

    }


}
