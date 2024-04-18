package com.example.cs4076_clientfx;
/*
 * JavaFX Imports
 */
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
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
    private static String[] sendData(ObjectInputStream in, ObjectOutputStream out, String userAction, String className, String classDate, LocalTime startTime, LocalTime endTime, String roomNumber) {
        JSONObject obj = new JSONObject();
        JSONObject data = null;
        String res = "An error occurred while communicating with the server";
        String earlyStatus = "";

        if (!userAction.equals("Display Schedule") && !userAction.equals("STOP") && !userAction.equals("Early Lectures")) {
            // Create a data object
            data = new JSONObject();
            data.put("name", className);
            data.put("dayOfWeek", classDate);
            data.put("startTime", startTime.toString());
            data.put("endTime", endTime.toString());
            data.put("roomNumber", roomNumber);
        }

        obj.put("action", userAction);
        obj.put("data", data);

        try {
            out.writeObject(obj);
            JSONObject resObj = (JSONObject) in.readObject();
            res = resObj.get("response").toString();
            earlyStatus = resObj.get("earlyLectureStatus").toString();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
        }

        return new String[]{res, earlyStatus};
    }

    /**
     * Opens a connection with the server
     *
     * @return true if the connection was opened
     */
    private static boolean openConnection() {
        try {
            host = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println("Host ID not found!");
            System.exit(1);
        }

        /*
            WARNING!!! This is just for demonstration purposes. You should store and retrieve the password in a secure way.
         */
        char[] password = "cs4076".toCharArray();
        return createSSLSocket("client_truststore.jks", password);
    }

    /**
     * Closes the connection with the server
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

    /**
     * Creates an SSL socket
     *
     * @param trustStoreFile The path to the client's truststore file
     * @param password       The truststore password
     * @return True, if the socket was created
     */
    private static boolean createSSLSocket(String trustStoreFile, char[] password) {
        SSLSocketFactory sslSocketFactory = null;

        try {
            // Load client's trusted keys into a trust manager
            KeyStore trustStore = KeyStore.getInstance("JKS");
            InputStream inputStream = new FileInputStream(trustStoreFile);
            trustStore.load(inputStream, password);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            // Create SSL factory with the trusted keys
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | KeyManagementException |
                 IOException e) {
            e.printStackTrace();
        }

        try {
            // Create SSL socket
            link = sslSocketFactory.createSocket(host, PORT);
            connectionOpen = true;
        } catch (NullPointerException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }

        return link != null;
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
        Label earlyLectureStatus = new Label("Early Lecture Status: Not Started");
        ChoiceBox<String> actions = new ChoiceBox<>();
        ChoiceBox<String> startHours = new ChoiceBox<>();
        ChoiceBox<String> endHours = new ChoiceBox<>();
        HBox startTimeBox = new HBox(5, new Label("Start Time:"), startHours);
        HBox endTimeBox = new HBox(5, new Label("End Time: "), endHours);
        HBox actionBox = new HBox(5, new Label("Action:"), actions);
        HBox btnControls = new HBox(25, submitButton, stopButton);
        //Schedule Labels
        Label Monday = new Label();
        Label Tuesday = new Label();
        Label Wednesday = new Label();
        Label Thursday = new Label();
        Label Friday = new Label();
        Label Title_9 = new Label();
        Label Title_10 = new Label();
        Label Title_11 = new Label();
        Label Title_12 = new Label();
        Label Title_13 = new Label();
        Label Title_14 = new Label();
        Label Title_15 = new Label();
        Label Title_16 = new Label();
        Label Title_17 = new Label();
        Label Title_18 = new Label();
        Label[][] Class_Array = new Label[5][10];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                Class_Array[i][j] = new Label();
            }
        }

        GridPane SchedulePane = new GridPane();
        SchedulePane.setPadding(new Insets(5));
        SchedulePane.addRow(1, new Label(), Monday, Tuesday, Wednesday, Thursday, Friday);
        SchedulePane.addRow(2, Title_9, Class_Array[0][0], Class_Array[1][0], Class_Array[2][0], Class_Array[3][0], Class_Array[4][0]);
        SchedulePane.addRow(3, Title_10, Class_Array[0][1], Class_Array[1][1], Class_Array[2][1], Class_Array[3][1], Class_Array[4][1]);
        SchedulePane.addRow(4, Title_11, Class_Array[0][2], Class_Array[1][2], Class_Array[2][2], Class_Array[3][2], Class_Array[4][2]);
        SchedulePane.addRow(5, Title_12, Class_Array[0][3], Class_Array[1][3], Class_Array[2][3], Class_Array[3][3], Class_Array[4][3]);
        SchedulePane.addRow(6, Title_13, Class_Array[0][4], Class_Array[1][4], Class_Array[2][4], Class_Array[3][4], Class_Array[4][4]);
        SchedulePane.addRow(7, Title_14, Class_Array[0][5], Class_Array[1][5], Class_Array[2][5], Class_Array[3][5], Class_Array[4][5]);
        SchedulePane.addRow(8, Title_15, Class_Array[0][6], Class_Array[1][6], Class_Array[2][6], Class_Array[3][6], Class_Array[4][6]);
        SchedulePane.addRow(9, Title_16, Class_Array[0][7], Class_Array[1][7], Class_Array[2][7], Class_Array[3][7], Class_Array[4][7]);
        SchedulePane.addRow(10, Title_17, Class_Array[0][8], Class_Array[1][8], Class_Array[2][8], Class_Array[3][8], Class_Array[4][8]);
        SchedulePane.addRow(11, Title_18, Class_Array[0][9], Class_Array[1][9], Class_Array[2][9], Class_Array[3][9], Class_Array[4][9]);

        ColumnConstraints Schedule_col1 = new ColumnConstraints();
        ColumnConstraints Schedule_col2 = new ColumnConstraints();
        ColumnConstraints Schedule_col3 = new ColumnConstraints();
        ColumnConstraints Schedule_col4 = new ColumnConstraints();
        ColumnConstraints Schedule_col5 = new ColumnConstraints();
        ColumnConstraints Schedule_col6 = new ColumnConstraints();
        Schedule_col1.setPrefWidth(100);
        Schedule_col2.setPrefWidth(100);
        Schedule_col3.setPrefWidth(100);
        Schedule_col4.setPrefWidth(100);
        Schedule_col5.setPrefWidth(100);
        Schedule_col6.setPrefWidth(100);
        SchedulePane.getColumnConstraints().addAll(Schedule_col1, Schedule_col2, Schedule_col3, Schedule_col4, Schedule_col5, Schedule_col6);


        // Set the necessary properties for the GUI elements
        classNameTextField.setPromptText("Class Name");
        classDatePicker.setPromptText("Class Date");
        roomNumberTextField.setPromptText("Room Number");

        actions.getItems().addAll("Add Class", "Remove Class", "Display Schedule", "Early Lectures");
        serverResponse.setWrapText(true);
        classDatePicker.setEditable(false);
        btnControls.setAlignment(Pos.CENTER);

        startHours.getItems().addAll("09:00", "10:00", "11:00", "12:00", "13:00", "14;00", "15:00", "16:00", "17:00");
        endHours.getItems().addAll("10:00", "11:00", "12:00", "13:00", "14;00", "15:00", "16:00", "17:00", "18:00");

        submitButton.disableProperty().bind(Bindings.isNull(actions.valueProperty()));

        // Disables selecting weekend days in the calendar
        classDatePicker.setDayCellFactory(new Callback<>() {
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
        gridPane.addRow(1, classDatePicker, earlyLectureStatus);
        gridPane.addRow(2, startTimeBox, serverResponse);
        gridPane.addRow(3, endTimeBox);
        gridPane.addRow(4, roomNumberTextField);
        gridPane.addRow(5, actionBox);
        gridPane.addRow(6, SchedulePane);

        // Set the column widths
        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPrefWidth(200);
        gridPane.getColumnConstraints().addAll(col1, col2);

        // Align the server response message
        GridPane.setRowSpan(serverResponse, 5);
        GridPane.setHalignment(serverResponse, HPos.CENTER);
        GridPane.setValignment(serverResponse, VPos.TOP);
        GridPane.setHalignment(earlyLectureStatus, HPos.CENTER);

        stage.setTitle("Class Scheduler");

        // Removes the focus from any GUI control
        stage.setOnShown(event -> gridPane.requestFocus());

        submitButton.setOnAction(event -> {
            try {
                // Connect to the server if needed
                if (!connectionOpen) {
                    // openConnection() was unsuccessful
                    if (!openConnection()) {
                        serverResponse.setText("Could not establish a connection with the server.");
                        return;
                    }
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
                if (!userAction.equals("Display Schedule") && !userAction.equals("Early Lectures")) {
                    className = classNameTextField.getText();
                    classDate = classDatePicker.getValue().getDayOfWeek().name();
                    startTime = LocalTime.parse(startHours.getValue());
                    endTime = LocalTime.parse(endHours.getValue());
                    roomNumber = roomNumberTextField.getText();
                }

                // Send the form data
                String[] res = sendData(in, out, userAction, className, classDate, startTime, endTime, roomNumber);

                if (userAction.equals("Display Schedule")) {
                    try {
                        JSONObject schedule = (JSONObject) new JSONParser().parse(res[0]);

                        //Setup first schedule
                        SchedulePane.setGridLinesVisible(true);
                        Monday.setText("MONDAY");
                        Tuesday.setText("TUESDAY");
                        Wednesday.setText("WEDNESDAY");
                        Thursday.setText("THURSDAY");
                        Friday.setText("FRIDAY");
                        Title_9.setText("09:00");
                        Title_10.setText("10:00");
                        Title_11.setText("11:00");
                        Title_12.setText("12:00");
                        Title_13.setText("13:00");
                        Title_14.setText("14:00");
                        Title_15.setText("15:00");
                        Title_16.setText("16:00");
                        Title_17.setText("17:00");
                        Title_18.setText("18:00");

                        //Clear Schedule
                        for (int i = 0; i < 5; i++) {
                            for (int j = 0; j < 10; j++) {
                                Class_Array[i][j].setText("");
                            }
                        }

                        //Get Info
                        JSONArray[] DaysInfo = new JSONArray[5];
                        DaysInfo[0] = (JSONArray) schedule.get("MONDAY");
                        DaysInfo[1] = (JSONArray) schedule.get("TUESDAY");
                        DaysInfo[2] = (JSONArray) schedule.get("WEDNESDAY");
                        DaysInfo[3] = (JSONArray) schedule.get("THURSDAY");
                        DaysInfo[4] = (JSONArray) schedule.get("FRIDAY");

                        //Updated Schedule
                        for (int day = 0; day < 5; day++) {
                            for (int i = 0; i < DaysInfo[day].size(); i++) {
                                JSONObject temp = (JSONObject) DaysInfo[day].get(i);
                                int startTimeInt;
                                if (temp.get("startTime").toString().equals("09:00")) {
                                    startTimeInt = 47;
                                } else {
                                    startTimeInt = temp.get("startTime").toString().charAt(1);
                                }
                                for (int j = 0; j < 9; j++) {
                                    if (startTimeInt == (j + 47)) {
                                        int endTimeInt = temp.get("endTime").toString().charAt(1);
                                        for(int k = j; k < ((endTimeInt-startTimeInt)+j); k++) {
                                            Class_Array[day][k].setText(temp.get("name").toString() + "\n" + temp.get("roomNumber").toString());
                                        }
                                    }
                                }
                            }
                        }
                        serverResponse.setText("Schedule Updated");
                        earlyLectureStatus.setText("Early Lecture Status: " + res[1]);
                    } catch (ParseException e) {
                        serverResponse.setText("Server sent an invalid response.");
                        earlyLectureStatus.setText("Early Lecture Status: " + res[1]);
                        e.printStackTrace();
                    }
                } else {
                    // Display the response
                    serverResponse.setText(res[0]);
                    earlyLectureStatus.setText("Early Lecture Status: " + res[1]);
                }

            } catch (SocketException e) {
                if (e.getMessage().equals("Connection reset by peer")) {
                    serverResponse.setText("Server disconnected.");
                } else {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        actions.setOnAction(event -> {
            String value = actions.getValue();
            // If the chosen action requires data, disable the submit button until all fields are filled
            if (!value.equals("Display Schedule") && !value.equals("Early Lectures")) {
                BooleanBinding requiredFields = Bindings.isEmpty(classNameTextField.textProperty()).or(Bindings.isNull(classDatePicker.valueProperty())).or(Bindings.isNull(startHours.valueProperty())).or(Bindings.isNull(endHours.valueProperty())).or(Bindings.isEmpty(roomNumberTextField.textProperty()));
                submitButton.disableProperty().bind(requiredFields);
            } else {
                // Enable the button if display is selected
                submitButton.disableProperty().unbind();
                submitButton.setDisable(false);
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

                String[] res = sendData(in, out, "STOP", null, null, null, null, null);
                serverResponse.setText(res[0]);
                earlyLectureStatus.setText("Early Lecture Status: " + res[1]);

                closeConnection();
            }
        });

        //Sets the scene
        Scene scene = new Scene(gridPane, 1000, 510);
        stage.setScene(scene);
        stage.show();
    }
}