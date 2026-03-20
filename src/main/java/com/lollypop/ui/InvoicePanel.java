package com.lollypop.ui;

import com.lollypop.model.Subscriber;
import com.lollypop.model.UserSession;
import com.lollypop.service.SubscriberService;
import com.lollypop.service.UserSessionService;
import com.lollypop.util.CrashRecoveryManager;
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

public class InvoicePanel {

    private final SubscriberService     subscriberService;
    private final UserSessionService    userSessionService;
    private final CrashRecoveryManager  recovery;

    private TextArea             invoiceArea;
    private Label                statusLabel;
    private ComboBox<Subscriber> cbSubscriber;

    private ObservableList<Subscriber> allSubscribers = FXCollections.observableArrayList();

    public InvoicePanel(SubscriberService svc, UserSessionService usSvc, CrashRecoveryManager r) {
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
        Label title = new Label("Invoices");
        title.getStyleClass().add("page-title");
        Label sub = new Label("Generate and view subscriber invoices");
        sub.getStyleClass().add("page-subtitle");
        header.getChildren().addAll(title, sub);

        HBox body = new HBox(16);
        body.setFillHeight(true);

        VBox controlCard = buildControlCard();
        VBox invoiceCard = buildInvoiceCard();
        controlCard.setPrefWidth(300);
        controlCard.setMinWidth(280);
        HBox.setHgrow(invoiceCard, Priority.ALWAYS);
        body.getChildren().addAll(controlCard, invoiceCard);

        pane.setTop(header);
        pane.setCenter(body);
        return pane;
    }

    // -----------------------------------------------------------------------
    // Control card
    // -----------------------------------------------------------------------

    private VBox buildControlCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        Label cardTitle = new Label("Invoice Options");
        cardTitle.getStyleClass().add("card-title");

        cbSubscriber = buildSubscriberCombo();

        Button btnGenerate = new Button("🧾  Generate Invoice");
        btnGenerate.getStyleClass().add("btn-primary");
        btnGenerate.setMaxWidth(Double.MAX_VALUE);
        btnGenerate.setOnAction(e -> generateInvoice());

        Button btnPreview = new Button("👁  Preview (no reset)");
        btnPreview.getStyleClass().add("btn-secondary");
        btnPreview.setMaxWidth(Double.MAX_VALUE);
        btnPreview.setOnAction(e -> previewInvoice());

        Button btnClear = new Button("Clear");
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setMaxWidth(Double.MAX_VALUE);
        btnClear.setOnAction(e -> {
            invoiceArea.clear();
            cbSubscriber.setValue(null);
            cbSubscriber.getEditor().clear();
            setStatus("Cleared.", true);
        });

        Label note = new Label("ℹ  Generate resets quota & clears sessions. Preview shows current state without resetting.");
        note.setWrapText(true);
        note.setStyle("-fx-text-fill: #8A93A8; -fx-font-size: 12px;");

        card.getChildren().addAll(
            cardTitle,
            fieldBlock("Subscriber", cbSubscriber),
            btnGenerate, btnPreview, btnClear,
            new Separator(),
            note
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
                Subscriber cur = combo.getValue();
                if (cur != null && toString(cur).equals(str)) return cur;
                return null;
            }
        };
        combo.setConverter(converter);

        combo.getEditor().textProperty().addListener((obs, old, text) -> {
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

            if (!combo.isShowing()) combo.show();
        });

        combo.valueProperty().addListener((obs, old, nv) -> {
            if (nv != null) filtered.setPredicate(s -> true);
        });

        return combo;
    }

    // -----------------------------------------------------------------------
    // Invoice card (status above invoice area)
    // -----------------------------------------------------------------------

    private VBox buildInvoiceCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        VBox.setVgrow(card, Priority.ALWAYS);

        Label cardTitle = new Label("Invoice Preview");
        cardTitle.getStyleClass().add("card-title");

        statusLabel = new Label("Select a subscriber and click Generate or Preview.");
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        styleStatus(null);

        invoiceArea = new TextArea();
        invoiceArea.setEditable(false);
        invoiceArea.setWrapText(false);
        invoiceArea.getStyleClass().add("invoice-box");
        invoiceArea.setPromptText("Invoice will appear here...");
        VBox.setVgrow(invoiceArea, Priority.ALWAYS);
        invoiceArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 13px;");

        card.getChildren().addAll(cardTitle, statusLabel, invoiceArea);
        return card;
    }

    // -----------------------------------------------------------------------
    // Logic
    // -----------------------------------------------------------------------

    private void generateInvoice() {
        Subscriber sel = cbSubscriber.getValue();
        if (sel == null) { setStatus("Please select a subscriber first.", false); return; }
        try {
            String invoice = subscriberService.generateInvoice(sel.getId());
            invoiceArea.setText(invoice);
            recovery.checkpoint("Invoice generated for subscriber id=" + sel.getId());
            try { allSubscribers.setAll(subscriberService.getAllSubscribers()); } catch (Exception ignored) {}
            setStatus("Invoice generated. Quota reset, sessions cleared.", true);
        } catch (IllegalArgumentException ex) {
            setStatus("Error: " + ex.getMessage(), false);
        } catch (Exception ex) {
            setStatus("Failed to generate invoice: " + ex.getMessage(), false);
        }
    }

    private void previewInvoice() {
        Subscriber sel = cbSubscriber.getValue();
        if (sel == null) { setStatus("Please select a subscriber first.", false); return; }
        try {
            Optional<Subscriber> opt = subscriberService.getSubscriber(sel.getId());
            if (opt.isEmpty()) { setStatus("Subscriber not found.", false); return; }

            Subscriber s = opt.get();
            List<UserSession> sessions = userSessionService.getSessionsForSubscriber(sel.getId());

            double totalVoiceSeconds = 0, totalDataMb = 0;
            for (UserSession us : sessions) {
                if (us.getServiceType().isVoice()) totalVoiceSeconds += us.getDurationSeconds();
                else totalDataMb += us.getUsedDataVolumeMb();
            }
            double totalVoiceMin = totalVoiceSeconds / 60.0;
            double inclMin       = s.getSubscriptionType().getIncludedMinutes();
            double extraMin      = Math.max(0, totalVoiceMin - inclMin);
            double voiceCharge   = extraMin * s.getSubscriptionType().getPricePerExtraMinuteEur();
            double total         = s.getSubscriptionType().getBaseFeeEur() + voiceCharge;

            String preview = String.format(
                "================================================%n" +
                "          MATSECOM INVOICE  (PREVIEW)           %n" +
                "================================================%n" +
                "Subscriber   : %s %s%n" +
                "IMSI         : %s%n" +
                "Plan         : %s%n" +
                "------------------------------------------------%n" +
                "Voice minutes used   : %.2f min%n" +
                "  Included minutes   : %.0f min%n" +
                "  Extra minutes      : %.2f min%n" +
                "  Voice charge       : €%.2f%n" +
                "------------------------------------------------%n" +
                "Data used            : %.2f MB%n" +
                "Remaining data       : %.2f MB%n" +
                "------------------------------------------------%n" +
                "Base fee             : €%.2f%n" +
                "TOTAL                : €%.2f%n" +
                "================================================%n" +
                "(Preview only — no data has been reset)%n",
                s.getFirstname(), s.getLastname(),
                s.getImsi(), s.getSubscriptionType().name(),
                totalVoiceMin, inclMin, extraMin, voiceCharge,
                totalDataMb, s.getRemainingDataMb(),
                s.getSubscriptionType().getBaseFeeEur(), total
            );
            invoiceArea.setText(preview);
            setStatus("Preview generated for subscriber " + sel.getId() + ".", true);
        } catch (Exception ex) {
            setStatus("Error: " + ex.getMessage(), false);
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

    private VBox fieldBlock(String label, javafx.scene.Node field) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("field-label");
        return new VBox(3, lbl, field);
    }
}
