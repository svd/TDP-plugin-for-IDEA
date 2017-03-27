package com.tdp.workspace.generator;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.tdp.workspace.generator.fileutils.ReadFileUtil;
import com.tdp.workspace.generator.fileutils.TdpPluginPropertiesReader;
import com.tdp.workspace.generator.utils.InputModulesValidator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * Created by Siarhei_Nahel on 11/10/2016.
 */
public class UpdateModulesAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        String path = project.getBasePath();
        NavigableSet<String> allModules = ReadFileUtil.getAllTDPModulesFromProjectDir(path);
        InputModulesValidator validator = new InputModulesValidator(allModules);
        String baseModulesFromProperty = null;
        boolean artifactoryDep = false;
        String pathToArtifactoryTmp = null;
        try {
            pathToArtifactoryTmp = TdpPluginPropertiesReader.getInstance(path).getArtifactoryTmpDer();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (pathToArtifactoryTmp.isEmpty()) {
            pathToArtifactoryTmp = path + Constants.SLASH + Constants.ARTIFACTORY_TMP;
        }
        File artifactoryTmpDir = new File(pathToArtifactoryTmp);
        boolean hasArtifactoryTmp = artifactoryTmpDir.exists();
        StringBuilder message = new StringBuilder("Enter module names for update (for example: 00005555;00005556):");
        if (hasArtifactoryTmp) {
            try {
                artifactoryDep = TdpPluginPropertiesReader.getInstance(path).getArtifactoryDep();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        Pair<String, Boolean> pair = Messages.showInputDialogWithCheckBox(message.toString(), "Input base modules",
                "Use dependencies from artifactory_tmp", artifactoryDep, hasArtifactoryTmp, null, baseModulesFromProperty, new InputValidator() {
                    @Override
                    public boolean checkInput(String s) {
                        if (s.equals(Constants.ALL_MODULES_STRING)) {
                            return false;
                        }
                        return validator.validate(s);
                    }

                    @Override
                    public boolean canClose(String s) {
                        if (s.equals(Constants.ALL_MODULES_STRING)) {
                            return false;
                        }
                        return checkInput(s);
                    }
                });
        String baseModules = pair.getFirst();
        artifactoryDep = pair.getSecond();
        if (baseModules != null) {
            List<String> modules = getBaseModulesList(baseModules);
            Controller controller = new Controller(modules, path, artifactoryDep);
            try {
                NavigableSet<String> modulesSet = new TreeSet<>();
                modulesSet.addAll(modules);
                controller.updateModules(project, modulesSet);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private List<String> getBaseModulesList(String baseModulesString) {
        List<String> baseModulesList = new ArrayList<>();
        String[] modules = baseModulesString.trim().split(Constants.SEMICOLON);
        baseModulesList.addAll(Arrays.asList(modules));
        return baseModulesList;
    }
}
