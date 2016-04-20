package com.tdp.decorator;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mikalai_Churakou on 4/19/2016.
 */
public class ModuleDecorator implements ProjectViewNodeDecorator {


    private static final String PROJECT = ".project";
    public static final String COMMENT = "comment";

    private Map<String,String> descriptionCache = new HashMap<String,String>();
    private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    @Override
    public void decorate(ProjectViewNode node, PresentationData data) {

        ModuleManager mm = ModuleManager.getInstance(node.getProject());
        for (Module module : mm.getModules()){

            /* decorate only module node */
            if (module.getName().equals(node.getName())){

                if (!descriptionCache.containsKey(module.getName())){
                    ModuleFileIndex moduleIndex = ModuleRootManager.getInstance(module).getFileIndex();

                    final String[] description = new String[1];

                    moduleIndex.iterateContent(new ContentIterator(){


                        @Override
                        public boolean processFile(VirtualFile fileOrDir) {

                            VirtualFile f = fileOrDir.findChild(PROJECT);

                            if (f != null){
                                description[0] = extractDescription(f);

                            }

                            return false;
                        }
                    });

                    descriptionCache.put(module.getName(), description[0]);

                }

                addModuleDescription(data, descriptionCache.get(module.getName()));


                break;

            }
        }

    }

    private String extractDescription(VirtualFile f) {

        String description = null;
        try {
            // use the factory to create a documentbuilder
            DocumentBuilder builder = factory.newDocumentBuilder();

            // create a new document from input stream
            Document doc = builder.parse(f.getInputStream());

            NodeList nodeList = doc.getElementsByTagName(COMMENT);

            if (nodeList != null && nodeList.getLength() > 0){
                description = nodeList.item(0).getTextContent();

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return description;
    }

    private void addModuleDescription(PresentationData data, String description) {
        if (description!= null){
            SimpleTextAttributes attr = new SimpleTextAttributes(Font.PLAIN, Color.black);
            PresentableNodeDescriptor.ColoredFragment fragment = new PresentableNodeDescriptor.ColoredFragment(description, attr);
            data.getColoredText().add(fragment);
        }


    }

    @Override
    public void decorate(PackageDependenciesNode node, ColoredTreeCellRenderer cellRenderer) {
    }
}
