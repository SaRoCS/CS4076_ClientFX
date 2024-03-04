package com.example.cs4076_clientfx;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.DayOfWeek;
import java.time.LocalDate;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.json.simple.JSONObject;


/**
 * JavaFX App
 */
public class App extends Application {
    static InetAddress host;
    static final int PORT = 1234;
    static boolean connectionOpen = false;
    static Socket link = null;
    TextField classNameTextField = new TextField();
    DatePicker classDatePicker = new DatePicker();
    TextField classTimeTextField = new TextField();
    TextField roomNumberTextField = new TextField();
    Button submitButton = new Button("Submit");
    Button stopButton = new Button("Stop");
    Label serverResponse = new Label("Server Response");
    ChoiceBox<String> actions = new ChoiceBox<String>();


    @Override
    public void start(Stage stage) {
        classNameTextField.setPromptText("Class Name");
        classDatePicker.setPromptText("Class Date");
        classTimeTextField.setPromptText("Class Time");
        roomNumberTextField.setPromptText("Room Number");
        actions.getItems().addAll("Add Class", "Remove Class", "Display Schedule");
        serverResponse.setWrapText(true);
        classDatePicker.setEditable(false);

        submitButton.disableProperty().bind(Bindings.isNull(actions.valueProperty()));

        classDatePicker.setDayCellFactory(new Callback<DatePicker, DateCell>() {
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item.getDayOfWeek() == DayOfWeek.SUNDAY || item.getDayOfWeek() == DayOfWeek.SATURDAY) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }
                    }
                };
            }
        });

        GridPane gridPane = new GridPane();
        gridPane.setHgap(8);
        gridPane.setVgap(8);
        gridPane.setPadding(new Insets(5));

        gridPane.addRow(0, classNameTextField, actions, submitButton, stopButton);
        gridPane.addRow(1, classDatePicker, serverResponse);
        gridPane.addRow(2, classTimeTextField);
        gridPane.addRow(3, roomNumberTextField);

        gridPane.setColumnSpan(serverResponse, 2);
        gridPane.setRowSpan(serverResponse, 3);
        gridPane.setHalignment(serverResponse, HPos.CENTER);

        stage.setTitle("Class Scheduler");
        stage.setOnShown(event -> gridPane.requestFocus());

        submitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                try {
                    if (!connectionOpen) {
                        openConnection();
                    }

                    ObjectOutputStream out = new ObjectOutputStream(link.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(link.getInputStream());

                    String className = classNameTextField.getText();
                    String classDate = classDatePicker.getValue().getDayOfWeek().name();
                    String classTime = classTimeTextField.getText();
                    String roomNumber = roomNumberTextField.getText();
                    String userAction = actions.getValue();

                    JSONObject res = sendData(in, out, className, classDate, classTime, roomNumber, userAction);

                    if (res != null) {
                        serverResponse.setText(res.get("response").toString());
                    } else {
                        serverResponse.setText("An error occurred while communicating with the server");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        actions.setOnAction(event -> {
            String value = actions.getValue();
            if (!value.equals("Display Schedule")) {
                BooleanBinding requiredFields = Bindings.isEmpty(classNameTextField.textProperty())
                        .or(Bindings.isNull(classDatePicker.valueProperty()))
                        .or(Bindings.isEmpty(classTimeTextField.textProperty()))
                        .or(Bindings.isEmpty(roomNumberTextField.textProperty()));
                submitButton.disableProperty().bind(requiredFields);
            }
        });

        stopButton.setOnAction(event -> closeConnection());

        Scene scene = new Scene(gridPane, 400, 200);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    private static JSONObject sendData(ObjectInputStream in, ObjectOutputStream out, String className, String classDate, String classTime, String roomNumber, String userAction) {
        JSONObject obj = new JSONObject();
        JSONObject data = null;
        JSONObject res = null;

        if (!userAction.equals("Display Schedule")) {
            data = new JSONObject();
            data.put("name", className);
            data.put("dayOfWeek", classDate);
            data.put("startTime", classTime);
            data.put("roomNumber", roomNumber);
        }

        obj.put("action", userAction);
        obj.put("data", data);

        try {
            out.writeObject(obj);
            res = (JSONObject) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return res;
    }

    private static void openConnection() {
        try {
            host = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println("Host ID not found!");
            System.exit(1);
        }

        try {
            link = new Socket(host, PORT);
            connectionOpen = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void closeConnection() {
        try {
            System.out.println("\n* Closing connection... *");
            link.close();
            connectionOpen = false;
        } catch (IOException e) {
            System.out.println("Unable to disconnect/close!");
            System.exit(1);
        }
    }

}