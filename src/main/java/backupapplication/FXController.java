package backupapplication;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


public class FXController implements Observer{

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
    private ProgressBar progressBar;

    @FXML
    private Text progressText = new Text();

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
    void startButtonPressed() {
        startBackup();

    }

    @FXML
    void cancelButtonPressed() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        //if (task != null && task.isRunning()) {
           /* task.cancel(true);
            if (task == null || task.isCancelled()) {
                stage.close();
            } else {
                cancelButtonPressed();
            }
        } else */ stage.close();
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
                sourceDirectoryText.setText(file.toString());
            } else {
                backUpApplication.setTargetRootFile(file);
                targetDirectoryText.setText(file.toString());
            }

        }
        checkIfBackupPossible();
    }

    private void startBackup() {
        backUpApplication.setObserver(this);
        backUpApplication.setProgressSize(4096);
        backUpApplication.setSourceDirectorySize(backUpApplication.getDirectorySizeCalculator().calculateSize(
                backUpApplication.getSourceRootFile().toPath(), backUpApplication.getDirectorySizeCalculator()));
        switch (backupMode) {
            case NEW -> {
                TextInputDialog inputDialog = new TextInputDialog("Backup-" + ZonedDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-hh-mm")));
                inputDialog.setHeaderText("Enter the name of the new directory.");
                inputDialog.showAndWait();
                String newDirectoryName = inputDialog.getResult();
                if (newDirectoryName != null) {
                    startWorkerThread(newDirectoryName);
                }
            } case CONSECUTIVE -> startWorkerThread(null);
            case UPDATING -> {
                if (showUpdatingModeWarning()) {
                    startWorkerThread(null);
                }
            }
        }
    }

    public void checkIfBackupPossible() {
        startButton.setDisable(!(this.backupMode != BackupMode.NONE && backUpApplication.getSourceRootFile() != null
                && backUpApplication.getTargetRootFile() != null));
    }

    private boolean showUpdatingModeWarning() {
        String warningMessage = """
                        This will delete all files in the target directory,
                        which are not present in the source directory.

                        If there are any files in the target directory which
                        should not be deleted, safe them somewhere else.

                        Are you sure you want to continue?
                        """;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, warningMessage, ButtonType.OK, ButtonType.CANCEL);

        alert.showAndWait();
        return alert.getResult() == ButtonType.OK;
    }

    @Override
    public void updateProgressBarAndText(BackupApplication backupApplication) {
        double progress = (((double) backupApplication.getProgressSize()
                / (double) backupApplication.getSourceDirectorySize()));
        progressBar.setProgress(progress);
        progressText.setText(((int)(progress*100)) + "%");
    }

    private void startWorkerThread(String newDirectoryName) {
       Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                System.out.println("Starting backup in '" + backupMode + "' mode.");
                switch (backupMode) {
                    case NEW -> backUpApplication.newBackup(newDirectoryName);
                    case CONSECUTIVE -> backUpApplication.consecutiveBackup();
                    case UPDATING -> backUpApplication.updatedBackup();
                }
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                System.out.println("Backup done.");
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION, null, ButtonType.OK);
                successAlert.setHeaderText("Backup done!");
                successAlert.showAndWait();
                if (successAlert.getResult() == ButtonType.OK) {
                    progressBar.setProgress(0);
                    progressText.setText("0%");
                }
            }
        };
        new Thread(task).start();
    }
}

