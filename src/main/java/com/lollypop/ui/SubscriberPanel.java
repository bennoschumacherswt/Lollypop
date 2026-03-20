package com.lollypop.ui;

import com.lollypop.model.Subscriber;
import com.lollypop.model.enums.SubscriptionType;
import com.lollypop.model.enums.TerminalType;
import com.lollypop.service.SubscriberService;
import com.lollypop.util.CrashRecoveryManager;
import com.lollypop.util.InputValidator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.List;
import java.util.Optional;

public class SubscriberPanel {

    private final SubscriberService subscriberService;
    private final CrashRecoveryManager recovery;

    private TableView<Subscriber> table;
    private ObservableList<Subscriber> tableData;
    private Label statusLabel;

    // Form fields
    private TextField tfMsin, tfFirstname, tfLastname;
    private ComboBox<TerminalType>    cbTerminal;
    private ComboBox<SubscriptionType> cbPlan;

    private Subscriber editingSubscriber = null;

    public SubscriberPanel(SubscriberService svc, CrashRecoveryManager recovery) {
        this.subscriberService = svc;
        this.recovery = recovery;
    }

    public BorderPane build() {
        BorderPane pane = new BorderPane();
        pane.getStyleClass().add("content-pane");

        // ── Header ────────────────────────────────────────────
        VBox header = new VBox(4);
        header.setPadding(new Insets(16, 0, 16, 0));

        // Title row with Import/Export buttons on the right
        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBlock = new VBox(2);
        Label title = new Label("Subscribers");
        title.getStyleClass().add("page-title");
        Label sub = new Label("Manage subscriber accounts");
        sub.getStyleClass().add("page-subtitle");
        titleBlock.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnImport = new Button("📂  Import CSV");
        Button btnExport = new Button("💾  Export CSV");
        btnImport.getStyleClass().add("btn-secondary");
        btnExport.getStyleClass().add("btn-success");
        btnImport.setOnAction(e -> doImport());
        btnExport.setOnAction(e -> doExport());

        HBox importExportButtons = new HBox(8, btnImport, btnExport);
        importExportButtons.setAlignment(Pos.CENTER_RIGHT);

        titleRow.getChildren().addAll(titleBlock, spacer, importExportButtons);
        header.getChildren().add(titleRow);

        // ── Main body ─────────────────────────────────────────
        HBox body = new HBox(16);
        body.setFillHeight(true);

        VBox tableCard = buildTableCard();
        VBox formCard  = buildFormCard();
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        formCard.setPrefWidth(300);
        formCard.setMinWidth(280);

        body.getChildren().addAll(tableCard, formCard);

        pane.setTop(header);
        pane.setCenter(body);

        refreshTable();
        return pane;
    }

    // -----------------------------------------------------------------------
    // Table card
    // -----------------------------------------------------------------------

    private VBox buildTableCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        VBox.setVgrow(card, Priority.ALWAYS);

        Label cardTitle = new Label("All Subscribers");
        cardTitle.getStyleClass().add("card-title");

        // Status label lives here, above the table
        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-info");
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        statusLabel.setStyle("-fx-padding:6 10 6 10;-fx-background-color:#F7F9FC;" +
                             "-fx-border-color:#DDE1EA;-fx-border-radius:6;-fx-background-radius:6;");

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Subscriber, String> colId   = col("ID",    s -> String.valueOf(s.getId()), 45);
        TableColumn<Subscriber, String> colName = col("Name",  s -> s.getFirstname() + " " + s.getLastname(), 150);
        TableColumn<Subscriber, String> colMsin = col("MSIN",  s -> String.valueOf(s.getMsin()), 120);
        TableColumn<Subscriber, String> colImsi = col("IMSI",  Subscriber::getImsi, 140);
        TableColumn<Subscriber, String> colTerm = col("Terminal", s -> s.getTerminalType().name(), 110);
        TableColumn<Subscriber, String> colPlan = col("Plan",  s -> s.getSubscriptionType().name(), 110);
        TableColumn<Subscriber, String> colData = col("Data (MB)", s -> String.format("%.2f", s.getRemainingDataMb()), 90);

        table.getColumns().addAll(colId, colName, colMsin, colImsi, colTerm, colPlan, colData);
        tableData = FXCollections.observableArrayList();
        table.setItems(tableData);
        table.setPlaceholder(new Label("No subscribers found."));

        // Selection fills form
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) fillForm(sel);
        });

        // Action buttons row
        Button btnRefresh = new Button("↻ Refresh");
        Button btnDelete  = new Button("✕ Delete");
        btnRefresh.getStyleClass().add("btn-secondary");
        btnDelete.getStyleClass().add("btn-danger");
        btnRefresh.setOnAction(e -> refreshTable());
        btnDelete.setOnAction(e -> deleteSelected());

        HBox actions = new HBox(8, btnRefresh, btnDelete);
        actions.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(cardTitle, statusLabel, table, actions);
        return card;
    }

    // -----------------------------------------------------------------------
    // Form card
    // -----------------------------------------------------------------------

    private VBox buildFormCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        Label cardTitle = new Label("Add / Edit Subscriber");
        cardTitle.getStyleClass().add("card-title");

        tfMsin      = styledField("MSIN (10 digits)");
        tfFirstname = styledField("First Name");
        tfLastname  = styledField("Last Name");
        cbTerminal  = new ComboBox<>(FXCollections.observableArrayList(TerminalType.values()));
        cbPlan      = new ComboBox<>(FXCollections.observableArrayList(SubscriptionType.values()));
        styleCombo(cbTerminal, "Terminal Type");
        styleCombo(cbPlan,     "Subscription Plan");

        Button btnSave  = new Button("💾  Save");
        Button btnClear = new Button("Clear");
        btnSave.getStyleClass().add("btn-primary");
        btnClear.getStyleClass().add("btn-secondary");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnClear.setMaxWidth(Double.MAX_VALUE);
        btnSave.setOnAction(e -> saveSubscriber());
        btnClear.setOnAction(e -> clearForm());

        card.getChildren().addAll(
            cardTitle,
            fieldBlock("MSIN", tfMsin),
            fieldBlock("First Name", tfFirstname),
            fieldBlock("Last Name", tfLastname),
            fieldBlock("Terminal Type", cbTerminal),
            fieldBlock("Subscription Plan", cbPlan),
            new Separator(),
            new HBox(8, btnSave, btnClear)
        );
        return card;
    }

    // -----------------------------------------------------------------------
    // Logic
    // -----------------------------------------------------------------------

    private void saveSubscriber() {
        try {
            long msin      = InputValidator.validateMsin(tfMsin.getText());
            String fn      = InputValidator.validateName(tfFirstname.getText(), "First Name");
            String ln      = InputValidator.validateName(tfLastname.getText(), "Last Name");
            TerminalType tt     = InputValidator.requireEnum(cbTerminal.getValue(), "Terminal Type");
            SubscriptionType st = InputValidator.requireEnum(cbPlan.getValue(), "Subscription Plan");

            if (editingSubscriber != null) {
                subscriberService.updateSubscriber(editingSubscriber.getId(), fn, ln, tt, st);
                setStatus("Subscriber updated successfully.", true);
                recovery.checkpoint("Updated subscriber id=" + editingSubscriber.getId());
            } else {
                Subscriber created = subscriberService.addSubscriber(msin, fn, ln, tt, st);
                setStatus("Subscriber added with ID " + created.getId(), true);
                recovery.checkpoint("Added subscriber id=" + created.getId());
            }
            clearForm();
            refreshTable();

        } catch (InputValidator.ValidationException | IllegalArgumentException ex) {
            setStatus("Validation error: " + ex.getMessage(), false);
        } catch (Exception ex) {
            setStatus("Error: " + ex.getMessage(), false);
        }
    }

    private void deleteSelected() {
        Subscriber sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { setStatus("Select a subscriber to delete.", false); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete subscriber '" + sel.getFirstname() + " " + sel.getLastname() + "' and all their sessions?",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try {
                    subscriberService.removeSubscriber(sel.getId());
                    recovery.checkpoint("Deleted subscriber id=" + sel.getId());
                    setStatus("Subscriber deleted.", true);
                    clearForm();
                    refreshTable();
                } catch (Exception ex) {
                    setStatus("Delete failed: " + ex.getMessage(), false);
                }
            }
        });
    }

    private void fillForm(Subscriber s) {
        editingSubscriber = s;
        tfMsin.setText(String.valueOf(s.getMsin()));
        tfMsin.setDisable(true);
        tfFirstname.setText(s.getFirstname());
        tfLastname.setText(s.getLastname());
        cbTerminal.setValue(s.getTerminalType());
        cbPlan.setValue(s.getSubscriptionType());
    }

    private void clearForm() {
        editingSubscriber = null;
        tfMsin.setText("");
        tfMsin.setDisable(false);
        tfFirstname.setText("");
        tfLastname.setText("");
        cbTerminal.setValue(null);
        cbPlan.setValue(null);
        table.getSelectionModel().clearSelection();
    }

    private void refreshTable() {
        try {
            List<Subscriber> all = subscriberService.getAllSubscribers();
            tableData.setAll(all);
        } catch (Exception ex) {
            setStatus("Failed to load subscribers: " + ex.getMessage(), false);
        }
    }

    private void setStatus(String msg, boolean ok) {
        statusLabel.setText(msg);
        statusLabel.getStyleClass().removeAll("status-ok", "status-error", "status-info");
        statusLabel.getStyleClass().add(ok ? "status-ok" : "status-error");
        statusLabel.setStyle("-fx-padding:6 10 6 10;-fx-background-color:"
            + (ok ? "#F0FFF4" : "#FFF0F0") + ";"
            + "-fx-border-color:" + (ok ? "#B7EBCA" : "#FFBDBD") + ";"
            + "-fx-border-radius:6;-fx-background-radius:6;");
    }

    // -----------------------------------------------------------------------
    // Import / Export (inline in Subscribers page)
    // -----------------------------------------------------------------------

    private void doImport() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select CSV File to Import");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv", "*.txt"));
        File file = fc.showOpenDialog(null);
        if (file == null) return;

        int imported = 0, skipped = 0, errors = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNum = 0;
            while ((line = br.readLine()) != null) {
                lineNum++;
                if (line.isBlank()) continue;
                String[] parts = line.split(",", -1);
                String col2 = parts.length > 2 ? parts[2].trim() : "";
                if (lineNum == 1 && (col2.isEmpty() || !Character.isDigit(col2.charAt(0)))) continue;
                if (parts.length < 5) { skipped++; continue; }
                try {
                    String fn = InputValidator.validateName(parts[0], "firstname");
                    String ln = InputValidator.validateName(parts[1], "lastname");
                    String msinStr = parts[2].trim().replaceAll("\\s+", "");
                    if (msinStr.length() > 10) msinStr = msinStr.substring(msinStr.length() - 10);
                    long msin = InputValidator.validateMsin(msinStr);
                    TerminalType tt = terminalFromIndex(parts[3].trim());
                    SubscriptionType st = planFromIndex(parts[4].trim());
                    subscriberService.addSubscriber(msin, fn, ln, tt, st);
                    imported++;
                } catch (Exception ex) { errors++; }
            }
        } catch (IOException ex) {
            setStatus("Import failed: " + ex.getMessage(), false);
            return;
        }
        recovery.checkpoint("Import: " + imported + " rows from " + file.getName());
        String summary = String.format("Import complete — %d imported, %d skipped, %d errors.", imported, skipped, errors);
        setStatus(summary, errors == 0);
        refreshTable();
    }

    private void doExport() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Subscribers as CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fc.setInitialFileName("subscribers_export.csv");
        File file = fc.showSaveDialog(null);
        if (file == null) return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("firstname,lastname,imsi,terminal_type,subscription_type,remaining_data_mb");
            bw.newLine();
            List<Subscriber> all = subscriberService.getAllSubscribers();
            for (Subscriber s : all) {
                bw.write(csvEscape(s.getFirstname()) + "," + csvEscape(s.getLastname()) + "," +
                    s.getImsi() + "," + s.getTerminalType().name() + "," +
                    s.getSubscriptionType().name() + "," + String.format("%.4f", s.getRemainingDataMb()));
                bw.newLine();
            }
            recovery.checkpoint("Export: " + all.size() + " rows to " + file.getName());
            setStatus("Exported " + all.size() + " subscribers.", true);
        } catch (IOException ex) {
            setStatus("Export failed: " + ex.getMessage(), false);
        }
    }

    private TerminalType terminalFromIndex(String s) {
        try { return TerminalType.values()[Integer.parseInt(s)]; } catch (Exception e) {
            for (TerminalType t : TerminalType.values()) if (t.name().equalsIgnoreCase(s)) return t;
            throw new IllegalArgumentException("Unknown terminal: " + s);
        }
    }

    private SubscriptionType planFromIndex(String s) {
        try { return SubscriptionType.values()[Integer.parseInt(s)]; } catch (Exception e) {
            for (SubscriptionType t : SubscriptionType.values()) if (t.name().equalsIgnoreCase(s)) return t;
            throw new IllegalArgumentException("Unknown plan: " + s);
        }
    }

    private String csvEscape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n"))
            return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private <T> TableColumn<Subscriber, String> col(String title, java.util.function.Function<Subscriber, String> extractor, double prefW) {
        TableColumn<Subscriber, String> c = new TableColumn<>(title);
        c.setCellValueFactory(d -> new SimpleStringProperty(extractor.apply(d.getValue())));
        c.setPrefWidth(prefW);
        return c;
    }

    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("text-field");
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private <T> void styleCombo(ComboBox<T> cb, String prompt) {
        cb.setPromptText(prompt);
        cb.getStyleClass().add("combo-box");
        cb.setMaxWidth(Double.MAX_VALUE);
    }

    private VBox fieldBlock(String label, javafx.scene.Node field) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("field-label");
        VBox block = new VBox(3, lbl, field);
        return block;
    }
}
