package com.tdp.workspace.generator.project;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.platform.templates.BuilderBasedTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * TDPProjectTemplate
 *
 * @author Pavel Semenov (<a href="mailto:Pavel_Semenov1@epam.com"/>)
 */
public class TDPProjectTemplate extends BuilderBasedTemplate {
    private String name;
    private String description;

    public TDPProjectTemplate(String name, String description, ModuleBuilder builder) {
        super(builder);
        this.name = name;
        this.description = description;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public String getDescription() {
        return description;
    }
}
