package com.lollypop.ui;

import com.lollypop.dao.DatabaseConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.sql.SQLException;
import java.util.Optional;

public class DatabaseConfigDialog {

    private final Stage dialogStage;
    private final TextField urlField;
    private final TextField userField;
    private final PasswordField passwordField;
    private final Label errorLabel;

    private DatabaseConnection.DbConfig result;

    public DatabaseConfigDialog(Window owner, DatabaseConnection.DbConfig initialConfig) {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            dialogStage.initOwner(owner);
        }
        dialogStage.setTitle("Database Connection");
        dialogStage.setResizable(false);

        urlField = new TextField(initialConfig.url());
        userField = new TextField(initialConfig.user());
        passwordField = new PasswordField();
        passwordField.setText(initialConfig.password());

        urlField.setPromptText("jdbc:mysql://host:3306/database?useSSL=false&serverTimezone=UTC");
        userField.setPromptText("Username");
        passwordField.setPromptText("Password");

        errorLabel = new Label();
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setStyle("-fx-text-fill: #B42318;");

        VBox root = new VBox(12,
                buildHeader(),
                buildForm(),
                errorLabel,
                buildActions()
        );
        root.setPadding(new Insets(18));
        root.setPrefWidth(520);

        dialogStage.setScene(new Scene(root));
    }

    public Optional<DatabaseConnection.DbConfig> showAndWait() {
        dialogStage.showAndWait();
        return Optional.ofNullable(result);
    }

    private VBox buildHeader() {
        Label title = new Label("Connect to the database");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label description = new Label(
                "Review the saved database settings, change them if needed, then connect. " +
                "The values are saved back to dbconfig.properties."
        );
        description.setWrapText(true);

        return new VBox(6, title, description);
    }

    private GridPane buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        addRow(grid, 0, "Database URL", urlField);
        addRow(grid, 1, "Username", userField);
        addRow(grid, 2, "Password", passwordField);

        return grid;
    }

    private void addRow(GridPane grid, int row, String labelText, TextField field) {
        Label label = new Label(labelText);
        grid.add(label, 0, row);
        grid.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private HBox buildActions() {
        Button cancelButton = new Button("Cancel");
        Button connectButton = new Button("Connect");
        connectButton.setDefaultButton(true);
        cancelButton.setCancelButton(true);

        cancelButton.setOnAction(event -> {
            result = null;
            dialogStage.close();
        });
        connectButton.setOnAction(event -> tryConnect());

        HBox actions = new HBox(10, cancelButton, connectButton);
        actions.setAlignment(Pos.CENTER_RIGHT);
        return actions;
    }

    private void tryConnect() {
        DatabaseConnection.DbConfig candidate = new DatabaseConnection.DbConfig(
                urlField.getText().trim(),
                userField.getText().trim(),
                passwordField.getText()
        );

        if (candidate.url().isBlank() || candidate.user().isBlank()) {
            showError("Database URL and username are required.");
            return;
        }

        try {
            DatabaseConnection.saveConfig(candidate);
            DatabaseConnection.testConnection(candidate);
            result = candidate;
            dialogStage.close();
        } catch (SQLException ex) {
            showError("Could not connect to the database: " + firstUsefulMessage(ex));
        } catch (RuntimeException ex) {
            showError("Could not save the database settings: " + firstUsefulMessage(ex));
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    private String firstUsefulMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) {
            if (current.getMessage() != null && !current.getMessage().isBlank()) {
                break;
            }
            current = current.getCause();
        }
        return current.getMessage() == null || current.getMessage().isBlank()
                ? error.getClass().getSimpleName()
                : current.getMessage();
    }
}
