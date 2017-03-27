package com.tdp.workspace.generator;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.tdp.workspace.generator.fileutils.TdpPluginPropertiesReader;
import com.tdp.workspace.generator.utils.ModulesDepUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * Created by Siarhei_Nahel on 12/14/2016.
 */
public class UpdateArtifactory extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        String path = project.getBasePath();
        int input = Messages.showOkCancelDialog(project, "Update Artifactory Dependencies", "Do you want to update artifactory_tmp", null);
        if (input == 0) {
            try {
                ModulesDepUtil modulesDepUtil = ModulesDepUtil.getInstance(path);
                modulesDepUtil.updateArtifactory();
                Map<String, String> artifactory = modulesDepUtil.getArtifactoryModules();
                String baseModules = TdpPluginPropertiesReader.getInstance(path).getBaseModules();
                Controller controller = new Controller(getBaseModulesList(baseModules), path, true);
                NavigableSet<String> artifactoryModuleNames = new TreeSet<>();
                artifactoryModuleNames.addAll(artifactory.keySet());
                controller.updateModules(project, artifactoryModuleNames);

            } catch (IOException e) {
                e.printStackTrace();
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
