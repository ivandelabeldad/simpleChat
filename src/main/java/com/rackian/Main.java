package com.rackian;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class Main extends Application {

    public static final String SERVER_IP = "130.211.88.185";
    // public static final String SERVER_IP = "192.168.1.100";
    // public static final String SERVER_IP = "192.168.1.254";

    public static final int PORT_LOGIN = 5000;
    public static final int PORT_REGISTER = 5001;
    public static final int PORT_ALIVE = 5002;
    public static final int PORT_SEND_MESSAGES = 5003;
    public static final int PORT_RECEIVE_MESSAGES = 5004;

    public static Executor pool;

    public static Stage stage;

    public static void main(String[] args) {

        pool = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            }
        });

        Main.launch(args);

    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        stage = primaryStage;

        FXMLLoader fxml;
        Pane rootPane;
        Scene scene;

        fxml = new FXMLLoader(getClass().getClassLoader().getResource("login.fxml"));
        rootPane = (Pane) fxml.load();
        scene = new Scene(rootPane);

        primaryStage.setTitle("Simple Chat - Sockets");
        primaryStage.setScene(scene);

        primaryStage.show();

        primaryStage.setMinWidth(primaryStage.getWidth());
        primaryStage.setMinHeight(primaryStage.getHeight());

    }

}
