package com.company;

import java.util.ArrayList;

/**
 * Start of printedBook class initializes pages and type
 */
public class PrintedBook extends Book {

    private int totalPages;
    private String type; // paperback or hardcover

    // default constructor
    public PrintedBook(){
        this.totalPages = 0;
        this.type = "";
    }

    /**
     * Custom constructor
     * @param title
     * @param location
     * @param yearPub
     * @param authorList
     * @param publisher
     * @param pages
     * @param type
     */
    public PrintedBook(String title, String location, int yearPub,
                       ArrayList<Author> authorList, Publisher publisher,
                       int pages, String type){
        super(title, location, yearPub, authorList, publisher);

        this.totalPages = pages;
        this.type = type;

    }

    /**
     * Gets total pages or type of book
     * @return the type of book or totalPages
     */
    public int getTotalPages(){
        return this.totalPages;
    }
    public String getType(){
        return this.type;
    }

    /**
     * Custom setter
     * @param pages
     */
    // setters
    public void setTotalPages(int pages){
        this.totalPages = pages;
    }
    public void setType(String type){
        this.type = type;
    }

    // overrides the toString from the base Book class

    /**
     * Overrides toString method from books and returns the value from the server
     * @return returns the book info
     */
    @Override
    public String toString(){

        return "Ok: The book called '" + this.getTitle() + " published in " + this.getYearPub() + ". It is located at " + this.getLocationCode() + " and the authors are " + this.getAuthors();

    }

}
