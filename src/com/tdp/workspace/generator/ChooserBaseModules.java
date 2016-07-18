package com.tdp.workspace.generator;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Siarhei Nahel on 21.05.2016.
 */
public class ChooserBaseModules extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        DescriptionsCache cache = DescriptionsCache.getInstance();
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
                    message.append(number).append(" - ").append(cache.getDescription(number)).append("\n");
                }
                message.append(" \n");
            }
        } catch (IOException e1) {
            e1.printStackTrace();
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
            Controller controller = new Controller(modules, path);
            try {
                controller.generateWorkspace(project);
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