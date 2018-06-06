package com.proxy.kiwi.ui;

import com.proxy.kiwi.tree.filter.NodeStatus;
import javafx.application.Platform;
import javafx.scene.layout.Pane;

import java.util.function.Consumer;

public class TileComponent {
    public Tile tile;
    public Pane pane;

    public TileComponent(Tile tile, Consumer<TileComponent> callback) {
        this.tile = tile;

        Consumer<NodeStatus> creation = (status) -> {
            if (status == NodeStatus.SHOW_SELF && pane == null) {
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
