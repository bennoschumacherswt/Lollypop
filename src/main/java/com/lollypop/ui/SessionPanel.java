package com.lollypop.ui;

import com.lollypop.model.Subscriber;
import com.lollypop.model.UserSession;
import com.lollypop.model.enums.ServiceType;
import com.lollypop.service.SubscriberService;
import com.lollypop.service.UserSessionService;
import com.lollypop.util.CrashRecoveryManager;
import com.lollypop.util.InputValidator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Optional;

public class SessionPanel {

    private final SubscriberService     subscriberService;
    private final UserSessionService    userSessionService;
    private final CrashRecoveryManager  recovery;

    private TableView<UserSession>       sessionTable;
    private ObservableList<UserSession>  sessionData;
    private Label                        statusLabel;

    private ComboBox<Subscriber> cbFilterSubscriber;
    private ComboBox<Subscriber> cbStartSubscriber;
    private ComboBox<ServiceType> cbService;
    private TextField             tfDuration;

    private ObservableList<Subscriber> allSubscribers = FXCollections.observableArrayList();

    public SessionPanel(SubscriberService svc, UserSessionService usSvc, CrashRecoveryManager r) {
        this.subscriberService  = svc;
        this.userSessionService = usSvc;
        this.recovery           = r;
    }

    public BorderPane build() {
        try { allSubscribers.setAll(subscriberService.getAllSubscribers()); }
        catch (Exception ignored) {}

        BorderPane pane = new BorderPane();
        pane.getStyleClass().add("content-pane");

        VBox header = new VBox(2);
        header.setPadding(new Insets(16, 0, 16, 0));
        Label title = new Label("Sessions");
        title.getStyleClass().add("page-title");
        Label sub = new Label("Start and view subscriber usage sessions");
        sub.getStyleClass().add("page-subtitle");
        header.getChildren().addAll(title, sub);

        HBox body = new HBox(16);
        body.setFillHeight(true);

        VBox tableCard = buildSessionTableCard();
        VBox formCard  = buildStartSessionCard();
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        formCard.setPrefWidth(310);
        formCard.setMinWidth(290);
        body.getChildren().addAll(tableCard, formCard);

        pane.setTop(header);
        pane.setCenter(body);
        return pane;
    }

    // -----------------------------------------------------------------------
    // Table card
    // -----------------------------------------------------------------------

    private VBox buildSessionTableCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        VBox.setVgrow(card, Priority.ALWAYS);

        Label cardTitle = new Label("Sessions");
        cardTitle.getStyleClass().add("card-title");

        statusLabel = new Label("Select a subscriber to view or start sessions.");
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        styleStatus(null);

        sessionTable = new TableView<>();
        sessionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(sessionTable, Priority.ALWAYS);

        TableColumn<UserSession, String> cId  = col("ID",        s -> String.valueOf(s.getId()), 50);
        TableColumn<UserSession, String> cSvc = col("Service",   s -> s.getServiceType().name().replace("_", " "), 200);
        TableColumn<UserSession, String> cDur = col("Duration",  s -> s.getDurationSeconds() + " s", 90);
        TableColumn<UserSession, String> cDat = col("Data (MB)", s -> String.format("%.2f", s.getUsedDataVolumeMb()), 100);

        sessionTable.getColumns().addAll(cId, cSvc, cDur, cDat);
        sessionData = FXCollections.observableArrayList();
        sessionTable.setItems(sessionData);
        sessionTable.setPlaceholder(new Label("No sessions loaded."));

        cbFilterSubscriber = buildSubscriberCombo();
        cbFilterSubscriber.valueProperty().addListener((obs, old, sel) -> {
            if (sel != null) loadSessions(sel.getId());
        });

        Button btnLoad = new Button("Load Sessions");
        btnLoad.getStyleClass().add("btn-secondary");
        btnLoad.setOnAction(e -> {
            Subscriber sel = cbFilterSubscriber.getValue();
            if (sel != null) loadSessions(sel.getId());
            else setStatus("Please select a subscriber first.", false);
        });

        HBox filterRow = new HBox(8, cbFilterSubscriber, btnLoad);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(cbFilterSubscriber, Priority.ALWAYS);

        card.getChildren().addAll(cardTitle, filterRow, statusLabel, sessionTable);
        return card;
    }

    // -----------------------------------------------------------------------
    // Start session card
    // -----------------------------------------------------------------------

    private VBox buildStartSessionCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        Label cardTitle = new Label("Start Session");
        cardTitle.getStyleClass().add("card-title");

        cbStartSubscriber = buildSubscriberCombo();

        tfDuration = new TextField();
        tfDuration.setPromptText("Duration (seconds)");
        tfDuration.getStyleClass().add("text-field");
        tfDuration.setMaxWidth(Double.MAX_VALUE);

        cbService = new ComboBox<>(FXCollections.observableArrayList(ServiceType.values()));
        cbService.setPromptText("Service Type");
        cbService.getStyleClass().add("combo-box");
        cbService.setMaxWidth(Double.MAX_VALUE);

        Button btnStart = new Button("▶  Start Session");
        btnStart.getStyleClass().add("btn-primary");
        btnStart.setMaxWidth(Double.MAX_VALUE);
        btnStart.setOnAction(e -> startSession());

        card.getChildren().addAll(
            cardTitle,
            fieldBlock("Subscriber", cbStartSubscriber),
            fieldBlock("Service", cbService),
            fieldBlock("Duration (seconds)", tfDuration),
            new Separator(),
            btnStart
        );
        return card;
    }

    // -----------------------------------------------------------------------
    // Filterable ComboBox factory
    // -----------------------------------------------------------------------

    private ComboBox<Subscriber> buildSubscriberCombo() {
        FilteredList<Subscriber> filtered = new FilteredList<>(allSubscribers, s -> true);

        ComboBox<Subscriber> combo = new ComboBox<>(filtered);
        combo.setEditable(true);
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.getStyleClass().add("combo-box");
        combo.setPromptText("Type to search subscriber…");

        StringConverter<Subscriber> converter = new StringConverter<>() {
            @Override public String toString(Subscriber s) {
                if (s == null) return "";
                return String.format("ID: %d  |  %s %s  |  MSIN: %s",
                    s.getId(), s.getFirstname(), s.getLastname(), s.getMsin());
            }
            @Override public Subscriber fromString(String str) {
                // Return current value if the text matches it
                Subscriber cur = combo.getValue();
                if (cur != null && toString(cur).equals(str)) return cur;
                return null;
            }
        };
        combo.setConverter(converter);

        // Filter the list as the user types in the editor
        combo.getEditor().textProperty().addListener((obs, old, text) -> {
            // If the text exactly matches the currently selected item, don't re-filter
            Subscriber selected = combo.getValue();
            if (selected != null && converter.toString(selected).equals(text)) return;

            String q = text == null ? "" : text.trim().toLowerCase();
            filtered.setPredicate(s -> {
                if (q.isEmpty()) return true;
                return String.valueOf(s.getId()).contains(q)
                    || String.valueOf(s.getMsin()).contains(q)
                    || s.getImsi().toLowerCase().contains(q)
                    || (s.getFirstname() + " " + s.getLastname()).toLowerCase().contains(q);
            });

            // Keep popup open while typing
            if (!combo.isShowing()) combo.show();
        });

        // When a value is actually selected from the list, stop filtering
        combo.valueProperty().addListener((obs, old, nv) -> {
            if (nv != null) filtered.setPredicate(s -> true);
        });

        return combo;
    }

    // -----------------------------------------------------------------------
    // Logic
    // -----------------------------------------------------------------------

    private void startSession() {
        Subscriber sel = cbStartSubscriber.getValue();
        if (sel == null) {
            setStatus("Please select a subscriber first.", false);
            return;
        }
        try {
            ServiceType svc = InputValidator.requireEnum(cbService.getValue(), "Service Type");
            int duration    = InputValidator.validatePositiveInt(tfDuration.getText(), "Duration");
            Optional<String> result = subscriberService.doUserSession(sel.getId(), svc, duration);
            if (result.isPresent()) {
                setStatus("Session started: " + result.get(), true);
                recovery.checkpoint("Session for subscriber id=" + sel.getId() + " service=" + svc);
                try { allSubscribers.setAll(subscriberService.getAllSubscribers()); } catch (Exception ignored) {}
                loadSessions(sel.getId());
            } else {
                setStatus("Session failed — quota exhausted or signal too weak.", false);
            }
        } catch (InputValidator.ValidationException | IllegalArgumentException ex) {
            setStatus("Error: " + ex.getMessage(), false);
        }
    }

    private void loadSessions(int subscriberId) {
        try {
            List<UserSession> sessions = userSessionService.getSessionsForSubscriber(subscriberId);
            sessionData.setAll(sessions);
            setStatus("Loaded " + sessions.size() + " sessions for subscriber " + subscriberId + ".", true);
        } catch (Exception ex) {
            setStatus("Error loading sessions: " + ex.getMessage(), false);
        }
    }

    private void setStatus(String msg, boolean ok) {
        statusLabel.setText(msg);
        styleStatus(ok);
    }

    private void styleStatus(Boolean ok) {
        if (ok == null) {
            statusLabel.setStyle("-fx-padding:6 10 6 10;-fx-background-color:#F7F9FC;" +
                "-fx-border-color:#DDE1EA;-fx-border-radius:6;-fx-background-radius:6;-fx-text-fill:#5C6B8A;");
        } else if (ok) {
            statusLabel.setStyle("-fx-padding:6 10 6 10;-fx-background-color:#F0FFF4;" +
                "-fx-border-color:#B7EBCA;-fx-border-radius:6;-fx-background-radius:6;-fx-text-fill:#2B9348;");
        } else {
            statusLabel.setStyle("-fx-padding:6 10 6 10;-fx-background-color:#FFF0F0;" +
                "-fx-border-color:#FFBDBD;-fx-border-radius:6;-fx-background-radius:6;-fx-text-fill:#D62828;");
        }
    }

    private <T> TableColumn<UserSession, String> col(String t,
            java.util.function.Function<UserSession, String> fn, double w) {
        TableColumn<UserSession, String> c = new TableColumn<>(t);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        c.setPrefWidth(w);
        return c;
    }

    private VBox fieldBlock(String label, javafx.scene.Node field) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("field-label");
        return new VBox(3, lbl, field);
    }
}
