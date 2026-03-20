package com.lollypop.ui;

import com.lollypop.ServiceFactory;
import com.lollypop.service.SubscriberService;
import com.lollypop.service.UserSessionService;
import com.lollypop.util.CrashRecoveryManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Top-level window shell for the Lollypop SMS application.
 * Hosts a left sidebar navigation and a center content area.
 */
public class MainWindow {

    private final Stage stage;
    private final CrashRecoveryManager recovery;

    private final ServiceFactory sf;
    private final SubscriberService subscriberService;
    private final UserSessionService userSessionService;

    private BorderPane root;
    private StackPane  contentArea;

    // Nav buttons
    private Button activeNavBtn = null;

    public MainWindow(Stage stage, CrashRecoveryManager recovery) {
        this.stage    = stage;
        this.recovery = recovery;
        this.sf                  = new ServiceFactory();
        this.subscriberService   = sf.getSubscriberService();
        this.userSessionService  = sf.getUserSessionService();
    }

    public void show() {
        root        = new BorderPane();
        contentArea = new StackPane();

        root.setLeft(buildSidebar());
        root.setCenter(contentArea);

        // Default view
        showSubscriberPanel();

        Scene scene = new Scene(root, 1100, 720);
        applyGlobalStyles(scene);

        stage.setTitle("Lollypop — Matsecom Subscriber Management");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    // -----------------------------------------------------------------------
    // Sidebar
    // -----------------------------------------------------------------------

    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(210);
        sidebar.setSpacing(0);

        // Logo area
        VBox logoArea = new VBox();
        logoArea.getStyleClass().add("logo-area");
        logoArea.setPrefHeight(110);
        logoArea.setAlignment(Pos.CENTER);

        Label logoPlaceholder = new Label("◉");
        logoPlaceholder.getStyleClass().add("logo-placeholder-icon");
        Label logoText = new Label("LOLLYPOP");
        logoText.getStyleClass().add("logo-text");
        Label logoSub  = new Label("Subscriber Management");
        logoSub.getStyleClass().add("logo-subtext");

        logoArea.getChildren().addAll(logoPlaceholder, logoText, logoSub);

        // Separator
        Separator sep = new Separator();
        sep.getStyleClass().add("sidebar-sep");

        // Navigation buttons
        Button btnSubscribers = navButton("👥  Subscribers",  () -> showSubscriberPanel());
        Button btnSessions    = navButton("📡  Sessions",     () -> showSessionPanel());
        Button btnInvoices    = navButton("🧾  Invoices",     () -> showInvoicePanel());

        // Bottom controls
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button btnHelp  = utilButton("❓  Help");
        Button btnDocs  = utilButton("📖  Documentation");
        Button btnExit  = utilButton("⏻   Exit");

        btnHelp.setOnAction(e -> showHelpDialog());
        btnDocs.setOnAction(e -> showDocumentationDialog());
        btnExit.setOnAction(e -> {
            recovery.deleteSafetyFile();
            Platform.exit();
        });

        VBox bottomButtons = new VBox(4, btnHelp, btnDocs, btnExit);
        bottomButtons.setPadding(new Insets(8, 12, 16, 12));

        sidebar.getChildren().addAll(
                logoArea, sep,
                btnSubscribers, btnSessions, btnInvoices,
                spacer, bottomButtons
        );

        // Activate first button
        setActiveNav(btnSubscribers);
        btnSubscribers.setOnAction(e -> { showSubscriberPanel(); setActiveNav(btnSubscribers); });
        btnSessions.setOnAction(e -> { showSessionPanel();    setActiveNav(btnSessions); });
        btnInvoices.setOnAction(e -> { showInvoicePanel();   setActiveNav(btnInvoices); });

        return sidebar;
    }

    private Button navButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private Button utilButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("util-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        return btn;
    }

    private void setActiveNav(Button btn) {
        if (activeNavBtn != null) activeNavBtn.getStyleClass().remove("nav-btn-active");
        btn.getStyleClass().add("nav-btn-active");
        activeNavBtn = btn;
    }

    // -----------------------------------------------------------------------
    // Panel switching
    // -----------------------------------------------------------------------

    private void showSubscriberPanel() {
        contentArea.getChildren().setAll(
            new SubscriberPanel(subscriberService, recovery).build()
        );
    }

    private void showSessionPanel() {
        contentArea.getChildren().setAll(
            new SessionPanel(subscriberService, userSessionService, recovery).build()
        );
    }

    private void showInvoicePanel() {
        contentArea.getChildren().setAll(
            new InvoicePanel(subscriberService, userSessionService, recovery).build()
        );
    }

    // -----------------------------------------------------------------------
    // Dialogs
    // -----------------------------------------------------------------------

    private void showHelpDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText("Lollypop — Quick Help");
        alert.setContentText(
            "• Subscribers tab: Add, edit, or delete subscribers.\n" +
            "• Sessions tab: Start and view usage sessions for a subscriber.\n" +
            "• Invoices tab: Generate invoices; view and print them.\n\n" +
            "For detailed documentation click the 'Documentation' button.\n\n" +
            "If anything goes wrong, the application auto-recovers from a\n" +
            "safety backup on next startup."
        );
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css") != null
            ? getClass().getResource("/styles.css").toExternalForm() : "");
        alert.showAndWait();
    }

    private void showDocumentationDialog() {
        Stage docStage = new Stage();
        docStage.setTitle("Lollypop — User Documentation");
        docStage.initOwner(stage);

        TextArea doc = new TextArea(Documentation.getText());
        doc.setEditable(false);
        doc.setWrapText(true);
        doc.setStyle("-fx-font-family: monospace; -fx-font-size: 13px;");

        ScrollPane scroll = new ScrollPane(doc);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);

        Scene s = new Scene(scroll, 720, 600);
        docStage.setScene(s);
        docStage.show();
    }

    // -----------------------------------------------------------------------
    // Styles
    // -----------------------------------------------------------------------

    private void applyGlobalStyles(Scene scene) {
        scene.getStylesheets().clear();
        String css = """
            .root {
                -fx-background-color: #F0F2F7;
                -fx-font-family: "Segoe UI", "Helvetica Neue", Arial, sans-serif;
                -fx-font-size: 14px;
            }
            /* ── Sidebar ─────────────────────────────────────── */
            .sidebar {
                -fx-background-color: #1E2235;
                -fx-border-color: transparent;
                -fx-border-width: 0;
            }
            .logo-area {
                -fx-padding: 22 16 16 16;
                -fx-spacing: 4;
            }
            .logo-placeholder-icon {
                -fx-font-size: 30px;
                -fx-text-fill: #6C7FF0;
            }
            .logo-text {
                -fx-font-size: 16px;
                -fx-font-weight: bold;
                -fx-text-fill: #FFFFFF;
            }
            .logo-subtext {
                -fx-font-size: 11px;
                -fx-text-fill: #7A85A3;
            }
            .sidebar-sep {
                -fx-background-color: #2D3250;
                -fx-opacity: 1;
            }
            .nav-btn {
                -fx-background-color: transparent;
                -fx-text-fill: #A0AABF;
                -fx-padding: 12 16 12 20;
                -fx-font-size: 14px;
                -fx-cursor: hand;
                -fx-border-width: 0;
                -fx-background-radius: 0;
                -fx-alignment: CENTER_LEFT;
            }
            .nav-btn:hover {
                -fx-background-color: #252B42;
                -fx-text-fill: #FFFFFF;
            }
            .nav-btn-active {
                -fx-background-color: #2D3562;
                -fx-text-fill: #7B93FF;
                -fx-font-weight: bold;
                -fx-border-color: #5C78FF;
                -fx-border-width: 0 0 0 3;
            }
            .util-btn {
                -fx-background-color: transparent;
                -fx-text-fill: #6A748A;
                -fx-padding: 9 12 9 16;
                -fx-font-size: 13px;
                -fx-cursor: hand;
                -fx-border-width: 0;
                -fx-background-radius: 6;
                -fx-alignment: CENTER_LEFT;
            }
            .util-btn:hover {
                -fx-background-color: #252B42;
                -fx-text-fill: #A0AABF;
            }
            /* ── Content area ────────────────────────────────── */
            .content-pane {
                -fx-background-color: #F0F2F7;
                -fx-padding: 0 28 24 28;
            }
            .page-title {
                -fx-font-size: 24px;
                -fx-font-weight: bold;
                -fx-text-fill: #1A1D2E;
            }
            .page-subtitle {
                -fx-font-size: 13px;
                -fx-text-fill: #8A93A8;
            }
            /* ── Status bar (top of content) ─────────────────── */
            .status-bar {
                -fx-background-color: #FFFFFF;
                -fx-border-color: #DDE1EA;
                -fx-border-width: 0 0 1 0;
                -fx-padding: 8 28 8 28;
            }
            .status-ok    { -fx-text-fill: #2B9348; -fx-font-size: 13px; -fx-font-weight: bold; }
            .status-error { -fx-text-fill: #D62828; -fx-font-size: 13px; -fx-font-weight: bold; }
            .status-info  { -fx-text-fill: #5C6B8A; -fx-font-size: 13px; }
            /* ── Cards ───────────────────────────────────────── */
            .card {
                -fx-background-color: #FFFFFF;
                -fx-background-radius: 12;
                -fx-border-color: #E2E6EF;
                -fx-border-radius: 12;
                -fx-border-width: 1;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 3);
                -fx-padding: 20 24 20 24;
            }
            .card-title {
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-text-fill: #2E3350;
            }
            /* ── Buttons ─────────────────────────────────────── */
            .btn-primary {
                -fx-background-color: #4361EE;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-background-radius: 8;
                -fx-padding: 10 22 10 22;
                -fx-cursor: hand;
                -fx-border-width: 0;
                -fx-font-size: 14px;
            }
            .btn-primary:hover { -fx-background-color: #3451D8; }
            .btn-danger {
                -fx-background-color: #E63946;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-background-radius: 8;
                -fx-padding: 10 22 10 22;
                -fx-cursor: hand;
                -fx-border-width: 0;
                -fx-font-size: 14px;
            }
            .btn-danger:hover { -fx-background-color: #C1121F; }
            .btn-secondary {
                -fx-background-color: #EDF0F7;
                -fx-text-fill: #3A3F5C;
                -fx-background-radius: 8;
                -fx-padding: 10 22 10 22;
                -fx-cursor: hand;
                -fx-border-width: 0;
                -fx-font-size: 14px;
            }
            .btn-secondary:hover { -fx-background-color: #D8DCE8; }
            .btn-success {
                -fx-background-color: #2DC653;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-background-radius: 8;
                -fx-padding: 10 22 10 22;
                -fx-cursor: hand;
                -fx-border-width: 0;
                -fx-font-size: 14px;
            }
            .btn-success:hover { -fx-background-color: #22A244; }
            /* ── Form fields ─────────────────────────────────── */
            .field-label {
                -fx-font-size: 12px;
                -fx-font-weight: bold;
                -fx-text-fill: #5C6B8A;
                -fx-padding: 0 0 3 0;
            }
            .text-field {
                -fx-background-color: #F7F9FC;
                -fx-border-color: #CDD3DF;
                -fx-border-radius: 7;
                -fx-background-radius: 7;
                -fx-padding: 9 13 9 13;
                -fx-font-size: 14px;
            }
            .text-field:focused {
                -fx-border-color: #4361EE;
                -fx-background-color: #FFFFFF;
            }
            /* ── ComboBox — outer container only ─────────────── */
            .combo-box {
                -fx-background-color: #F7F9FC;
                -fx-border-color: #CDD3DF;
                -fx-border-radius: 7;
                -fx-background-radius: 7;
                -fx-padding: 0;
                -fx-font-size: 14px;
            }
            .combo-box:focused, .combo-box:showing {
                -fx-border-color: #4361EE;
                -fx-background-color: #FFFFFF;
            }
            /* Inner text-field of editable ComboBox — no extra border/bg */
            .combo-box .text-field {
                -fx-background-color: transparent;
                -fx-border-color: transparent;
                -fx-border-width: 0;
                -fx-background-radius: 7;
                -fx-padding: 9 13 9 13;
                -fx-font-size: 14px;
            }
            .combo-box .text-field:focused {
                -fx-background-color: transparent;
                -fx-border-color: transparent;
            }
            /* Arrow button area */
            .combo-box .arrow-button {
                -fx-background-color: transparent;
                -fx-padding: 0 8 0 0;
            }
            .combo-box .arrow-button .arrow {
                -fx-background-color: #8A93A8;
            }
            /* Non-editable combo selected label */
            .combo-box .list-cell {
                -fx-text-fill: #1A1D2E;
                -fx-background-color: transparent;
                -fx-font-size: 14px;
                -fx-padding: 9 13 9 13;
            }
            /* Popup list */
            .combo-box-popup .list-view {
                -fx-background-color: #FFFFFF;
                -fx-border-color: #CDD3DF;
                -fx-border-radius: 7;
            }
            .combo-box-popup .list-view .list-cell {
                -fx-font-size: 14px;
                -fx-text-fill: #1A1D2E;
                -fx-background-color: transparent;
            }
            .combo-box-popup .list-view .list-cell:hover {
                -fx-background-color: #EEF2FF;
                -fx-text-fill: #4361EE;
            }
            .combo-box-popup .list-view .list-cell:selected {
                -fx-background-color: #4361EE;
                -fx-text-fill: #FFFFFF;
            }
            /* ── Table ───────────────────────────────────────── */
            .table-view {
                -fx-background-color: #FFFFFF;
                -fx-border-color: #E2E6EF;
                -fx-border-radius: 8;
                -fx-background-radius: 8;
                -fx-font-size: 14px;
            }
            .table-view .column-header-background {
                -fx-background-color: #F5F7FC;
            }
            .table-view .column-header {
                -fx-background-color: transparent;
                -fx-border-color: #E2E6EF;
                -fx-padding: 11 12 11 12;
            }
            .table-view .column-header .label {
                -fx-text-fill: #5C6B8A;
                -fx-font-weight: bold;
                -fx-font-size: 12px;
            }
            .table-row-cell {
                -fx-background-color: #FFFFFF;
                -fx-border-color: transparent transparent #F0F2F7 transparent;
                -fx-border-width: 0 0 1 0;
                -fx-text-fill: #1A1D2E;
                -fx-font-size: 14px;
            }
            .table-row-cell:odd {
                -fx-background-color: #FAFBFE;
            }
            .table-row-cell:selected {
                -fx-background-color: #4361EE;
            }
            .table-row-cell:selected .table-cell {
                -fx-text-fill: #FFFFFF;
            }
            .table-row-cell:focused:selected {
                -fx-background-color: #3451D8;
            }
            .table-row-cell:hover:!selected {
                -fx-background-color: #EEF2FF;
            }
            .table-cell {
                -fx-text-fill: #1A1D2E;
                -fx-padding: 8 12 8 12;
            }
            /* ── Subscriber search dropdown ───────────────────── */
            .search-dropdown {
                -fx-background-color: #FFFFFF;
                -fx-border-color: #4361EE;
                -fx-border-radius: 0 0 8 8;
                -fx-border-width: 0 1 1 1;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 12, 0, 0, 4);
            }
            .search-dropdown-item {
                -fx-background-color: transparent;
                -fx-text-fill: #1A1D2E;
                -fx-padding: 9 14 9 14;
                -fx-font-size: 13px;
                -fx-cursor: hand;
                -fx-alignment: CENTER_LEFT;
                -fx-border-width: 0;
                -fx-background-radius: 0;
            }
            .search-dropdown-item:hover {
                -fx-background-color: #EEF2FF;
                -fx-text-fill: #4361EE;
            }
            /* ── Invoice area ────────────────────────────────── */
            .invoice-box {
                -fx-background-color: #FAFBFE;
                -fx-border-color: #DDE1EA;
                -fx-border-radius: 8;
                -fx-background-radius: 8;
                -fx-font-family: "Courier New", monospace;
                -fx-font-size: 13px;
                -fx-padding: 16;
                -fx-text-fill: #1A1D2E;
            }
            /* ── ScrollPane ──────────────────────────────────── */
            .scroll-pane {
                -fx-background-color: transparent;
            }
            .scroll-pane > .viewport {
                -fx-background-color: transparent;
            }
            """;
        scene.setUserData(css);

        // We use a data-URI-like approach: write to temp file
        try {
            java.nio.file.Path tmp = java.nio.file.Files.createTempFile("lollypop_style", ".css");
            java.nio.file.Files.writeString(tmp, css);
            tmp.toFile().deleteOnExit();
            scene.getStylesheets().add(tmp.toUri().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
