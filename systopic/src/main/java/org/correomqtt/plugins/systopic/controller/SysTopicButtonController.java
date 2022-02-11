package org.correomqtt.plugins.systopic.controller;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import org.correomqtt.business.dispatcher.ConnectionLifecycleDispatcher;
import org.correomqtt.business.dispatcher.ConnectionLifecycleObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("ALL")
public class SysTopicButtonController  implements ConnectionLifecycleObserver {

    private Logger LOGGER = LoggerFactory.getLogger(SysTopicButtonController.class);

    @FXML
    private Button SYSbutton;

    private String connectionId;

    public SysTopicButtonController(String connectionId) {
        this.connectionId= connectionId;
        ConnectionLifecycleDispatcher.getInstance().addObserver(this);
    }

    public void addItems(HBox toolbar, int indexToinsert) {
        HBox.setMargin(SYSbutton, new Insets(0, 0, 0, 5));
        toolbar.getChildren().add(indexToinsert,SYSbutton);
    }

    @FXML
     void OnClicked() throws IOException {
        LOGGER.info("$SYS Button clicked with connectionId:" + connectionId);
        SysTopicViewController.showAsDialog(connectionId);
    }

    @Override
    public void onDisconnectFromConnectionDeleted(String connectionId) {
        // do nothing
    }

    @Override
    public void onConnect() {
        Platform.runLater(() -> {
            SYSbutton.setDisable(false);
        });
    }

    @Override
    public void onConnectRunning() {
        Platform.runLater(() -> {
            SYSbutton.setDisable(true);
        });
    }

    @Override
    public void onConnectionFailed(Throwable e) {
        Platform.runLater(() -> {
            SYSbutton.setDisable(true);
        });
    }

    @Override
    public void onConnectionCanceled() {
        Platform.runLater(() -> {
            SYSbutton.setDisable(true);
        });
    }

    @Override
    public void onConnectionLost() {
        Platform.runLater(() -> {
            SYSbutton.setDisable(true);
        });
    }

    @Override
    public void onDisconnect() {
        Platform.runLater(() -> {
            SYSbutton.setDisable(true);
        });
    }

    @Override
    public void onDisconnectFailed(Throwable exception) {
        Platform.runLater(() -> {
            SYSbutton.setDisable(false);
        });
    }

    @Override
    public void onDisconnectRunning() {
        Platform.runLater(() -> {
            SYSbutton.setDisable(true);
        });
    }

    @Override
    public void onConnectionReconnected() {
        Platform.runLater(() -> {
            SYSbutton.setDisable(false);
        });
    }

    @Override
    public void onReconnectFailed(AtomicInteger triedReconnects, int maxReconnects) {
        Platform.runLater(() -> {
            SYSbutton.setDisable(true);
        });
    }

    @Override
    public String getConnectionId() {
        return connectionId;
    }
}




