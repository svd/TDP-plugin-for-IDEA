package com.tdp.decorator;

import com.intellij.ide.DataManager;
import com.intellij.ide.PsiCopyPasteManager;
import com.intellij.ide.projectView.impl.ProjectViewPane;
import com.intellij.ide.projectView.impl.ProjectViewTree;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.ui.customization.CustomizationUtil;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.containers.Convertor;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * TDPProjectViewPane
 *
 * @author Pavel Semenov (<a href="mailto:Pavel_Semenov1@epam.com"/>)
 */
public class TDPProjectViewPane extends ProjectViewPane {
    @NonNls public static final String ID = "TDPProjectPane";
    @NonNls public static final String TITLE = "Project";

    private JScrollPane myComponent;

    public TDPProjectViewPane(Project project) {
        super(project);
    }

    @Override
    public JComponent createComponent() {
        // Whole this code is necessary magic :)
        if (myComponent != null) return myComponent;
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(null);
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        myTree = createTree(treeModel);
        enableDnD();
        myComponent = ScrollPaneFactory.createScrollPane(myTree);
        myTreeStructure = createStructure();
        setTreeBuilder(createBuilder(treeModel));

        installComparator();

        myTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        UIUtil.setLineStyleAngled(myTree);
        myTree.setRootVisible(false);
        myTree.setShowsRootHandles(true);
        myTree.expandPath(new TreePath(myTree.getModel().getRoot()));
        myTree.setSelectionPath(new TreePath(myTree.getModel().getRoot()));

        EditSourceOnDoubleClickHandler.install(myTree);

        ToolTipManager.sharedInstance().registerComponent(myTree);
        TreeUtil.installActions(myTree);

        myTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                fireTreeChangeListener();
            }
        });
        myTree.getModel().addTreeModelListener(new TreeModelListener() {
            @Override
            public void treeNodesChanged(TreeModelEvent e) {
                fireTreeChangeListener();
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {
                fireTreeChangeListener();
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
                fireTreeChangeListener();
            }

            @Override
            public void treeStructureChanged(TreeModelEvent e) {
                fireTreeChangeListener();
            }
        });

        new TDPSpeedSearch(myTree);

        myTree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (KeyEvent.VK_ENTER == e.getKeyCode()) {

                    final DefaultMutableTreeNode selectedNode = ((ProjectViewTree)myTree).getSelectedNode();
                    if (selectedNode != null && !selectedNode.isLeaf()) {
                        return;
                    }

                    DataContext dataContext = DataManager.getInstance().getDataContext(myTree);
                    OpenSourceUtil.openSourcesFrom(dataContext, false);
                }
                else if (KeyEvent.VK_ESCAPE == e.getKeyCode()) {
                    if (e.isConsumed()) return;
                    PsiCopyPasteManager copyPasteManager = PsiCopyPasteManager.getInstance();
                    boolean[] isCopied = new boolean[1];
                    if (copyPasteManager.getElements(isCopied) != null && !isCopied[0]) {
                        copyPasteManager.clear();
                        e.consume();
                    }
                }
            }
        });
        CustomizationUtil.installPopupHandler(myTree, IdeActions.GROUP_PROJECT_VIEW_POPUP, ActionPlaces.PROJECT_VIEW_POPUP);

        return myComponent;
    }

    @NotNull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public int getWeight() {
        return -1; // First position
    }

    protected static final class TDPSpeedSearch extends TreeSpeedSearch {
        private static final Convertor<TreePath, String> CONVERTER = new Convertor<TreePath, String>() {
            @Override
            public String convert(TreePath object) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) object.getLastPathComponent();
                if (node.getUserObject().getClass() != PsiDirectoryNode.class) {
                    return node.toString();
                }
                PsiDirectoryNode psiDirectoryNode = (PsiDirectoryNode) node.getUserObject();
                List<PresentableNodeDescriptor.ColoredFragment> fragments =
                        psiDirectoryNode.getPresentation().getColoredText();
                StringBuilder result = new StringBuilder();
                for (int i = 0; i < fragments.size() - 1 /* exclude module path */; i++) {
                    result.append(fragments.get(i).getText());
                }
                return result.toString();
            }
        };

        TDPSpeedSearch(JTree tree) {
            super(tree, CONVERTER);
        }
    }
}
