package com.proxy.kiwi.ui;

import java.util.function.Consumer;

import com.proxy.kiwi.tree.filter.NodeStatus;

import javafx.application.Platform;
import javafx.scene.layout.Pane;

public class TileComponent {
    public Tile tile;
    public Pane pane;

    public TileComponent(Tile tile, Consumer<TileComponent> callback) {
        this.tile = tile;

        Consumer<NodeStatus> creation = (status) -> {
            if (status.show() && pane == null) {
                Platform.runLater(() -> {
                    pane = tile.component();
                    callback.accept(this);
                });
            }
        };

        creation.accept(tile.node.status.get());
        tile.node.status.onChange(creation);
    }
}
