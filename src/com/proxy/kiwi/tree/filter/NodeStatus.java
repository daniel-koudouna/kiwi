package com.proxy.kiwi.tree.filter;

import java.io.Serializable;

public class NodeStatus  implements Serializable{
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private final boolean showSelf, showChildren;

  public NodeStatus(boolean self, boolean children) {
    super();
    this.showSelf = self;
    this.showChildren = children;
  }

  public boolean show() {
    return showSelf || showChildren;
  }

  public boolean self() {
    return showSelf;
  }

  public boolean children() {
    return showChildren;
  }
}
