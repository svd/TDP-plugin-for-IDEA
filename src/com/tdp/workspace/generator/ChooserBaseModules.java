package com.tdp.workspace.generator;

import com.intellij.openapi.util.Pair;
import com.tdp.decorator.DescriptionsCache;
import com.tdp.workspace.generator.fileutils.ReadFileUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.tdp.workspace.generator.fileutils.TdpPluginPropertiesReader;
import com.tdp.workspace.generator.utils.InputModulesValidator;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.NavigableSet;

/**
 * Created by Siarhei Nahel on 21.05.2016.
 */
public class ChooserBaseModules extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {

        DescriptionsCache cache = DescriptionsCache.getInstance();
        Project project = e.getProject();
        String path = project.getBasePath();
        NavigableSet<String> allModules = ReadFileUtil.getAllTDPModulesFromProjectDir(path);
        InputModulesValidator validator = new InputModulesValidator(allModules);
        String baseModulesFromProperty = null;
        boolean artifactoryDep = false;
        File artifactoryTmpDir = new File(path + Constants.SLASH + Constants.ARTIFACTORY_TMP);
        boolean hasArtifactoryTmp = artifactoryTmpDir.exists();
        StringBuilder message = new StringBuilder("Base modules (for example: 00005555;00005556) or \"all\"");
        try {
            baseModulesFromProperty = TdpPluginPropertiesReader.getInstance(path).getBaseModules();
            if (hasArtifactoryTmp) {
                artifactoryDep = TdpPluginPropertiesReader.getInstance(path).getArtifactoryDep();
            }
            if (baseModulesFromProperty != null && !baseModulesFromProperty.isEmpty()) {
                message.append("\n \nYou have added these modules before:\n");
                if (baseModulesFromProperty.equals(Constants.ALL_MODULES_STRING)) {
                    message.append(Constants.ALL_MODULES_STRING).append("\n");
                } else {
                    List<String> list = getBaseModulesList(baseModulesFromProperty);
                    for (String number : list) {
                        message.append(number).append(" - ").append(cache.getDescription(number)).append("\n");
                    }
                    message.append(" \n");
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        Pair<String, Boolean> pair = Messages.showInputDialogWithCheckBox(message.toString(), "Choose base modules",
                "Use dependencies from artifactory_tmp", artifactoryDep, hasArtifactoryTmp, null, baseModulesFromProperty, new InputValidator() {
                    @Override
                    public boolean checkInput(String s) {
                        return validator.validate(s);
                    }

                    @Override
                    public boolean canClose(String s) {
                        return checkInput(s);
                    }
                });
        String baseModules = pair.getFirst();
        artifactoryDep = pair.getSecond();
        long startTime = new Date().getTime();
        if (baseModules != null) {
            List<String> modules = new ArrayList<>();
            if (baseModules.equals(Constants.ALL_MODULES_STRING)) {
                modules.addAll(allModules);
            } else {
                modules = getBaseModulesList(baseModules);
            }
             Controller controller = new Controller(modules, path, artifactoryDep);
             try {
                 controller.generateWorkspace(project);
                 controller.saveBaseModulesProperty(baseModules, artifactoryDep);
                 if (!baseModules.equals(Constants.ALL_MODULES_STRING)) {
                     controller.renameProject(project, modules.get(0));
                 }
             } catch (IOException | TransformerException| ParserConfigurationException | SAXException e1) {
                e1.printStackTrace();
             }
        }
        long endTime = new Date().getTime();
        System.out.println((endTime - startTime)/1000);
    }

    private List<String> getBaseModulesList(String baseModulesString) {
        List<String> baseModulesList = new ArrayList<>();
        String[] modules = baseModulesString.trim().split(Constants.SEMICOLON);
        baseModulesList.addAll(Arrays.asList(modules));
        return baseModulesList;
    }
}