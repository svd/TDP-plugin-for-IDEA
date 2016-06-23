package com.tdp.workspace.generator;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.impl.ProjectImpl;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.Properties;
import java.util.TreeSet;

/**
 * Created by Siarhei Nahel on 21.05.2016.
 */
public class ChooserBaseModules extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Properties descriptions = new Properties();
        try {
            File prFile = new File(Constants.PATH_TO_DESCRIPTIONS);
            FileInputStream ins = new FileInputStream(prFile);
            descriptions.load(ins);
        } catch (IOException ex) {

        }
        Project project = e.getProject();
        String path = project.getBasePath();
        InputModulesValidator validator = new InputModulesValidator(
                ReadFileUtil.getAllTDPModulesFromProjectDir(path));
        String baseModulesFromProperty = null;
        StringBuilder message = new StringBuilder("Base modules (for example: 00005555;00005556)");

        try {
            baseModulesFromProperty = TdpPluginPropertiesReader.getInstance(path).getBaseModules();
            if (baseModulesFromProperty != null && !baseModulesFromProperty.isEmpty()) {
                message.append("\n \nYou have added these modules before:\n");
                List<String> list = getBaseModulesList(baseModulesFromProperty);
                for (String number : list) {
                    message.append(number).append(" - ").append(descriptions.getProperty(number)).append("\n");
                }
                message.append(" \n");
            }
        } catch (IOException e1) {
        }

        String baseModules = Messages.showInputDialog(project, message.toString(),
                "Choose base modules", null, baseModulesFromProperty, new InputValidator() {
                    @Override
                    public boolean checkInput(String s) {
                        return validator.validate(s);
                    }

                    @Override
                    public boolean canClose(String s) {
                        return checkInput(s);
                    }
                });
        if (baseModules != null){
            List<String> modules = getBaseModulesList(baseModules);
            String newNameProject = descriptions.getProperty(modules.get(0));
            if (newNameProject != null) {
                ProjectImpl projectImpl = (ProjectImpl) project;
                projectImpl.setProjectName(newNameProject);
            }
            NavigableSet<String> baseModulesList = new TreeSet<>();
            baseModulesList.addAll(modules);
            Controller controller = new Controller(baseModulesList, path);
            try {
                controller.generateWorkspace(project);
                TdpPluginPropertiesReader.getInstance(path).setProperty("baseModules", baseModules);
            } catch (IOException | TransformerException| ParserConfigurationException | SAXException e1) {
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