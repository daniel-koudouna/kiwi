package com.proxy.kiwi.tree.node;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.proxy.kiwi.tree.Tree;
import com.proxy.kiwi.tree.TreeNode;
import com.proxy.kiwi.tree.event.ChildAdded;
import com.proxy.kiwi.tree.event.TreeEvent;

public class ImageNode extends Node{

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  List<URI> images;

  public static Optional<ImageNode> optional(Path path) {
    try {
      return Optional.of(new ImageNode(null, path));
    } catch (NodeException e) {
      System.err.println("Could not open file " + path);
      return Optional.empty();
    }
  }

  public ImageNode(TreeNode parent, Path path) throws NodeException {
    super(parent, path);
    images = new LinkedList<>();
  }

  @Override
  protected void buildInternal() {
    try {
      Files.list(getPath())
	.filter(FolderNode::isImage)
	.map(Path::toUri)
	.sorted(FileComparators.WINDOWS_LIKE)
	.forEach(images::add);
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (this.parent instanceof Tree) {
      emit(new ChildAdded(this,this,0));
    }
  }

  @Override
  public void accept(TreeEvent event) {

  }

  @Override
  public void prune() {

  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  public Stream<Path> getImages() {
    return images.stream()
      .map(Paths::get);
  }

  @Override
  public Stream<Node> getChildren() {
    return Stream.empty();
  }

  @Override
  public String toString() {
    return super.toString() + " " + images.size() + " images";
  }

  public Path before(Path path) {
    for (int i = 0; i < images.size(); i++) {
      if (Paths.get(images.get(i)).equals(path)) {
	return Paths.get(images.get(Math.max(0,i-1)));
      }
    }

    return path;
  }

  public Path after(Path path) {
    for (int i = 0; i < images.size(); i++) {
      if (Paths.get(images.get(i)).equals(path)) {
	return Paths.get(images.get(Math.min(images.size()-1,i+1)));
      }
    }

    return path;
  }
}
