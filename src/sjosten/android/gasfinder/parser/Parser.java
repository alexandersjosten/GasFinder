package sjosten.android.gasfinder.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Parser {
	// Method for parsing a txt-file, only used when starting the app to get initial
	// data to the database
	public static List<GasStation> parseTXTFile(String fileName, InputStream is) throws PanicException {
        Scanner scan = null;
        scan = new Scanner(is);
        
        String name = fileName.substring(0, fileName.indexOf("."));
        name = Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase(Locale.getDefault());
        List<GasStation> list = new ArrayList<>();
        while(scan.hasNext()) {
            String line = scan.nextLine();
            String[] data = line.split(",");

            // Nasty solution, damn UTF8 BOM...            
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
            street = street.charAt(0) + street.substring(1).toLowerCase(Locale.getDefault());
            String city = data[2].replace("\"", "").trim();
            city = city.charAt(0) + city.substring(1).toLowerCase(Locale.getDefault());
            
            list.add(new GasStation(new Coordinate(pos1, pos2), name, street, city));
        }
        
        scan.close();
        return list;
    }
	
	// Parse the file to a Document when parsing KML document
    public static void parseKMLFile(String fileName, InputStream is) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            parseDocument(db.parse(is));
        } catch(ParserConfigurationException | SAXException | IOException | PanicException e) {
            e.printStackTrace();
        }
    }

    // Parse the KML document for the important information
    private static List<GasStation> parseDocument(Document dom) throws PanicException {
        if(dom == null) {
            throw new PanicException("Document is null!");
        }

        Element root = dom.getDocumentElement();
        
        // The actual name of the station, e.g. Shell. Is at the beginning of a KML-file.
        String name = root.getElementsByTagName("name").item(0).getTextContent();
        NodeList nodes = root.getElementsByTagName("Placemark");
        List<GasStation> stations = new ArrayList<>();

        for(int i = 0; i < nodes.getLength(); i++) {
            Coordinate coord = parseCoordinate((Element)nodes.item(i));
            String[] names = getStreetCity((Element)nodes.item(i));
            if(names.length == 1) {  // No addres..
                stations.add(new GasStation(coord, name, "", names[0]));
            }
            else {
                stations.add(new GasStation(coord, name, names[0], names[1]));
            }
        }
        
        return stations;
    }

    private static String[] getStreetCity(Element elem) throws PanicException {
        Element name = (Element)elem.getElementsByTagName("name").item(0);
        Element addr = (Element)elem.getElementsByTagName("address").item(0);

        // If nothing in "address" tag, then we know "name" is of form name city phone
        // If "address" tag non-empty, "address" is of form street and "name" is of form name-city phone (mostly)
        // or of form name-city moreInfo phone or form name-city
        if(addr.getFirstChild() == null) {
            String[] info = name.getFirstChild().getNodeValue().split(" ");
            String[] ret = new String[1];
            ret[0] = Character.toUpperCase(info[1].trim().charAt(0)) + info[1].trim().substring(1).toLowerCase(Locale.getDefault());
            return ret;
        }
        else {
            String infoAddr = addr.getFirstChild().getNodeValue().trim();
            String[] infoName = name.getFirstChild().getNodeValue().trim().split(" ");
            String[] ret = new String[2];
            ret[0] = infoAddr;
            
            if(infoName.length != 2) {  // Form name-city phone or name-city
                ret[1] = infoName[0];
            }
            else {  // Form name-city moreInfo phone
                ret[1] = infoName[0] + " " + infoName[1];
            }
            
            return ret;
        }
    }

    private static Coordinate parseCoordinate(Element elem) throws PanicException {
        // size of ns should be 1..
        NodeList ns = elem.getElementsByTagName("Point");
        Element e = (Element)ns.item(0);

        NodeList coords = e.getElementsByTagName("coordinates");
        // Should only be one value in coords..
        if(coords != null && coords.getLength() == 1) {
            Element e2 = (Element)coords.item(0);

            // val is of type "longitude,latitude"
            String val = e2.getFirstChild().getNodeValue();
            String[] vals = val.split(",");
            return new Coordinate(
                Double.parseDouble(vals[0]),
                Double.parseDouble(vals[1])
            );
        }
        else {
            throw new PanicException("Problem reading coordinates..");
        }
    }
}
