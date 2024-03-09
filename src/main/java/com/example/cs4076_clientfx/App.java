package com.example.cs4076_clientfx;
/**
 * JSON package
 */
import org.json.simple.JSONObject;
/**
 * JavaFX packages
 */
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
/**
 * Other Java packages
 */
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
 *
 * @author Westin Gjervold
 * @author Samuel Schroeder
 */
public class App extends Application {
    /**
     * The port to communicate on
     */
    static final int PORT = 1234;
    /**
     * The client's address
     */
    static InetAddress host;
    /**
     * The current state of the connection with the server
     */
    static boolean connectionOpen = false;
    /**
     * The communication socket
     */
    static Socket link = null;

    public static void main(String[] args) {
        launch();
    }

    /**
     * Sends the given data in JSON format and receives the server's response
     *
     * @param in         The socket's input
     * @param out        The socket's output
     * @param userAction The desired action to take
     * @param className  The name of the class to perform the action on
     * @param classDate  The date of the class to perform the action on
     * @param startTime  The start time of the class to perform the action on
     * @param endTime    The end time of the class to perform the action on
     * @param roomNumber The room number of the class to perform the action on
     * @return The server's response
     */
    private static JSONObject sendData(ObjectInputStream in, ObjectOutputStream out, String userAction, String className, String classDate, LocalTime startTime, LocalTime endTime, String roomNumber) {
        JSONObject obj = new JSONObject();
        JSONObject data = null;
        JSONObject res = null;

        if (!userAction.equals("Display Schedule") && !userAction.equals("STOP")) {
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

    /**
     * Opens a connection with the server
     */
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

    /**
     * CLoses the connection with the server
     */
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
        // Create the GUI elements
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
        HBox endTimeBox = new HBox(5, new Label("End Time: "), endHours, new Label(":"), endMinutes);
        HBox actionBox = new HBox(5, new Label("Action:"), actions);
        HBox btnControls = new HBox(25, submitButton, stopButton);

        // Set the necessary properties for the GUI elements
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

        // Disables selecting weekend days in the calendar
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

        // Format the pane
        GridPane gridPane = new GridPane();
        gridPane.setHgap(8);
        gridPane.setVgap(8);
        gridPane.setPadding(new Insets(5));

        // Add the GUI elements to the pane
        gridPane.addRow(0, classNameTextField, btnControls);
        gridPane.addRow(1, classDatePicker, serverResponse);
        gridPane.addRow(2, startTimeBox);
        gridPane.addRow(3, endTimeBox);
        gridPane.addRow(4, roomNumberTextField);
        gridPane.addRow(5, actionBox);

        // Set the column widths
        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPrefWidth(200);
        gridPane.getColumnConstraints().addAll(col1, col2);

        // Align the server response message
        GridPane.setRowSpan(serverResponse, 5);
        GridPane.setHalignment(serverResponse, HPos.CENTER);

        stage.setTitle("Class Scheduler");

        // Removes the focus from any GUI control
        stage.setOnShown(event -> gridPane.requestFocus());

        submitButton.setOnAction(event -> {
            try {
                // Connect to the server if needed
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

                // Collect the form data
                if (!userAction.equals("Display Schedule")) {
                    className = classNameTextField.getText();
                    classDate = classDatePicker.getValue().getDayOfWeek().name();
                    startTime = LocalTime.of(startHours.getValue(), Integer.parseInt(startMinutes.getValue()));
                    endTime = LocalTime.of(endHours.getValue(), Integer.parseInt(endMinutes.getValue()));
                    roomNumber = roomNumberTextField.getText();
                }

                // Send the form data
                JSONObject res = sendData(in, out, userAction, className, classDate, startTime, endTime, roomNumber);

                // Display the response
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
            // If the chosen action requires data, disable the submit button until all fields are filled
            if (!value.equals("Display Schedule")) {
                BooleanBinding requiredFields = Bindings.isEmpty(classNameTextField.textProperty()).or(Bindings.isNull(classDatePicker.valueProperty())).or(Bindings.isNull(startHours.valueProperty())).or(Bindings.isNull(startMinutes.valueProperty())).or(Bindings.isNull(endHours.valueProperty())).or(Bindings.isNull(endMinutes.valueProperty())).or(Bindings.isEmpty(roomNumberTextField.textProperty()));
                submitButton.disableProperty().bind(requiredFields);
            }
        });

        stopButton.setOnAction(event -> {
            if (connectionOpen) {
                // Send a stop message to the server
                ObjectOutputStream out = null;
                ObjectInputStream in = null;
                try {
                    out = new ObjectOutputStream(link.getOutputStream());
                    in = new ObjectInputStream(link.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                JSONObject res = sendData(in, out, "STOP", null, null, null, null, null);

                if (res != null) {
                    serverResponse.setText(res.get("response").toString());
                } else {
                    serverResponse.setText("An error occurred while communicating with the server");
                }

                closeConnection();
            }
        });
        //Sets the scene
        Scene scene = new Scene(gridPane, 400, 210);
        stage.setScene(scene);
        stage.show();
    }
}