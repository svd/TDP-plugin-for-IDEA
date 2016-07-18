package com.tdp.decorator;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.tdp.workspace.generator.Constants;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Date;

public class ModuleDecorator implements ProjectViewNodeDecorator {
    // For using only in the package
    static String START_FRAMING = " [";
    static String END_FRAMING = "]";

    private final static long TWO_DAY_DURATION = 172800000;

    public ModuleDecorator() {
        super();
        // Remove the old project view pane
        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
            ProjectView projectView = ProjectView.getInstance(project);
            AbstractProjectViewPane pane = projectView.getProjectViewPaneById("ProjectPane");
            if (pane != null) {
                if (pane.getWeight() == 0) { // "0" is a weight of the default pane
                    projectView.changeView("TDPProjectPane"); // It is need before removing old pane
                    projectView.removeProjectPane(pane);
                }
            }
        }
    }

    @Override
    public void decorate(ProjectViewNode node, PresentationData data) {
        DescriptionsCache cache = DescriptionsCache.getInstance();
        VirtualFile nodeVirtualFile = node.getVirtualFile();
        if (nodeVirtualFile != null) {
            String fileName = nodeVirtualFile.getName();
            if (!cache.hasDescriptionForModule(fileName) && nodeVirtualFile.isDirectory() && fileName.contains("000")) {
                File propFile = new File(Constants.PATH_TO_DESCRIPTIONS);
                long now = new Date().getTime();
                long lastMod = propFile.lastModified();
                if (now - lastMod > TWO_DAY_DURATION){
                    try {
                        cache.update();
                    } catch (IOException | ParserConfigurationException | SAXException e) {
                        e.printStackTrace();
                    }
                }
            }
            addModuleDescription(data, cache.getDescription(fileName));
        }
    }


    private void addModuleDescription(PresentationData data, String description) {
        if (description != null){
            String text = START_FRAMING + description + END_FRAMING;
            List<PresentableNodeDescriptor.ColoredFragment> coloredFragmentList = data.getColoredText();
            /*decorate module*/
            if (coloredFragmentList.size() > 0){
                PresentableNodeDescriptor.ColoredFragment nodeFragment = coloredFragmentList.get(0);
                /* copy text settings from first text attribute*/
                SimpleTextAttributes attr = nodeFragment.getAttributes();
                SimpleTextAttributes attrF = new SimpleTextAttributes(attr.getBgColor(), attr.getFgColor(), attr.getWaveColor(), 0);
                PresentableNodeDescriptor.ColoredFragment fragment = new PresentableNodeDescriptor.ColoredFragment(text, attrF);
                if (coloredFragmentList.size() > 1){
                    coloredFragmentList.add(1, fragment);
                } else {
                    coloredFragmentList.add(fragment);
                }
            } else { //decorate node
                data.setPresentableText(data.getPresentableText() + text);
            }
        }
    }

    @Override
    public void decorate(PackageDependenciesNode node, ColoredTreeCellRenderer cellRenderer) {
    }
}