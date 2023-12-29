package backupapplication;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;


public class FXController{

    private final BackupApplication backUpApplication = new BackupApplication(null, null);
    private final String newModeString = """
                    New Backup:

                    In this mode, a completely new backup of the source
                    directory will be created in the target location.
                    
                    You can choose a name for the new directory
                    after pressing 'start'.""";
    private BackupMode backupMode = BackupMode.NEW;



    @FXML
    private Button sourceDirectoryChooseButton;

    @FXML
    private TextArea sourceDirectoryText = new TextArea();

    @FXML
    private Button targetDirectoryChooseButton;

    @FXML
    private TextArea targetDirectoryText = new TextArea();

    @FXML
    public RadioButton newRadioButton = new RadioButton();

    @FXML
    public RadioButton consecutiveRadioButton = new RadioButton();

    @FXML
    private CheckBox deleteCheckBox = new CheckBox();

    @FXML
    private TextArea backupModeInfoText = new TextArea(newModeString);

    @FXML
    private Button startButton = new Button();

    @FXML
    private Button cancelButton = new Button();


    @FXML
    void initialize() {
        backupModeInfoText.setStyle("-fx-control-inner-background: #e6e6e6 ;");
        targetDirectoryText.setStyle("-fx-control-inner-background: #e6e6e6 ;");
        sourceDirectoryText.setStyle("-fx-control-inner-background: #e6e6e6 ;");
    }

    @FXML
    void chooseSourceDirectoryPressed() {
        showDirectoryChooser(sourceDirectoryChooseButton);
    }

    @FXML
    void chooseTargetDirectoryPressed() {
        showDirectoryChooser(targetDirectoryChooseButton);
    }

    @FXML
    void newRadioPressed() {
        backupMode = BackupMode.NEW;
        backupModeInfoText.setText(newModeString);
        deleteCheckBox.setDisable(true);
        deleteCheckBox.setSelected(false);
        checkIfBackupPossible();
    }

    @FXML
    void consecutiveModeButtonPressed() {
        backupMode = BackupMode.CONSECUTIVE;
        backupModeInfoText.setText("""
                Consecutive Backup:

                In consecutive mode, all of those files in the source
                directory, which don't exist in the target location
                or were changed since the last backup will be copied.
                
                Additionally, You can choose to delete all of the files
                not existent in the source directory in the target
                directory by choosing 'delete option'""");
        deleteCheckBox.setDisable(false);
        checkIfBackupPossible();
    }

    @FXML
    void deleteOptionPressed() {
        if (deleteCheckBox.isSelected()) {
            backupMode = BackupMode.UPDATING;
        }
    }

    @FXML
    void startButtonPressed() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(FXApplication.class.getResource("progressWindow.fxml"));
        ((ProgressWindow)fxmlLoader.getController()).setBackupApplication(backUpApplication);
        Scene scene = new Scene(fxmlLoader.load());
        Stage progressStage = new Stage();
        progressStage.setScene(scene);
        Stage mainStage = (Stage) startButton.getScene().getWindow();
        progressStage.initOwner(mainStage);
        progressStage.initModality(Modality.APPLICATION_MODAL);
        progressStage.showAndWait();

        //ProgressWindow p = new ProgressWindow();
        //p.startBackup(backUpApplication);
        progressStage.centerOnScreen();
        //startBackup();
    }

    @FXML
    void cancelButtonPressed() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        /*if (task != null && task.isRunning()) {
            String warningMessage = """
                        There is a backup in progress.
                        Are you sure you want to cancel the process?
                        It may produce defective files.
                        """;
            if (showWarningDialog(warningMessage)) {
                stage.close();
            }
        } else {
            stage.close();
        }

         */
        stage.close();
    }

    private void showDirectoryChooser(Button button) {
        DirectoryChooser fileChooser = new DirectoryChooser();
        File file;
        fileChooser.setTitle("choose " + (button.getId().contains("source") ? "source" : "target") + " directory");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        file = (fileChooser.showDialog((button.getScene().getWindow())));
        if (file != null) {
            if (button.getId().contains("source")) {
                backUpApplication.setSourceRootFile(file);
                sourceDirectoryText.setText(printFilePath(file));
            } else {
                backUpApplication.setTargetRootFile(file);
                targetDirectoryText.setText(printFilePath(file));
            }
        }
        checkIfBackupPossible();
    }

    public void checkIfBackupPossible() {
        startButton.setDisable(!(this.backupMode != BackupMode.NONE && backUpApplication.getSourceRootFile() != null
                && backUpApplication.getTargetRootFile() != null));
    }

    private static String printFilePath(File file) {
        String[] splitPath = file.toString().split("(?<=/)");
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder eachLine = new StringBuilder();
        int textAreaWidth = 45;
        int i = 0;
        while (i < splitPath.length) {
            while (i < splitPath.length && (splitPath[i].length() > textAreaWidth
                    || eachLine.length() + splitPath[i].length() < textAreaWidth)) {
                eachLine.append(splitPath[i]);
                i++;
            }
            stringBuilder.append(eachLine).append("\n");
            eachLine.setLength(0);
        }
        return stringBuilder.toString();
    }
}

