package sjosten.android.gasfinder.parser;

import java.util.List;

public class Main {
    public static void main(String[] args) throws PanicException {
        if(args.length != 1) {
            throw new IllegalArgumentException("Wrong number of args, should be 1 but is " + args.length);
        }

        List<GasStation> stations = null;
        // Check file extension
        if(args[0].contains(".kml")) {
            KMLParser kml = new KMLParser();
            kml.parseFile(args[0]);
            stations = kml.parseDocument();
        }
        else {
            TXTParser txt = new TXTParser();
            stations = txt.parseFile(args[0]);
        }
        
        for(GasStation gs : stations) {
            System.out.println(gs);
        }
    }
}
