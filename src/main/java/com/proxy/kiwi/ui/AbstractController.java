package com.proxy.kiwi.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

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
            try {
                URL url = this.getClass().getResource(path());
                System.out.println(url);
                FXMLLoader loader = new FXMLLoader(url);
                loader.setController(this);
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
