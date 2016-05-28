package com.tdp.decorator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Created by Siarhei Nahel on 27.05.2016.
 */
public class Parser {
    private static final String START_TAG_LINE_1 = "<tr class=\"Line1\">";
    private static final String START_TAG_LINE_2 = "<tr class=\"Line2\">";
    private static final String CLOSE_TAG = "</tr>";
    private static final String HREF = " href=";
    private static final String HTML_SPACE = "&nbsp;";
    private static final String TD_TAG = "td";
    private static final String A_TAG = "a";
    private static final String CLASS_ATR = "class";
    private static final String PROJECT_NUMBER_ATR_NAME = "ProjectNumber";
    private static final String PROJECT_DESCRIPTION_ATR_NAME = "ProjectDescription";

    public static Map<String, String> descrFromHtml(File file) throws IOException, SAXException, ParserConfigurationException {
        Scanner scanner = new Scanner(file);
        Map<String, String> cache = new TreeMap<>();
        while (scanner.hasNext()){
            String line = scanner.nextLine().trim();
            if (line.equals(START_TAG_LINE_1) ||
                    line.equals(START_TAG_LINE_2)){
                StringBuilder fragment = new StringBuilder();
                while (!line.equals(CLOSE_TAG)){
                    fragment.append(clearLink(line));
                    line = scanner.nextLine().trim();
                }
                fragment.append(clearLink(line));
                Entry entry = getModuleDescr(fragment.toString());
                cache.put(entry.getKey(), entry.getValue());
            }
        }
        return cache;
    }

    private static String clearLink(String line){
        String resultLine = line.replace(HTML_SPACE, "");
        if (line.contains(HREF)){
            int startInd = line.indexOf(HREF);
            int endInd = line.indexOf("\">", startInd);
            if (endInd != -1){
                String link = line.substring(startInd, endInd+1);
                resultLine = line.replace(link, "");
            }
        }
        return resultLine;
    }

    private static Entry getModuleDescr(String tableLine) throws ParserConfigurationException, IOException, SAXException {
        Entry entry = new Entry();
        Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(tableLine)));

        NodeList nList = d.getElementsByTagName(TD_TAG);
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE){
                Element element = (Element)nNode;
                String atr = element.getAttribute(CLASS_ATR);
                if (atr.equals(PROJECT_NUMBER_ATR_NAME)){
                    Node numberNode = element.getElementsByTagName(A_TAG).item(0);
                    if (numberNode.getNodeType() == Node.ELEMENT_NODE){
                        Element num = (Element) numberNode;
                        entry.setKey(num.getFirstChild().getTextContent().trim());
                    }
                }
                if (atr.equals(PROJECT_DESCRIPTION_ATR_NAME)){
                    entry.setValue(element.getFirstChild().getTextContent().trim());
                }
            }
        }
        return entry;
    }

    private static class Entry{
        private String key;
        private String value;

        public Entry() {
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
