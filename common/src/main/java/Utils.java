import javafx.scene.control.TreeItem;

import java.io.File;

abstract public class Utils {

    public static TreeItem<String> getNodesForDirectory(File directory) {
        TreeItem<String> root = new TreeItem<>(directory.getName());
        for(File f : directory.listFiles()) {
            if(f.isDirectory())
                root.getChildren().add(getNodesForDirectory(f));
            else
                root.getChildren().add(new TreeItem<>(f.getName()));
        }
        root.setExpanded(true);
        return root;
    }

    public static String getPathFromTree(TreeItem<?> treeItem) {
        StringBuilder sb = new StringBuilder();
        TreeItem<?> item = treeItem;
        while (item.getParent() != null) {
            sb.insert(0, item.getValue());
            sb.insert(0, "/");
            item = item.getParent();
        }
        return String.join("", sb);
    }

}
