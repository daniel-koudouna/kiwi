package com.proxy.kiwi.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import com.proxy.kiwi.instancer.LaunchParameters;
import com.proxy.kiwi.tree.Tree;
import com.proxy.kiwi.tree.node.FolderNode;
import com.proxy.kiwi.tree.node.ImageNode;
import com.proxy.kiwi.tree.node.Node;
import com.proxy.kiwi.ui.Explorer;
import com.proxy.kiwi.ui.Viewer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Kiwi extends Application {
    public static URL resource(String path) {
        return Kiwi.class.getResource("../res/" + path);
    }

    @Override
    public void start(Stage stage) throws Exception{
        long then = System.nanoTime();
    	List<String> params = getParameters().getUnnamed();

        if (params.size() == 0) {
            Path rootPath = Paths.get("S:", "Cloud","Hens");
//            Path rootPath = Paths.get("mnt","Storage","Cloud","Hens");
            FolderNode root = new FolderNode(null,rootPath);

            Tree tree = new Tree(root);

            Explorer controller = new Explorer(tree);

            stage.setScene(new Scene(controller.component(),700, 700));
            stage.setOnCloseRequest((e) -> {
                controller.exit();
            });

            long now = System.nanoTime();
            System.out.println("Startup Time " + ((now - then)/1_000_000_000.0) + " s");
        } else {

            String str = params.get(0);
            Path path = Paths.get(str);

            if (Node.isImage(path)) {
            	ImageNode node = new ImageNode(null,path.getParent());
                node.build();
                LinkedList<ImageNode> list = new LinkedList<>();
                list.add(node);
                Viewer controller = new Viewer(list, path);

                stage.setScene(new Scene(controller.component(),700, 700));
                stage.setOnCloseRequest((e) -> {
                    controller.exit();
                });

                long now = System.nanoTime();
                System.out.println("Startup Time " + ((now - then)/1_000_000_000.0) + " s");
            } else if (path.toString().endsWith(".tmp")) {

            	System.out.println("READING TEMP FILE");

            	File infile = path.toFile();
                FileInputStream fis = new FileInputStream(infile);
                ObjectInputStream ois = new ObjectInputStream(fis);

                LaunchParameters parameters = null;
                try {
                    parameters = (LaunchParameters) ois.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                if (parameters == null) {
                	System.out.println("Could not read parameters. Exiting.");
                	System.exit(0);
                }

                Path initialPath = Paths.get(parameters.initial);
                ImageNode node = new ImageNode(null,initialPath.getParent());

                parameters.nodelist.forEach(in -> in.build());

                Viewer controller = new Viewer(parameters.nodelist, initialPath);
                stage.setScene(new Scene(controller.component(),700, 700));
                stage.setOnCloseRequest((e) -> {
                    controller.exit();
                });

            } else {
            	System.out.println("Unrecognized input file. Exiting.");
            	System.out.println(path.toString());
            	System.exit(0);
            }
        }

        stage.setTitle("Kiwi");
        stage.getScene().getStylesheets().add(Kiwi.resource("application.css").toString());
        stage.show();

        long now = System.nanoTime();
        System.out.println("Startup Time " + ((now - then)/1_000_000_000.0) + " s");
    }


    public static void main(String[] args) {
        launch(args);
    }
}
