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
import com.tdp.workspace.generator.fileutils.TdpPluginPropertiesReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;

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

    public static void generateLibs(Project project) {
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
                                if (entry instanceof LibraryOrderEntry) {
                                    LibraryOrderEntry orderEntry = (LibraryOrderEntry) entry;
                                    if (orderEntry.getLibraryName().equals(LIBRARY_NAME)) {
                                        hasTdpLib = true;
                                    } else {
                                        model.removeOrderEntry(orderEntry);
                                    }
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
        List<String> jars = ReadFileUtil.getJars(project.getBasePath());
        if (jars.isEmpty()) {
            return;
        }
        VirtualFile[] classes = getClasses(jars, project);
        VirtualFile[] sources = getSourcesFromRepository(jars, project);
        LibrariesContainer container = LibrariesContainerFactory.createContainer(project);
        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    public void run() {
                        container.createLibrary(LIBRARY_NAME, LibrariesContainer.LibraryLevel.PROJECT, classes, sources);
                    }
                });
            }
        }, null, null);
    }

    private static VirtualFile[] getClasses(List<String> jars, Project project) {
        VirtualFile[] result = new VirtualFile[jars.size()];
        for (int i = 0; i < jars.size(); i++){
            VirtualFile file = VfsUtilCore.findRelativeFile(jars.get(i), project.getBaseDir());
            result[i] = StandardFileSystems.getJarRootForLocalFile(file);
        }
        return result;
    }

    private static VirtualFile[] getSourcesFromRepository(List<String> jars, Project project) {
        NavigableSet<String> sourceDirs = Collections.emptyNavigableSet();
        try {
            sourceDirs = TdpPluginPropertiesReader.getInstance(project.getBasePath()).getSourceDirs();
            sourceDirs.addAll(TdpPluginPropertiesReader.getInstance(project.getBasePath()).getGeneratedDirs());
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<VirtualFile> result = new ArrayList<>();
        for (String jar : jars) {
            String convertJar = jar.replace("\\", "/");
            String nameModule = getNameModule(convertJar);
            boolean hasSourceDir = false;
            if (nameModule != null){
                for (String sourceDir : sourceDirs) {
                    String pathToSource = project.getBasePath() + "/" + nameModule + sourceDir;
                    File dir = new File(pathToSource);
                    if (dir.exists()) {
                        hasSourceDir = true;
                        VirtualFile file = VfsUtilCore.findRelativeFile(pathToSource, project.getBaseDir());
                        result.add(file);
                    }
                }
            }
            if (!hasSourceDir) {
                VirtualFile file = VfsUtilCore.findRelativeFile(convertJar, project.getBaseDir());
                result.add(StandardFileSystems.getJarRootForLocalFile(file));
            }
        }
        return result.toArray(new VirtualFile[result.size()]);
    }

    private static String getNameModule(String pathToJar) {
        String[] parts = pathToJar.split("/");
        for (String part : parts) {
            if (part.contains("000")) {
                return part;
            }
        }
        return null;
    }
}
