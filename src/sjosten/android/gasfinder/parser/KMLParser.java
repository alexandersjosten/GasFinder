package sjosten.android.gasfinder.parser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;
import java.util.ArrayList;

public class KMLParser {
    private Document dom;

    // Parse the file to a Document
    public void parseFile(String fileName) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(fileName);
        } catch(ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    // Parse the document for the important information
    public List<GasStation> parseDocument() throws PanicException {
        if(dom == null) {
            throw new PanicException("Document is null!");
        }

        Element root = dom.getDocumentElement();
        NodeList nodes = root.getElementsByTagName("Placemark");
        List<GasStation> stations = new ArrayList<>();

        for(int i = 0; i < nodes.getLength(); i++) {
            Coordinate coord = parseCoordinate((Element)nodes.item(i));
            String[] names = getStreetCity((Element)nodes.item(i));
            if(names.length == 1) {  // No addres..
                stations.add(new GasStation(coord, "", names[0]));
            }
            else {
                stations.add(new GasStation(coord, names[0], names[1]));
            }
        }
        
        return stations;
    }

    private String[] getStreetCity(Element elem) throws PanicException {
        Element name = (Element)elem.getElementsByTagName("name").item(0);
        Element addr = (Element)elem.getElementsByTagName("address").item(0);

        // If nothing in "address" tag, then we know "name" is of form name city phone
        // If "address" tag non-empty, "address" is of form street and "name" is of form name-city phone (mostly)
        // or of form name-city moreInfo phone or form name-city
        if(addr.getFirstChild() == null) {
            String[] info = name.getFirstChild().getNodeValue().split(" ");
            String[] ret = new String[1];
            ret[0] = Character.toUpperCase(info[1].trim().charAt(0)) + info[1].trim().substring(1).toLowerCase();
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

    private Coordinate parseCoordinate(Element elem) throws PanicException {
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
