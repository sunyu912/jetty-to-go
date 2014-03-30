package io.magnum.jetty.server.url;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class TestCSVParsing {

    public static void main(String[] args) throws IOException {
        Reader in = new FileReader("/Users/yusun/Desktop/top-1m.csv");
        Writer out = new FileWriter("/Users/yusun/Desktop/top-500.txt");
        Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
        int index = 0;
        for (CSVRecord record : records) {
          out.write(record.get(1) + "\n");
          index++;
          if (index == 500) break;
        }
        out.close();
    }
}
