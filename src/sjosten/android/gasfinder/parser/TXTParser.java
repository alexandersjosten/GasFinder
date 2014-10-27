package sjosten.android.gasfinder.parser;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class TXTParser {
    public List<GasStation> parseFile(String fileName) throws PanicException {
        Scanner scan = null;
        try {
            scan = new Scanner(new File(fileName));
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }

        List<GasStation> list = new ArrayList<>();
        while(scan.hasNext()) {
            String line = scan.nextLine();
            String[] data = line.split(",");

            // Nasty solution, fucking UTF8 BOM...            
            char[] c1 = data[0].toCharArray();
            char[] c1Correct = null;
            if((int)c1[0] == 65279) {
                c1Correct = new char[c1.length - 1];
                for(int i = 1; i < c1.length; i++) {
                    c1Correct[i - 1] = c1[i];
                }
            }
            
            char[] c2 = data[1].toCharArray();
            char[] c2Correct = null;
            if((int)c2[0] == 65279) {
                c2Correct = new char[c2.length - 1];
                for(int i = 1; i < c2.length; i++) {
                    c2Correct[i - 1] = c2[i];
                }
            }
            
            double pos1 = 0.0;
            if(c1Correct != null) {
                pos1 = Double.parseDouble(new String(c1Correct));
            }
            else {
                pos1 = Double.parseDouble(new String(c1));
            }

            double pos2 = 0.0;
            if(c2Correct != null) {
                pos2 = Double.parseDouble(new String(c2Correct));
            }
            else {
                pos2 = Double.parseDouble(new String(c2));
            }

            String street = data[3].replace("\"", "").trim();
            street = street.charAt(0) + street.substring(1).toLowerCase();
            String city = data[2].replace("\"", "").trim();
            city = city.charAt(0) + city.substring(1).toLowerCase();
            
            list.add(new GasStation(new Coordinate(pos1, pos2), street, city));
        }
        
        scan.close();
        return list;
    }
}
