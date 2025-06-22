import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Controller {

    @FXML
    AnchorPane mainPane;
    @FXML
    ScrollPane scrollPane;
    @FXML
    TextField taskInput;
    @FXML
    VBox tasksContainer;
    @FXML
    Button addButton;

    List<String> tasks = new ArrayList<>();

    Image deleteImage = new Image(getClass().getResource("deleteIcon.png").toExternalForm());

    // used to change colours of images on mouse hover
    ColorAdjust colorAdjust = new ColorAdjust();

    public void initialize() {
        // sets up scroll pane
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setFitToWidth(true);

        tasksContainer.setSpacing(10);
        loadFromFile(); // puts tasks form file into array
        loadFromArray(); // displays tasks in array on screen
        System.out.println(tasks);

        colorAdjust.setBrightness(-0.3);

        //scaleAnimation(addButton);
        changeColour(addButton);
    }

    public void scaleAnimation(Button button) {

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(80), button);

        button.setOnMouseEntered(e -> {
            scaleTransition.setToX(1.15);
            scaleTransition.setToY(1.15);

            scaleTransition.play();
        });

        button.setOnMouseExited(e -> {
            scaleTransition.setToX(1);
            scaleTransition.setToY(1);

            scaleTransition.play();
        });
    }

    public void changeColour(Button button) {

        button.setOnMouseEntered(e -> {
          button.setEffect(colorAdjust);
        });

        button.setOnMouseExited(e -> {
         button.setEffect(null);
        });
    }

    public void addTask(ActionEvent event) {

        String inputText = taskInput.getText().trim();

        if (!(inputText.isEmpty())) {

            writeToFile("I: "+inputText); // sets to incomplete at first
            tasks.add("I: "+inputText); // sets to incomplete at first
            System.out.println(tasks);
            AtomicInteger taskIndex = new AtomicInteger();

            // setting up Hbox to contain and elements
            CheckBox completeBox = createCompleteBox();

            Button deleteButton = createDeleteButton();

            // task label
            Text task = new Text(inputText);
            task.setStyle("-fx-padding: 8px; -fx-font-size: 20px; -fx-font-family: Chalkboard;");
            System.out.println(task.getText());

            setCompleteBox(completeBox, taskIndex, inputText, task);

            // adding all to container
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS); // creates gap for delete button to be on right side
            HBox taskBox = new HBox();
            taskBox.setPrefHeight(10);

            setDeleteButton(deleteButton, taskIndex, inputText, taskBox);

            taskBox.getChildren().addAll(completeBox, task, spacer, deleteButton);

            tasksContainer.getChildren().addAll(taskBox);

            taskInput.clear();
            taskInput.requestFocus(); // sends cursor to input field
            System.out.println("cleared");
        }
    }

    private Button createDeleteButton() {
        Button deleteButton = new Button();
        ImageView deleteButtonIcon = new ImageView(deleteImage);

        deleteButton.setGraphic(deleteButtonIcon);

        deleteButtonIcon.setFitHeight(35);
        deleteButtonIcon.setFitWidth(35);
        deleteButtonIcon.setEffect(null);

        deleteButton.setOnMouseEntered(e -> deleteButtonIcon.setEffect(colorAdjust));
        deleteButton.setOnMouseExited(e -> deleteButtonIcon.setEffect(null));

        //scaleAnimation(deleteButton);
        changeColour(deleteButton);

        HBox.setMargin(deleteButton, new Insets(5, 5, 0, 0));
        return deleteButton;
    }

    // sets up delete button function
    private void setDeleteButton(Button deleteButton, AtomicInteger taskIndex, String inputText, HBox taskBox) {
        // setting up delete button
        deleteButton.setOnAction(e -> {

            taskIndex.set(tasks.indexOf("I: " + inputText)); // gets index of task in tasks array

            // checks if completion is correct, if not then searches for other completion status
            if (taskIndex.get() != -1) {

                System.out.println("removed: "+ inputText);
                tasks.remove(tasks.get(taskIndex.get()));
                updateFile(inputText);
                tasksContainer.getChildren().remove(taskBox);
            }

            else {
                taskIndex.set(tasks.indexOf("C: "+ inputText));

                System.out.println("removed: "+ inputText);
                tasks.remove(tasks.get(taskIndex.get()));
                updateFile(inputText);
                tasksContainer.getChildren().remove(taskBox);
            }
        });
    }

    private static CheckBox createCompleteBox() {

        CheckBox completeBox = new CheckBox();
        completeBox.setPadding(new Insets(5, 5, 0, 5));

        return completeBox;
    }

    // set up complete button function
    private void setCompleteBox(CheckBox completeBox, AtomicInteger taskIndex, String inputText, Text task) {
        // setting up completeButton
        completeBox.setOnAction(e -> {

            if (completeBox.isSelected()) {
                taskIndex.set(tasks.indexOf("I: " + inputText)); // gets index of task in tasks array

                tasks.set(taskIndex.get(), "C: "+ inputText); // sets task in array to 'completed'
                task.setStyle("-fx-padding: 8px; -fx-font-size: 20px; -fx-strikethrough: true; -fx-font-family: Chalkboard;");
                updateFile(inputText); // updates completion status
            }
            else {
                taskIndex.set(tasks.indexOf("C: " + inputText)); // gets index of task in tasks array

                tasks.set(taskIndex.get(), "I: "+ inputText); // sets task in array to 'incomplete'
                task.setStyle("-fx-padding: 8px; -fx-font-size: 20px; -fx-font-family: Chalkboard;");
                updateFile(inputText);
            }
        });
    }

    public void loadFromFile() {

        try {
            Scanner reader = new Scanner(new File("tasks.txt"));

            while (reader.hasNextLine()) {

                String task = reader.nextLine();

                if (!(tasks.contains(task))) {
                    tasks.add(task);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        }
    }

    public void loadFromArray() {

        for (String entry : tasks) {

            String finalEntry = entry.substring(3); // excludes completion status from text
            int entryIndex = tasks.indexOf(entry);

            // setting up hBox to contain elements
            CheckBox completeBox = createCompleteBox();

            Button deleteButton = createDeleteButton();

            // task label
            Text task = new Text(finalEntry);

            // sets strikethrough based on completion status when task gets first loaded
            if (entry.startsWith("C")) {
                task.setStyle("-fx-padding: 8px; -fx-font-size: 20px; -fx-strikethrough: true; -fx-font-family: Chalkboard;");
                completeBox.setSelected(true);
            }
            else {
                task.setStyle("-fx-padding: 8px; -fx-font-size: 20px; -fx-font-family: Chalkboard;");
                completeBox.setSelected(false);
            }
            System.out.println(task.getText());
            // //

            // setting up completeButton
            completeBox.setOnAction(e -> {
                if (completeBox.isSelected()) {
                    tasks.set(entryIndex, "C: "+finalEntry); // sets task in array to 'completed'
                    task.setStyle("-fx-padding: 8px; -fx-font-size: 20px; -fx-strikethrough: true; -fx-font-family: Chalkboard;");
                    updateFile(finalEntry);
                }
                else {
                    tasks.set(entryIndex, "I: "+finalEntry); // sets task in array to 'uncompleted'
                    task.setStyle("-fx-padding: 8px; -fx-font-size: 20px; -fx-font-family: Chalkboard;");
                    updateFile(finalEntry);
                }
            });

            // adding all to container
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            HBox taskBox = new HBox();
            taskBox.setPrefHeight(10);

            deleteButton.setOnAction(e -> {
                System.out.println("removed: "+finalEntry);
                tasks.remove(entry);
                updateFile(finalEntry);
                tasksContainer.getChildren().remove(taskBox);
            });

            taskBox.getChildren().addAll(completeBox, task, spacer, deleteButton);

            tasksContainer.getChildren().addAll(taskBox);
        }
    }

    public void writeToFile(String task) {

        try {
            FileWriter writer = new FileWriter("tasks.txt", true);

            writer.write(task+ "\n");
            System.out.println("Written: "+task);
            writer.close();
        }
        catch (IOException e) {
            System.out.println("file not found.");
        }
    }


    // updates file by rewriting the file using the tasks array
    public void updateFile(String fileToUpdate) {
        System.out.println("entered function");

        try {
            FileWriter writer = new FileWriter("tasks.txt");

            for (String entry : tasks) {
                writer.write(entry + "\n");
            }
            writer.close();
            System.out.println("updated: "+fileToUpdate);
        }
        catch (IOException e) {
            System.out.println("File not found.");
        }
    }
}
// final code update (all works)