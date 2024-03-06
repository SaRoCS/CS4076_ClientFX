package com.example.cs4076_clientfx;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;


/**
 * JavaFX App
 */
public class App extends Application {
    static final int PORT = 1234;
    static InetAddress host;
    static boolean connectionOpen = false;
    static Socket link = null;

    public static void main(String[] args) {
        launch();
    }

    private static JSONObject sendData(ObjectInputStream in, ObjectOutputStream out, String className, String classDate, LocalTime startTime, LocalTime endTime, String roomNumber, String userAction) {
        JSONObject obj = new JSONObject();
        JSONObject data = null;
        JSONObject res = null;

        if (!userAction.equals("Display Schedule")) {
            data = new JSONObject();
            data.put("name", className);
            data.put("dayOfWeek", classDate);
            data.put("startTime", startTime);
            data.put("endTime", endTime);
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

    @Override
    public void start(Stage stage) {
        TextField classNameTextField = new TextField();
        DatePicker classDatePicker = new DatePicker();
        TextField roomNumberTextField = new TextField();
        Button submitButton = new Button("Submit");
        Button stopButton = new Button("Stop");
        Label serverResponse = new Label("Server Response");
        ChoiceBox<String> actions = new ChoiceBox<String>();
        ChoiceBox<Integer> startHours = new ChoiceBox<Integer>();
        ChoiceBox<String> startMinutes = new ChoiceBox<String>();
        ChoiceBox<Integer> endHours = new ChoiceBox<Integer>();
        ChoiceBox<String> endMinutes = new ChoiceBox<String>();
        HBox startTimeBox = new HBox(5, new Label("Start Time:"), startHours, new Label(":"), startMinutes);
        HBox endTimeBox = new HBox(5, new Label("End Time:"), endHours, new Label(":"), endMinutes);
        HBox btnControls = new HBox(25, submitButton, stopButton);

        classNameTextField.setPromptText("Class Name");
        classDatePicker.setPromptText("Class Date");
        roomNumberTextField.setPromptText("Room Number");
        actions.getItems().addAll("Add Class", "Remove Class", "Display Schedule");
        serverResponse.setWrapText(true);
        classDatePicker.setEditable(false);
        btnControls.setAlignment(Pos.CENTER);

        startHours.getItems().addAll(9, 10, 11, 12, 13, 14, 15, 16, 17);
        startMinutes.getItems().addAll("00", "30");
        endHours.getItems().addAll(10, 11, 12, 13, 14, 15, 16, 17, 18);
        endMinutes.getItems().addAll("00", "30");

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

        gridPane.addRow(0, classNameTextField, btnControls);
        gridPane.addRow(1, classDatePicker, serverResponse);
        gridPane.addRow(2, startTimeBox);
        gridPane.addRow(3, endTimeBox);
        gridPane.addRow(4, roomNumberTextField);
        gridPane.addRow(5, actions);

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPrefWidth(200);
        gridPane.getColumnConstraints().addAll(col1, col2);

        GridPane.setRowSpan(serverResponse, 5);
        GridPane.setHalignment(serverResponse, HPos.CENTER);

        stage.setTitle("Class Scheduler");
        stage.setOnShown(event -> gridPane.requestFocus());

        submitButton.setOnAction(event -> {
            try {
                if (!connectionOpen) {
                    openConnection();
                }

                ObjectOutputStream out = new ObjectOutputStream(link.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(link.getInputStream());

                String userAction = actions.getValue();
                String className = null;
                String classDate = null;
                LocalTime startTime = null;
                LocalTime endTime = null;
                String roomNumber = null;

                if (!userAction.equals("Display Schedule")) {
                    className = classNameTextField.getText();
                    classDate = classDatePicker.getValue().getDayOfWeek().name();
                    startTime = LocalTime.of(startHours.getValue(), startHours.getValue());
                    endTime = LocalTime.of(endHours.getValue(), endHours.getValue());
                    roomNumber = roomNumberTextField.getText();
                }

                JSONObject res = sendData(in, out, className, classDate, startTime, endTime, roomNumber, userAction);

                if (res != null) {
                    serverResponse.setText(res.get("response").toString());
                } else {
                    serverResponse.setText("An error occurred while communicating with the server");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        actions.setOnAction(event -> {
            String value = actions.getValue();
            if (!value.equals("Display Schedule")) {
                BooleanBinding requiredFields = Bindings.isEmpty(classNameTextField.textProperty()).or(Bindings.isNull(classDatePicker.valueProperty())).or(Bindings.isNull(startHours.valueProperty())).or(Bindings.isNull(startMinutes.valueProperty())).or(Bindings.isNull(endHours.valueProperty())).or(Bindings.isNull(endMinutes.valueProperty())).or(Bindings.isEmpty(roomNumberTextField.textProperty()));
                submitButton.disableProperty().bind(requiredFields);
            }
        });

        stopButton.setOnAction(event -> closeConnection());

        Scene scene = new Scene(gridPane, 400, 210);
        stage.setScene(scene);
        stage.show();
    }

}