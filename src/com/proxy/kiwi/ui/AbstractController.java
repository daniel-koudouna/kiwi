package com.proxy.kiwi.ui;

import java.io.IOException;
import java.util.ArrayList;

import com.proxy.kiwi.app.Kiwi;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;

public abstract class AbstractController implements Initializable {
    private ArrayList<Runnable> exitCallbacks;

    public Pane component;

    public AbstractController() {
        this.exitCallbacks = new ArrayList<>();
    }

    protected abstract String path();

    public final Pane component() {
        if (component == null) {
            FXMLLoader loader = new FXMLLoader(Kiwi.resource(path()));
            loader.setController(this);
            try {
                component = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return component;
    }

    public void onExit(Runnable r) {
        exitCallbacks.add(r);
    }

    public void exit() {
        exitCallbacks.forEach(Runnable::run);
    };
}
