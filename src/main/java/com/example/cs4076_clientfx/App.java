package com.example.cs4076_clientfx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;


/**
 * JavaFX App
 */
public class App extends Application {
    static InetAddress host;
    static final int PORT = 1234;
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
                try
                {
                    host = InetAddress.getLocalHost();
                }
                catch(UnknownHostException e)
                {
                    System.out.println("Host ID not found!");
                    System.exit(1);
                }
                Socket link = null;
                try
                {
                    link = new Socket(host,PORT);
                    //link = new Socket( "192.168.0.59", PORT);
                    BufferedReader in = new BufferedReader(new InputStreamReader(link.getInputStream()));
                    PrintWriter out = new PrintWriter(link.getOutputStream(),true);

                    String className = null;
                    String classDate = null;
                    String classTime = null;
                    String roomNumber = null;
                    String userAction = null;
                    String response= null;

                    className =  classNameTextField.getText().toString();
                    classDate = classDatePicker.getValue().getDayOfWeek().name();
                    classTime =  classTimeTextField.getText().toString();
                    roomNumber =  roomNumberTextField.getText().toString();
                    userAction = actions.getValue().toString();
                    out.println(className);
                    response = in.readLine();
                    serverResponse.setText(response);
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    try
                    {
                        System.out.println("\n* Closing connection... *");
                        link.close();				//Step 4.
                    }catch(IOException e)
                    {
                        System.out.println("Unable to disconnect/close!");
                        System.exit(1);
                    }
                }
            }});

        Scene scene = new Scene(gridPane, 400, 200);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}