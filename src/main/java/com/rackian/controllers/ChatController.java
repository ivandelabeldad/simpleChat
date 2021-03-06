package com.rackian.controllers;

import com.jfoenix.controls.JFXListView;
import com.rackian.Main;
import com.rackian.models.Message;
import com.rackian.models.MessageByTime;
import com.rackian.models.User;
import com.rackian.services.ReceiveMessageService;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class ChatController implements Initializable {

    private static User user;
    private static User userDest;
    private static List<User> contacts;
    private static List<Message> messages;

    @FXML
    private Label lNick;
    @FXML
    private TextField tfMessage;
    @FXML
    private Pane messagePane;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private JFXListView<StackPane> usersListView;

    public static User getUser() {
        return user;
    }

    public static void setUser(User user) {
        ChatController.user = user;
    }

    public static User getUserDest() {
        return userDest;
    }

    public static void setUserDest(User userDest) {
        ChatController.userDest = userDest;
    }

    public static List<Message> getMessages() {
        return messages;
    }

    public static void setMessages(List<Message> messages) {
        ChatController.messages = messages;
    }

    public static List<User> getContacts() {
        return contacts;
    }

    public static void setContacts(List<User> contacts) {
        for (int i = 0; i < contacts.size(); i++) {
            //System.out.println(contacts.get(i).getEmail() + ": " + contacts.get(i).isOnline());
            if (contacts.get(i).compareTo(user) == 0) {
                contacts.remove(i);
                i--;
            }
        }
        ChatController.contacts = contacts;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        lNick.setText(user.getNick());
        tfMessage.setDisable(true);

        // INICIO EL SERVICIO DE ESCUCHA
        Thread thread = new Thread(new ReceiveMessageService(messagePane));
        thread.setDaemon(true);
        thread.start();

        // HAGO UN BINDEO DEL SCROLL
        scrollBind();

        // CARGO LOS CONTACTOS
        loadContacts();

        updateOnlineContacts();

    }

    @FXML
    private void handleSend() throws Exception {

        // SI ESTA VACIO NO ENVIO
        if (tfMessage.getText().replaceAll(" ", "").equals("")) {
            return;
        }

        Message message = newMessage();

        // CREO MENSAJE EN EL PANEL
        createMessagePanel(message, MessageController.MESSAGE_SENT);

        // ENVIO EL MENSAJE MEDIANTE SOCKETS
        sendMessage(message);

        // LO AÑADO A LA LISTA
        messages.add(message);

        // RESETEO EL CAMPO
        tfMessage.setText("");

    }

    @FXML
    private void handleSendKey(KeyEvent keyEvent) throws Exception {

        if (keyEvent.getCode().equals(KeyCode.ENTER))
            handleSend();

    }

    public void createMessagePanel(Message message, int direction) throws IOException {
        // CREO EL MENSAJE ENVIADO EN EL PANEL
        MessageController mc;
        mc = new MessageController();
        mc.setParent(messagePane);
        mc.setMessage(message);
        mc.setDirection(direction);
        mc.createMessage();
    }

    private void sendMessage(Message message) throws Exception {

        Socket socket;
        OutputStream os;
        ObjectOutputStream oos;

        socket = new Socket(Main.SERVER_IP, Main.PORT_SEND_MESSAGES);
        os = socket.getOutputStream();
        oos = new ObjectOutputStream(os);
        oos.writeObject(message);
        oos.close();
        os.close();
        socket.close();

    }

    private Message newMessage() {
        Message message;
        message = new Message();
        message.setUserOri(user);
        message.setUserDest(userDest);
        message.setMessage(tfMessage.getText());
        message.setTime(LocalDateTime.now());
        return message;
    }

    private void loadMessages(User userDest) {

        // BORRO TODOS LOS MENSAJES
        while(messagePane.getChildren().size() > 0) {
            messagePane.getChildren().remove(0);
        }

        for (Message message : this.messages) {
            System.out.println("Origen: " + message.getUserOri().getEmail());
            System.out.println("Destino: " + message.getUserDest().getEmail());
            System.out.println("Mensaje: " + message.getMessage());
            System.out.println();
        }

        // GUARDO TODOS LOS MENSAJES DEL USUARIO SEÑALADO
        List<Message> messages;
        messages = new ArrayList<>();

        for (Message message : this.messages) {
            if (message.getUserOri().compareTo(userDest) == 0 ||
                    message.getUserDest().compareTo(userDest) == 0) {
                messages.add(message);
            }
        }

        // LOS ORDENO POR FECHA
        Collections.sort(messages, new MessageByTime());

        // LOS MUESTRO EN EL PANEL
        messages.forEach(m -> {
            try {
                if (m.getUserOri().compareTo(user) == 0)
                    createMessagePanel(m, MessageController.MESSAGE_SENT);
                else
                    createMessagePanel(m, MessageController.MESSAGE_RECEIVED);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

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

    private void loadContacts() {
        // MUESTRO LOS CONTACTOS Y ASIGNO EVENTO
        for (User contact : contacts) {
            StackPane stackPane;
            Label label;
            Circle circle;

            // CREO EL CIRCULO
            circle = new Circle(7, Paint.valueOf("#4CAF50"));
            circle.setId(contact.getEmail() + "_circle");

            // CREO LA ETIQUETA
            label = new Label(contact.getNick());
            label.setId(contact.getEmail() + "_label");

            // CREO EL PANEL
            stackPane = new StackPane(label, circle);
            stackPane.setId(contact.getEmail() + "_stackPane");
            stackPane.setPadding(new Insets(10,10,10,10));
            StackPane.setAlignment(label, Pos.CENTER_LEFT);
            StackPane.setAlignment(circle, Pos.CENTER_RIGHT);

            stackPane.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    if (tfMessage.isDisable()) {
                        tfMessage.setDisable(false);
                    }
                    StackPane pulsed = (StackPane) event.getTarget();
                    userDest = contact;
                    loadMessages(userDest);
                }
            });
            usersListView.getItems().add(stackPane);
        }
    }

    public void updateOnlineContacts() {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                List<StackPane> stackPanes;
                stackPanes = usersListView.getItems();
                //System.out.println("Modificando");
                for (StackPane stackPane : stackPanes) {
                    for (User contact : contacts) {
                        Label label;
                        Circle circle;
                        label = (Label) stackPane.getChildren().get(0);
                        circle = (Circle) stackPane.getChildren().get(1);
                        if (stackPane.getId().equals(contact.getEmail() + "_stackPane")) {
                            if (contact.isOnline()) {
                                label.setDisable(false);
                                circle.setFill(Paint.valueOf("#4CAF50"));
                                circle.setStroke(Paint.valueOf("#4CAF50"));
                            } else {
                                label.setDisable(true);
                                circle.setFill(Paint.valueOf("#CFD8DC"));
                                circle.setStroke(Paint.valueOf("#CFD8DC"));
                            }
                        }
                    }
                }
            }
        });

    }

}
