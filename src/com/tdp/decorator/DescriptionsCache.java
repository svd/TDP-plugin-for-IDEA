package com.tdp.decorator;

import com.tdp.workspace.generator.Constants;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Siarhei_Nahel on 7/15/2016.
 */
public class DescriptionsCache {

    private static DescriptionsCache instance;

    private static Properties descriptionCache = new Properties();
    private static String tempRepositoryFile = System.getenv("TEMP") + "/Repository.txt";
    private static final String URL_TO_CVS = "http://10.160.254.238/cvs/";

    private DescriptionsCache() {
    }

    public static DescriptionsCache getInstance(){
        if (instance == null) {
            instance = new DescriptionsCache();
            try {
                instance.init();
            } catch (IOException | ParserConfigurationException | SAXException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public String getDescription(String nameModule) {
        return descriptionCache.getProperty(nameModule);
    }

    public boolean hasDescriptionForModule(String nameModule) {
        return descriptionCache.containsKey(nameModule);
    }

    private void init() throws IOException, ParserConfigurationException, SAXException {
        File prFile = new File(Constants.PATH_TO_DESCRIPTIONS);
        if (!prFile.exists()){
            prFile.createNewFile();
            update();
        } else {
            FileInputStream ins = new FileInputStream(prFile);
            descriptionCache.load(ins);
        }
    }

    public void update() throws IOException, ParserConfigurationException, SAXException {
        parseHtml();
        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(new File(Constants.PATH_TO_DESCRIPTIONS));
            descriptionCache.store(fos, null);
            fos.flush();
        } finally {
            if(fos != null){
                fos.close();
            }
        }
    }

    private void parseHtml() throws IOException, ParserConfigurationException, SAXException {
        URL url = new URL(URL_TO_CVS);
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(tempRepositoryFile), "utf-8");
        InputStream in = url.openStream();
        int i;
        while((i=in.read())!=-1){
            out.write(i);
        }
        in.close();
        out.flush();
        out.close();
        Map<String, String> cache = Parser.descrFromHtml(new File(tempRepositoryFile));
        descriptionCache.putAll(cache);
    }
}
