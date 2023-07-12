package com.example.database_test.clustering;
import static com.example.database_test.MainActivity.vectorlist;

import java.io.IOException;


public class DatabaseReader {

    //method to read database
    public static void readDatabase() {
        for (String s : vectorlist) {
            System.out.println(s);
        }
    }
}
