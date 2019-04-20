package com.proxy.kiwi.app;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.proxy.kiwi.tree.Tree;
import com.proxy.kiwi.tree.node.FolderNode;
import com.proxy.kiwi.tree.node.ImageNode;
import com.proxy.kiwi.tree.node.Node;
import com.proxy.kiwi.tree.node.NodeException;
import com.proxy.kiwi.ui.AbstractController;
import com.proxy.kiwi.ui.Explorer;
import com.proxy.kiwi.ui.Viewer;

import javafx.stage.Stage;

public class Parameter {
  private final Path initial;
  private final Node result;

  private Parameter(Node result, Path initial) {
    this.result = result;
    this.initial = initial;
  }

  public boolean exists() {
    return result != null;
  }

  public boolean hasInitial() {
    return initial != null;
  }

  public Path initial() {
    return this.initial;
  }

  public static Parameter empty() {
    return new Parameter(null,null);
  }

  public static Parameter imageNode(Path root) {
    return imageNode(root, null);
  }

  public static Parameter imageNode(Path root, Path initial) {
    try {
      ImageNode node = new ImageNode(null, root);
      return new Parameter(node, initial);
    } catch (NodeException e) {
      e.printStackTrace();
      return Parameter.empty();
    }
  }

  public static Parameter folderNode(Path root) {
    try {
      FolderNode node = new FolderNode(null, root);
      return new Parameter(node, null);
    } catch (NodeException e) {
      e.printStackTrace();
      return Parameter.empty();
    }
  }

  private static Parameter parse(Path path) {
    if (Node.isImage(path)) {
      return Parameter.imageNode(path.getParent(), path);
    }

    if (!Files.isDirectory(path)) {
      return Parameter.empty();
    }

    boolean isImageNode = Arrays.stream(path.toFile().listFiles())
      .map(File::toPath)
      .allMatch(Node::isImage);
    if (isImageNode) {
      return Parameter.imageNode(path);
    }
    return Parameter.folderNode(path);
  }

  public Node get() {
    return this.result;
  }

  public static Optional<AbstractController> controller(Stage stage, List<String> params, Configuration config) {

    List<String> rawPaths = new LinkedList<>();
    if (params.size() == 0) {
      rawPaths.addAll(config.paths);
    } else {
      rawPaths.addAll(params);
    }

    rawPaths.forEach(System.out::println);

    List<Parameter> parsed = rawPaths.stream()
      .map(Paths::get)
      .map(Parameter::parse)
      .filter(Parameter::exists)
      .collect(Collectors.toList());

    if (parsed.isEmpty()) {
      System.out.println("Unrecognized input files. Exiting.");
      return Optional.empty();
    }

    boolean onlyImageNodes = parsed.stream()
      .allMatch(p -> p.result instanceof ImageNode);

    if (onlyImageNodes) {
      List<ImageNode> iNodes = parsed.stream()
	.map(Parameter::get)
	.map(n -> (ImageNode)n)
	.collect(Collectors.toList());

      iNodes.forEach(n -> n.build());

      Optional<Path> initial = parsed.stream()
	.filter(Parameter::hasInitial)
	.map(Parameter::initial)
	.findFirst();

      if (!initial.isPresent()) {
	initial = iNodes.stream()
	  .map(in -> in.getImages().findFirst())
	  .filter(Optional::isPresent)
	  .map(Optional::get)
	  .findFirst();
      }

      Path first = initial.get();
      return Optional.of(new Viewer(iNodes, first, stage, config));

    } else {
      List<Node> nodes = parsed.stream()
	.map(Parameter::get)
	.collect(Collectors.toList());

      Tree tree = Tree.from(nodes);

//      Tree tree = new Tree(nodes);

      return Optional.of(new Explorer(tree, stage));
    }
  }

}
