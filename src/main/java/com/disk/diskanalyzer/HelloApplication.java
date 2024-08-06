package com.disk.diskanalyzer;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

public class HelloApplication extends Application {
    private Stage stage;
    private Map<String, Long> sizes;
    private ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
    private PieChart pieChart;
    private Button backButton;
    private Stack<String> pathStack = new Stack<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        stage.setTitle("Disk Analyzer");

        // Установка иконки
        stage.getIcons().add(new Image("file:resources/icon.png")); // Укажите путь к вашей иконке

        Button chooseDirButton = new Button("Choose directory");
        chooseDirButton.setOnAction(event -> {
            File file = new DirectoryChooser().showDialog(stage);
            if (file != null) {
                String path = file.getAbsolutePath();
                loadDirectory(Path.of(path));
            }
        });

        StackPane pane = new StackPane();
        pane.getChildren().add(chooseDirButton);
        stage.setScene(new Scene(pane, 300, 250));
        stage.show();
    }

    private void loadDirectory(Path path) {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(true);

        StackPane loadingPane = new StackPane(progressIndicator);
        stage.setScene(new Scene(loadingPane, 300, 250));

        Task<Map<String, Long>> task = new Task<>() {
            @Override
            protected Map<String, Long> call() throws Exception {
                return new Analyzer().calculateDirectorySize(path);
            }
        };

        task.setOnSucceeded(event -> {
            sizes = task.getValue();
            pathStack.clear();
            pathStack.push(path.toString());
            buildChart(path.toString());
        });

        task.setOnFailed(event -> {
            task.getException().printStackTrace();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void buildChart(String path) {
        pieChart = new PieChart(pieChartData);

        backButton = new Button("<");
        backButton.setOnAction(event -> {
            if (pathStack.size() > 1) {
                pathStack.pop();
                String previousPath = pathStack.peek();
                refillChart(previousPath);
            }
        });

        refillChart(path);

        BorderPane pane = new BorderPane();
        pane.setTop(backButton);
        pane.setCenter(pieChart);

        stage.setScene(new Scene(pane, 900, 600));
        stage.show();
    }

    private void refillChart(String path) {
        pieChartData.clear();
        pieChartData.addAll(
                sizes.entrySet().stream()
                        .filter(entry -> {
                            Path parent = Path.of(entry.getKey()).getParent();
                            return parent != null && parent.toString().equals(path);
                        })
                        .map(entry -> new PieChart.Data(Path.of(entry.getKey()).getFileName().toString(), entry.getValue()))
                        .collect(Collectors.toList())
        );

        pieChart.getData().forEach(data -> {
            data.getNode().addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
                String fullPath = Path.of(path, data.getName()).toString();
                if (Files.isDirectory(Path.of(fullPath))) {
                    pathStack.push(fullPath);
                    refillChart(fullPath);
                }
            });
        });
    }
}
