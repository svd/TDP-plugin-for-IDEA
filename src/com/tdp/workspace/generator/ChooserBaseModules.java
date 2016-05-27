package com.tdp.workspace.generator;

import com.tdp.workspace.generator.fileutils.ReadFileUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * Created by Siarhei Nahel on 21.05.2016.
 */
public class ChooserBaseModules extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        NavigableSet<String> allModules = ReadFileUtil.getAllTDPModulesFromProjectDir(project.getBasePath());
        String baseModules = Messages.showInputDialog(project, "Base modules (for example: 00005555;00005556)",
                "Choose base modules", null, null, new InputValidator() {
                    @Override
                    public boolean checkInput(String s) {
                        String[] modules = s.split(Constants.SEMICOLON);
                        if (modules.length == 0){
                            return false;
                        }
                        int count = 0;
                        for (String module : modules){
                            if (allModules.contains(module)){
                                count++;
                            }
                        }
                        if (modules.length == count){
                            return true;
                        }
                       return false;
                    }

                    @Override
                    public boolean canClose(String s) {
                        return checkInput(s);
                    }
                });
        if (baseModules != null){
            NavigableSet<String> baseModulesList = new TreeSet<>();
            String[] modules = baseModules.split(Constants.SEMICOLON);
            for (String module : modules){
                baseModulesList.add(module);
            }
            Controller controller = new Controller(baseModulesList, project.getBasePath());
            try {
                controller.generateWorkspace(project);
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (TransformerException e1) {
                e1.printStackTrace();
            } catch (ParserConfigurationException e1) {
                e1.printStackTrace();
            } catch (SAXException e1) {
                e1.printStackTrace();
            }
        }
    }
}
