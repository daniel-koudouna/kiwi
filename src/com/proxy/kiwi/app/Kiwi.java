package com.proxy.kiwi.app;

import com.proxy.kiwi.tree.Tree;
import com.proxy.kiwi.tree.event.TreeBuilt;
import com.proxy.kiwi.tree.node.FolderNode;
import com.proxy.kiwi.tree.node.ImageNode;
import com.proxy.kiwi.tree.node.Node;
import com.proxy.kiwi.ui.Explorer;
import com.proxy.kiwi.ui.Viewer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.swing.text.View;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class Kiwi extends Application {
    public static URL resource(String path) {
        return Kiwi.class.getResource("../res/" + path);
    }

    @Override
    public void start(Stage stage) throws Exception{
        List<String> params = getParameters().getUnnamed();

        if (params.size() == 0) {
            long then = System.nanoTime();

            Path rootPath = Paths.get("/mnt/Storage/Cloud/Hens");
            FolderNode root = new FolderNode(null,rootPath);

            Tree tree = new Tree(root);

            //tree.onEvent((t) -> {
            //    if (t instanceof TreeBuilt) {
            //        ((TreeBuilt )t).tree.stream().forEach(n -> System.out.println(n.path));
            //    }
            //});

            Explorer controller = new Explorer(tree);
            stage.setTitle("Hello World");
            stage.setScene(new Scene(controller.component(),300, 275));
            stage.getScene().getStylesheets().add(Kiwi.resource("application.css").toString());
            stage.show();

            stage.setOnCloseRequest((e) -> {
                controller.exit();
            });

            long now = System.nanoTime();
            System.out.println("Startup Time " + ((now - then)/1_000_000_000.0) + " s");
        } else {
            long then = System.nanoTime();

            String str = params.get(0);
            Path path = Paths.get(str);

            if (!Node.isImage(path)) {
                System.exit(0);
            }

            ImageNode node = new ImageNode(null,path.getParent());
            node.build();
            LinkedList<ImageNode> list = new LinkedList<>();
            list.add(node);
            Viewer controller = new Viewer(list, path);
            stage.setTitle("Hello World");
            stage.setScene(new Scene(controller.component(),300, 275));
            stage.getScene().getStylesheets().add(Kiwi.resource("application.css").toString());
            stage.show();

            stage.setOnCloseRequest((e) -> {
                controller.exit();
            });

            long now = System.nanoTime();
            System.out.println("Startup Time " + ((now - then)/1_000_000_000.0) + " s");
        }

    }


    public static void main(String[] args) {
        launch(args);
    }
}
