package com.tdp.workspace.generator;

import com.tdp.workspace.generator.utils.GeneratorModuleDep;
import com.tdp.workspace.generator.utils.GeneratorSourceContent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.impl.ModuleManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesContainer;
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesContainerFactory;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * Created by Siarhei Nahel on 21.05.2016.
 */
public class Controller {
    private static final String JAVA_MODULE = "JAVA_MODULE";
    private String path;
    private NavigableSet<String> baseModules;
    private NavigableSet<String> allDepend;

    public Controller(NavigableSet<String> baseModules, String path) {
        this.baseModules = baseModules;
        this.path = path;
    }

    public void generateWorkspace(Project project) throws IOException, TransformerException, ParserConfigurationException, SAXException {
        NavigableSet<String> nameLibraries = new TreeSet<>();
        Map<String, VirtFilesLibrary> libs = new HashMap<>();
        ModuleManager manager = ModuleManagerImpl.getInstance(project);
        LibraryTable libraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable(project);
        Library[] libraries = libraryTable.getLibraries();
        for (Library library : libraries) {
            nameLibraries.add(library.getName());
            CommandProcessor.getInstance().executeCommand(project, new Runnable() {
                public void run() {
                    ApplicationManager.getApplication().runWriteAction(new Runnable() {
                        public void run() {
                            libs.put(library.getName(), new VirtFilesLibrary(library.getFiles(OrderRootType.CLASSES),
                                    library.getFiles(OrderRootType.SOURCES)));
                            libraryTable.removeLibrary(library);
                        }
                    });
                }
            }, null, null);
        }

        GeneratorModuleDep generatorModuleDep = new GeneratorModuleDep(path, baseModules);
        allDepend = generatorModuleDep.getModuleNames();

        final List<Module> modules = Arrays.asList(manager.getModules());
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                for (Module module : modules){
                    String moduleName = module.getName();
                    if (!allDepend.contains(moduleName)){
                        manager.disposeModule(module);
                    }
                }
            }
        });

        for (String moduleName : allDepend) {
            String pathToModule = MessageFormat.format(Constants.PATTERN_PATH_TO_MODULE_FILE, path, moduleName);
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                public void run() {
                    Module module = manager.newModule(pathToModule, JAVA_MODULE);
                    ModuleRootModificationUtil.setSdkInherited(module);
                    String rootContent = path + Constants.SLASH + moduleName;
                    String output = rootContent + Constants.CLASSES_DIR;
                    ModuleRootModificationUtil.updateModel(module, new Consumer<ModifiableRootModel>() {
                        @Override
                        public void consume(ModifiableRootModel model) {
                            CompilerModuleExtension extension = model.getModuleExtension(CompilerModuleExtension.class);
                            ContentEntry rootContentEntry = model.addContentEntry(VfsUtilCore.pathToUrl(rootContent));
                            extension.inheritCompilerOutputPath(false);
                            extension.setCompilerOutputPath(VfsUtilCore.pathToUrl(output));
                            extension.setCompilerOutputPathForTests(VfsUtilCore.pathToUrl(output));
                            extension.setExcludeOutput(true);
                            try {
                                GeneratorSourceContent.generateRes(path, rootContentEntry, rootContent);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            List<String> existLibrary = new ArrayList<String>();
                            OrderEntry[] orderEntries = model.getOrderEntries();
                            for (OrderEntry entry : orderEntries) {
                                try {
                                    LibraryOrderEntry orderEntry = (LibraryOrderEntry) entry;
                                    existLibrary.add(orderEntry.getLibraryName());
                                } catch (ClassCastException e){
                                }
                            }
                            for (String libraryName : nameLibraries) {
                                if (!existLibrary.contains(libraryName)) {
                                    model.addInvalidLibrary(libraryName, "project");
                                }
                            }
                        }
                    });
                }
            });
        }

        LibrariesContainer container = LibrariesContainerFactory.createContainer(project);
        for (Map.Entry<String, VirtFilesLibrary> library : libs.entrySet()){
            CommandProcessor.getInstance().executeCommand(project, new Runnable() {
                public void run() {
                    ApplicationManager.getApplication().runWriteAction(new Runnable() {
                        public void run() {
                            container.createLibrary(library.getKey(),
                                    LibrariesContainer.LibraryLevel.PROJECT,
                                    library.getValue().classRoots,
                                    library.getValue().sourceRoots);
                        }
                    });
                }
            }, null, null);
        }
    }

    private class VirtFilesLibrary{
        VirtualFile[] classRoots;
        VirtualFile[] sourceRoots;

        public VirtFilesLibrary(VirtualFile[] classRoots, VirtualFile[] sourceRoots) {
            this.classRoots = classRoots;
            this.sourceRoots = sourceRoots;
        }
    }
}