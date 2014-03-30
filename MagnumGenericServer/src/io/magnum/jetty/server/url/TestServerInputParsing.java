package io.magnum.jetty.server.url;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TestServerInputParsing {

    /**
     * @param args
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws FileNotFoundException {
        Scanner s = new Scanner(new File("/Users/yusun/Desktop/top-500.txt"));
        while(s.hasNext()) {
            System.out.println(s.nextLine().trim());
        }
    }

}
