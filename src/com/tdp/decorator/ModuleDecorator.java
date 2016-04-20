 package com.tdp.decorator;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.util.*;
import java.util.List;

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

            VirtualFile nodeVirtualFile = node.getVirtualFile();
            String fileName = nodeVirtualFile.getName();
            if (!descriptionCache.containsKey(fileName)) {


                VirtualFile f = nodeVirtualFile.findChild(PROJECT);
                if (f != null) {
                    String description = extractDescription(f);
                    if (StringUtil.isNotEmpty(description)) {
                        descriptionCache.put(fileName, description);
                        System.out.println(fileName + " * " + description);
                    }
                }


            }

            addModuleDescription(data, descriptionCache.get(fileName));

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
        if (description != null){
            String text = " [" + description + "]";
            List<PresentableNodeDescriptor.ColoredFragment> coloredFragmentList = data.getColoredText();

            /*decorate module*/
            if (coloredFragmentList.size() > 0){
                SimpleTextAttributes attr = new SimpleTextAttributes(Font.PLAIN, Color.black);
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
}
