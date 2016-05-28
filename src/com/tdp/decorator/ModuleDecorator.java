 package com.tdp.decorator;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.*;
import java.util.List;


public class ModuleDecorator implements ProjectViewNodeDecorator {

    private Properties descriptionCache = new Properties();
    private String pathToProperties = (System.getenv("USERPROFILE")+"/description.properties");
    private final static long TWO_DAY_DURATION = 172800000;


    @Override
    public void decorate(ProjectViewNode node, PresentationData data) {

        if (descriptionCache.size() == 0) {
            try {
                init();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
        }
        VirtualFile nodeVirtualFile = node.getVirtualFile();
        if (nodeVirtualFile != null) {
            String fileName = nodeVirtualFile.getName();
            if (!descriptionCache.containsKey(fileName) && nodeVirtualFile.isDirectory() && fileName.contains("000")) {
                File propFile = new File(pathToProperties);
                long now = new Date().getTime();
                long lastMod = propFile.lastModified();
                if (now - lastMod > TWO_DAY_DURATION){
                try {
                    update();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }}
            }
            addModuleDescription(data, descriptionCache.getProperty(fileName));
        }


    }


    private void addModuleDescription(PresentationData data, String description) {
        if (description != null){
            String text = " [" + description + "]";
            List<PresentableNodeDescriptor.ColoredFragment> coloredFragmentList = data.getColoredText();

            /*decorate module*/
            if (coloredFragmentList.size() > 0){
                PresentableNodeDescriptor.ColoredFragment nodeFragment = coloredFragmentList.get(0);
                /* copy text settings from first text attribute*/
                SimpleTextAttributes attr = nodeFragment.getAttributes();
                PresentableNodeDescriptor.ColoredFragment fragment = new PresentableNodeDescriptor.ColoredFragment(text, attr);
                coloredFragmentList.add(fragment);
            } else { //decorate node
                data.setPresentableText(data.getPresentableText() + text);
            }
        }


    }

    @Override
    public void decorate(PackageDependenciesNode node, ColoredTreeCellRenderer cellRenderer) {
    }

     private void init() throws IOException, ParserConfigurationException, SAXException {
         File prFile = new File(pathToProperties);
         if (!prFile.exists()){
             prFile.createNewFile();
             update();
         } else {
             FileInputStream ins = new FileInputStream(prFile);
             descriptionCache.load(ins);
         }
     }

     private void update() throws IOException, ParserConfigurationException, SAXException {
         parseHtml();
         FileOutputStream fos = null;
         try{
             fos = new FileOutputStream(new File(pathToProperties));
             descriptionCache.store(fos, null);
             fos.flush();
         } finally {
             if(fos != null){
                 fos.close();
             }
         }
     }

     private void parseHtml() throws IOException, ParserConfigurationException, SAXException {
         URL url = new URL("http://10.160.254.238/cvs/");
         String nameOutFile = System.getenv("TEMP") + "/repo.txt";
         OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(nameOutFile), "utf-8");
         InputStream in = url.openStream();
         int i;
         while((i=in.read())!=-1){
             out.write(i);
         }
         in.close();
         out.flush();
         out.close();
         Map<String, String> cache = Parser.descrFromHtml(new File(nameOutFile));
         descriptionCache.putAll(cache);
     }
}
