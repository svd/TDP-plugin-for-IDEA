package com.tdp.workspace.generator;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.impl.ModuleManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesContainer;
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesContainerFactory;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.tdp.workspace.generator.fileutils.ReadFileUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Siarhei_Nahel on 6/3/2016.
 */
public class UpdateTDPLibraryAction extends AnAction {
    private static final String LIBRARY_NAME = "TDPLibraries";

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        int i = Messages.showOkCancelDialog(project, "Do you want to run the libraries generator?",
                "Update libraries", "Ok", "Cancel", null);
        if (i == 0) {
            generateLibs(project);
        }
    }

    private VirtualFile[] getAllProjectJars(Project project) {
        List<String> jars = ReadFileUtil.getJars(project.getBasePath());
        VirtualFile[] result = new VirtualFile[jars.size()];
        for (int i = 0; i < jars.size(); i++){
            VirtualFile file = VfsUtilCore.findRelativeFile(jars.get(i), project.getBaseDir());
            result[i] = StandardFileSystems.getJarRootForLocalFile(file);
        }
        return result;
    }

    private void generateLibs(Project project) {
        ModuleManager manager = ModuleManagerImpl.getInstance(project);
        LibraryTable libraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable(project);
        Library[] libraries = libraryTable.getLibraries();
        for (Library library : libraries) {
            CommandProcessor.getInstance().executeCommand(project, new Runnable() {
                public void run() {
                    ApplicationManager.getApplication().runWriteAction(new Runnable() {
                        public void run() {
                            libraryTable.removeLibrary(library);
                        }
                    });
                }
            }, null, null);
        }

        List<Module> modules = Arrays.asList(manager.getModules());

        for (Module module : modules) {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                public void run() {
                    ModuleRootModificationUtil.updateModel(module, new Consumer<ModifiableRootModel>() {
                        @Override
                        public void consume(ModifiableRootModel model) {
                            List<String> existLibrary = new ArrayList<String>();
                            OrderEntry[] orderEntries = model.getOrderEntries();
                            boolean hasTdpLib = false;
                            for (OrderEntry entry : orderEntries) {
                                try {
                                    LibraryOrderEntry orderEntry = (LibraryOrderEntry) entry;
                                    if (orderEntry.getLibraryName().equals(LIBRARY_NAME)) {
                                        hasTdpLib = true;
                                    } else {
                                        model.removeOrderEntry(orderEntry);
                                    }
                                } catch (ClassCastException e) {
                                }
                            }
                            if (!hasTdpLib) {
                                model.addInvalidLibrary(LIBRARY_NAME, "project");
                            }
                        }
                    });
                }
            });
        }
        VirtualFile[] jars = getAllProjectJars(project);
        LibrariesContainer container = LibrariesContainerFactory.createContainer(project);
        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    public void run() {
                        container.createLibrary(LIBRARY_NAME, LibrariesContainer.LibraryLevel.PROJECT, jars, jars);
                    }
                });
            }
        }, null, null);
    }

}
