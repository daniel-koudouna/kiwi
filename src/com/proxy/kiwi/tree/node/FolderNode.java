package com.proxy.kiwi.tree.node;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Stream;

import com.proxy.kiwi.tree.event.ChildAdded;
import com.proxy.kiwi.tree.event.TreeEvent;

public class FolderNode extends Node {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  public TreeSet<Node> children;

  public static Optional<FolderNode> optional(Path path) {
    try {
      return Optional.of(new FolderNode(null, path));
    } catch (NodeException e) {
      System.err.println("Error opening " + path);
      return Optional.empty();
    }
  }

  public FolderNode(Node parent, Path path) throws NodeException {
    super(parent,path);
    children = new TreeSet<>();
  }

  @Override
  protected void buildInternal() {
    try {
      if (Files.list(getPath()).anyMatch(Node::isImage)) {
	Node self = new ImageNode(this,getPath());
	children.add(self);
	emit(new ChildAdded(self,this,0));
      }
      Files.list(getPath()).sorted().forEach(this::handleDir);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (NodeException e) {
      e.printStackTrace();
    }
  }

  private void handleDir(Path path) {
    if (Files.isDirectory(path)) {
      try {
	boolean hasChildrenFolders = Files.list(path).anyMatch(Files::isDirectory);
	if (hasChildrenFolders) {
	  Node child = new FolderNode(this,path);
	  children.add(child);
	  emit(new ChildAdded(child,this,this.children.size()));
	}
	boolean hasChildrenImages = Files.list(path).anyMatch(FolderNode::isImage);
	if (hasChildrenImages) {
	  Node child = new ImageNode(this,path);
	  children.add(child);
	  emit(new ChildAdded(child,this,this.children.size()));
	}
      } catch (NodeException e) {
	e.printStackTrace();
      } catch (IOException e) {
	e.printStackTrace();
      }
    }
  }

  @Override
  public Stream<Node> stream() {
    return Stream.concat(Stream.of(this), this.children.stream().flatMap(Node::stream));
  }


  @Override
  public void accept(TreeEvent event) {
    children.forEach(c -> c.accept(event));
  }

    @Override
    public void prune() {
    	for (Iterator<Node> outeri = this.children.iterator(); outeri.hasNext(); ) {
    		Node child = outeri.next();
    		child.prune();
    	}
    	ArrayList<Node> marked = new ArrayList<>();
    	ArrayList<Node> markedAdd = new ArrayList<>();
        for (Iterator<Node> iterator = this.children.iterator(); iterator.hasNext(); ) {
            Node child = iterator.next();
            if (child instanceof FolderNode) {
                FolderNode folderNode = (FolderNode)child;
                if (folderNode.children.size() == 1) {
                    System.out.println(folderNode.getPath() + " has one child");
                    folderNode.children.forEach(c -> {
                    	markedAdd.add(c);
                    });
                    marked.add(child);
                }
            }
        }
        marked.forEach(n -> this.children.remove(n));
        markedAdd.forEach(n -> {
        	n.setName(n.parent.toString());
        	n.parent = this;
        	this.children.add(n);
        });
    }

//  @Override
//  public void prune() {
//    children.forEach(TreeNode::prune);
//    for (Iterator<Node> iterator = children.iterator(); iterator.hasNext(); ) {
//      Node child = iterator.next();
//      if (child instanceof FolderNode) {
//	FolderNode folderNode = (FolderNode)child;
//	if (folderNode.children.size() == 1) {
//	  //System.out.println(folderNode.path + " has one child");
//	}
//      }
//    }
//  }

  @Override
  public boolean isEmpty() {
    return children.isEmpty();
  }

  @Override
  public Stream<Node> getChildren() {
    return children.stream();
  }
}
