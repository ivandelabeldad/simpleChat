package com.rackian.controllers;

import com.rackian.Main;
import com.rackian.models.Message;
import com.rackian.services.MessageService;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class ChatController implements Initializable {

    private String nick;
    @FXML
    private Label lNick;
    @FXML
    private TextField tfMessage;
    @FXML
    private Pane messagePane;
    @FXML
    private ScrollPane scrollPane;

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        lNick.setText(nick);

        // INICIO EL SERVICIO DE ESCUCHA
        Thread thread = new Thread(new MessageService(messagePane));
        thread.setDaemon(true);
        thread.start();

        // HAGO UN BINDEO DEL SCROLL
        scrollBind();

    }

    @FXML
    private void handleSend() throws Exception {

        // SI ESTA VACIO NO ENVIO
        if (tfMessage.getText().replaceAll(" ", "").equals("")) {
            return;
        }

        // CREO EL MENSAJE ENVIADO EN EL PANEL
        MessageController mc;
        mc = new MessageController();
        mc.setParent(messagePane);
        mc.setMessage(getMessage());
        mc.setDirection(MessageController.MESSAGE_SENT);
        mc.createMessage();

        // ENVIO EL MENSAJE MEDIANTE SOCKETS
        sendMessage();

        // RESETEO EL CAMPO
        tfMessage.setText("");

    }

    @FXML
    private void handleSendKey(KeyEvent keyEvent) throws Exception {

        if (keyEvent.getCode().equals(KeyCode.ENTER))
            handleSend();

    }

    private void sendMessage() throws Exception {

        Socket socket;
        OutputStream os;
        ObjectOutputStream oos;

        socket = new Socket(Main.SERVER_IP, 1122);
        os = socket.getOutputStream();
        oos = new ObjectOutputStream(os);
        oos.writeObject(getMessage());
        oos.close();
        os.close();
        socket.close();

    }

    private Message getMessage() {
        Message message;
        message = new Message();
        message.setNick(nick);
        message.setMessage(tfMessage.getText());
        message.setTime(LocalDateTime.now());
        return message;
    }

    private void scrollBind() {

        ChangeListener<Number> changeListener;

        changeListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                scrollPane.setVvalue(scrollPane.getVmax());
            }
        };

        messagePane.heightProperty().addListener(changeListener);

    }

}
