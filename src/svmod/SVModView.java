package svmod;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class SVModView {

    private static SVModView instance = null;

    private final int PADDING = 10;
    private final int BUTTON_WIDTH = 150;

    private final ModManager modManager;
    private final FileIdentifier fileIdentifier;
    private final StackPane view;
    private Stage window = null;

    VBox mainMenu;
    GridPane modPanel;

    TextArea logger;

    protected SVModView() {
        modManager = ModManager.getInstance();
        fileIdentifier = FileIdentifier.getInstance();
        view = new StackPane();
        createMainMenu();
        showMainMenu();
    }

    public static SVModView getInstance() {
        if (instance == null) {
            instance = new SVModView();
        }
        return instance;
    }

    public void showMainMenu() {
        view.getChildren().clear();
        view.getChildren().add(mainMenu);
        ((ListView) mainMenu.getChildren().get(2)).refresh();
    }

    public void showModPanel(Mod item) {
        view.getChildren().clear();
        createModPanel(item);
        view.getChildren().add(modPanel);
    }

    private void createMainMenu() {

        // GUI
        mainMenu = new VBox(PADDING);
        mainMenu.setPadding(new Insets(PADDING, PADDING, PADDING, PADDING));
        HBox modFolderGUI = new HBox(PADDING);
        HBox backupFolderGUI = new HBox(PADDING);

        TextField modFolderText = new TextField(modManager.getModDirectory().toString());
        modFolderText.setDisable(true);
        HBox.setHgrow(modFolderText, Priority.ALWAYS);
        Button changeModFolderBtn = new Button("Change Mod Folder...");
        changeModFolderBtn.setMinWidth(BUTTON_WIDTH);
        modFolderGUI.getChildren().addAll(modFolderText, changeModFolderBtn);

        TextField backupFolderText = new TextField(modManager.getBackupDirectory().toString());
        backupFolderText.setDisable(true);
        HBox.setHgrow(backupFolderText, Priority.ALWAYS);
        Button changeBackupFolderBtn = new Button("Change Backup Folder...");
        changeBackupFolderBtn.setMinWidth(BUTTON_WIDTH);
        backupFolderGUI.getChildren().addAll(backupFolderText, changeBackupFolderBtn);

        ListView<Mod> modList = new ListView<Mod>(modManager.getModList());

        logger = new TextArea();
        logger.setEditable(false);
        logger.setMaxHeight(logger.getHeight());
        logger.setText(".");
        HBox.setHgrow(logger, Priority.ALWAYS);

        HBox installOptions = new HBox(PADDING);
        Button installBtn = new Button("Install");
        Button uninstallBtn = new Button("Uninstall");
        Button partialBtn = new Button("Partial...");
        installBtn.setMinWidth(BUTTON_WIDTH);
        uninstallBtn.setMinWidth(BUTTON_WIDTH);
        partialBtn.setMinWidth(BUTTON_WIDTH);
        installOptions.setAlignment(Pos.BOTTOM_RIGHT);
        installOptions.getChildren().addAll(installBtn, uninstallBtn, partialBtn);

        mainMenu.getChildren().addAll(modFolderGUI, backupFolderGUI, modList, logger, installOptions);

        // Functionality
        changeModFolderBtn.setOnAction((ActionEvent event) -> {
            selectDirectory(modFolderText, "Change Mod Folder", modManager.getModDirectory(), modManager::setModDirectory);
            addToLogger(modManager.loadMods());
        });
        changeBackupFolderBtn.setOnAction((ActionEvent event) -> {
            selectDirectory(backupFolderText, "Change Backup Folder", modManager.getBackupDirectory(), modManager::setBackupDirectory);
        });
        modList.setCellFactory(param -> new ListCell<Mod>() {
            @Override
            protected void updateItem(Mod item, boolean empty) {
                super.updateItem(item, empty);
                Rectangle rect = new Rectangle(5, 10);
                if (empty || item == null) {
                    //setText("BAD MOD");
                    return;
                }
                if (!item.isValid()) {
                    rect.setFill(Color.RED);
                } else if (item.getInstallStatus() == 0) {
                    rect.setFill(Color.WHITE);
                } else if (item.getInstallStatus() == 1) {
                    rect.setFill(Color.YELLOW);
                } else if (item.getInstallStatus() == 2) {
                    rect.setFill(Color.GREEN);
                } else {
                    rect.setFill(Color.WHITE);
                }
                setGraphic(rect);
                setText(item.toString());
                setTextFill(Color.BLACK);
            }
        });
        installBtn.setOnAction((ActionEvent event) -> {
            Mod item = modList.getSelectionModel().getSelectedItem();
            if (item == null) {
                return;
            }
            addToLogger(modManager.installMod(item));
            modList.refresh();
        });
        uninstallBtn.setOnAction((ActionEvent event) -> {
            Mod item = modList.getSelectionModel().getSelectedItem();
            if (item == null) {
                return;
            }
            addToLogger(modManager.uninstallMod(item));
            modList.refresh();
        });
        partialBtn.setOnAction((ActionEvent event) -> {
            Mod item = modList.getSelectionModel().getSelectedItem();
            if (item == null) {
                return;
            }
            showModPanel(item);
        });
        logger.textProperty().addListener((ObservableValue<?> observable, Object oldValue, Object newValue) -> {
            logger.setScrollTop(Double.MAX_VALUE);
        });
    }

    private void createModPanel(Mod mod) {

        // GUI
        modPanel = new GridPane();
        modPanel.setHgap(PADDING);
        modPanel.setPadding(new Insets(PADDING, PADDING, PADDING, PADDING));

        VBox modHolder = new VBox();
        Label name = new Label(mod.getName());
        Label version = new Label("v" + mod.getVersion());
        Label authors = new Label(mod.getAuthors().toString());

        ObservableList<Path> oListModFiles = FXCollections.observableArrayList(mod.getModFiles());
        ListView<Path> filesList = new ListView<>(oListModFiles);
        filesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        VBox buttonHolder = new VBox();
        Button identifyBtn = new Button("Load identifier file...");
        identifyBtn.setMinWidth(BUTTON_WIDTH);
        Button installBtn = new Button("Install selected");
        installBtn.setMinWidth(BUTTON_WIDTH);
        Button uninstallBtn = new Button("Uninstall selected");
        uninstallBtn.setMinWidth(BUTTON_WIDTH);
        Button backBtn = new Button("Cancel");
        backBtn.setMinWidth(BUTTON_WIDTH);
        buttonHolder.getChildren().addAll(identifyBtn, installBtn, uninstallBtn, backBtn);
        buttonHolder.setAlignment(Pos.BOTTOM_RIGHT);

        modHolder.getChildren().addAll(name, version, authors, filesList, buttonHolder);

        VBox previewHolder = new VBox();
        Button externalWindowBtn = new Button("Open preview...");
        externalWindowBtn.setMinWidth(BUTTON_WIDTH);
        ScrollPane scrollPreview = new ScrollPane();
        StackPane previewPane = new StackPane();
        previewPane.setPadding(new Insets(PADDING, PADDING, PADDING, PADDING));
        MediaView previewMediaPane = new MediaView();
        ImageView previewImagePane = new ImageView();
        scrollPreview.setContent(previewPane);

        previewPane.getChildren().addAll(previewMediaPane, previewImagePane);
        previewPane.setAlignment(Pos.CENTER);
        previewHolder.setAlignment(Pos.CENTER_RIGHT);

        previewHolder.getChildren().addAll(scrollPreview, externalWindowBtn);

        VBox.setVgrow(scrollPreview, Priority.ALWAYS);
        HBox.setHgrow(scrollPreview, Priority.ALWAYS);

        modPanel.add(modHolder, 0, 0);
        modPanel.add(previewHolder, 1, 0);
        modPanel.getColumnConstraints().add(new ColumnConstraints(BUTTON_WIDTH * 4 / 3.0));

        // Functionality
        filesList.setCellFactory(param -> new ListCell<Path>() {
            @Override
            protected void updateItem(Path item, boolean empty) {
                super.updateItem(item, empty);
                Rectangle rect = new Rectangle(5, 10);
                if (empty || item == null) {
                    return;
                }
                Path svFile = Paths.get(SVGameFiles.getInstance().getGameFileLocation().toString(), item.toString());
                Path modFile = Paths.get(mod.getFolderPath().toString(), item.toString());
                Checksum svCheck = new Checksum(svFile);
                Checksum modCheck = new Checksum(modFile);
                if (svCheck.getFile() == null) {
                    rect.setFill(Color.RED);
                } else if (svCheck.equals(modCheck)) {
                    rect.setFill(Color.GREEN);
                } else {
                    rect.setFill(Color.WHITE);
                }
                setGraphic(rect);
                String identify = fileIdentifier.identify(item.getFileName());
                if (identify == null) {
                    identify = "";
                } else {
                    identify = " [" + identify.trim() + "]";
                }
                setText(item.toString() + identify);
                setTextFill(Color.BLACK);
            }
        });
        filesList.setOnMouseClicked((MouseEvent event) -> {
            previewMediaPane.setMediaPlayer(null);
            previewImagePane.setImage(null);
            Path preview = mod.getPreviewFiles().get(filesList.getSelectionModel().getSelectedItem());
            if (preview == null) {
                return;
            }
            Path previewPath = Paths.get(mod.getFolderPath().toString(), preview.toString());
            Media m = null;
            Image i = null;
            try {
                m = new Media(previewPath.toUri().toString());
            } catch (Exception e) {
                //quality coding here
            }
            try {
                i = new Image(previewPath.toUri().toString());
            } catch (Exception e) {
            }
            if (m != null) {
                MediaPlayer previewPlayer = new MediaPlayer(m);
                previewMediaPane.setMediaPlayer(previewPlayer);
                previewPlayer.play();
            }
            if (i != null) {
                previewImagePane.setImage(i);
            }

        });
        identifyBtn.setOnAction((ActionEvent event) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(Paths.get(".").toFile());
            fileChooser.setTitle("Open Identifier File");
            Path p = null;
            try {
                p = fileChooser.showOpenDialog(window).toPath().normalize();
            } catch (Exception e) {
                return;
            }
            fileIdentifier.loadIdentifierFile(p);
            filesList.refresh();
        });
        installBtn.setOnAction((ActionEvent event) -> {
            SVGameFiles svgf = SVGameFiles.getInstance();
            ObservableList<Path> selectedItems = filesList.getSelectionModel().getSelectedItems();
            for (Path filepath : selectedItems) {
                addToLogger(modManager.installFileFromMod(filepath, mod));
            }
            showMainMenu();
        });
        uninstallBtn.setOnAction((ActionEvent event) -> {
            SVGameFiles svgf = SVGameFiles.getInstance();
            ObservableList<Path> selectedItems = filesList.getSelectionModel().getSelectedItems();
            for (Path filepath : selectedItems) {
                addToLogger(modManager.uninstallFileFromMod(filepath, mod));
            }
            showMainMenu();
        });
        backBtn.setOnAction((ActionEvent event) -> {
            showMainMenu();
        });
        externalWindowBtn.setOnAction((ActionEvent event) -> {
            Media m = null;
            Image i = null;
            try {
                m = previewMediaPane.getMediaPlayer().getMedia();
            } catch (Exception e) {      
            }
            try {
                i = previewImagePane.getImage();
            } catch (Exception e) {      
            }
            if (m == null && i == null) {
                return;
            }
            Stage stage = new Stage();
            stage.setTitle("Preview");
            Scene scene;
            StackPane sp = new StackPane();
            if (m != null) {
                MediaPlayer mp = new MediaPlayer(m);
                MediaView mv = new MediaView(mp);
                mv.setMediaPlayer(mp);
                sp.getChildren().add(mv);
                mp.play();
            }
            if (i != null) {
                ImageView iv = new ImageView();
                iv.setImage(i);
                sp.getChildren().add(iv);
            }
            scene = new Scene(sp, sp.getMinWidth(), sp.getMinHeight());
            stage.setScene(scene);
            stage.show();
        });
    }

    private void selectDirectory(TextField text, String title, Path initial, Consumer<String> modManagerUpdate) {
        if (Files.notExists(initial)) {
            initial = Paths.get(".");
        }
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(title);
        chooser.setInitialDirectory(initial.toFile());
        String newDirectory;
        try {
            newDirectory = chooser.showDialog(window).toPath().normalize().toString();
        } catch (Exception e) {
            return;
        }
        text.setText(newDirectory);
        modManagerUpdate.accept(newDirectory);
    }

    private void addToLogger(String text) {
        logger.appendText("\n" + text);
    }

    public void setWindow(Stage s) {
        window = s;
    }

    public Parent asParent() {
        return view;
    }

}
