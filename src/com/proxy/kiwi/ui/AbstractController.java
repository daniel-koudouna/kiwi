package com.proxy.kiwi.ui;

import com.proxy.kiwi.app.Kiwi;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.ArrayList;

public abstract class AbstractController implements Initializable {
    private ArrayList<Runnable> exitCallbacks;

    public AbstractController() {
        this.exitCallbacks = new ArrayList<>();
    }

    protected abstract String path();

    public final Pane component() {
        FXMLLoader loader = new FXMLLoader(Kiwi.resource(path()));
        loader.setController(this);
        try {
            return loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void onExit(Runnable r) {
        exitCallbacks.add(r);
    }

    public void exit() {
        exitCallbacks.forEach(Runnable::run);
    };
}
