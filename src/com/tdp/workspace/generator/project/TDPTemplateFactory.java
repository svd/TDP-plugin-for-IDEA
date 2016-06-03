package com.tdp.workspace.generator.project;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TDPTemplateFactory
 *
 * @author Pavel Semenov (<a href="mailto:Pavel_Semenov1@epam.com"/>)
 */
public class TDPTemplateFactory extends ProjectTemplatesFactory {
    public static final String GROUP_NAME = "TDP";

    @NotNull
    @Override
    public String[] getGroups() {
        return new String[] { GROUP_NAME };
    }

    @NotNull
    @Override
    public ProjectTemplate[] createTemplates(@Nullable String group, WizardContext context) {
        Project project = context.getProject();
        List<ProjectTemplate> projectTemplates = new ArrayList<>();
        if (project == null) {
            projectTemplates.add(new TDPProjectTemplate("TDP Project", "Creates a new TDP project", new TDPProjectBuilder()));
        } else {
            // Add modules templates here
        }
        return projectTemplates.toArray(new ProjectTemplate[projectTemplates.size()]);
    }

    @Override
    public Icon getGroupIcon(String group) {
        return IconLoader.getIcon("/images/favicon.png");
    }
}
