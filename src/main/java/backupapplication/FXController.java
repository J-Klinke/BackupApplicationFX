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

// TODO: canceln funktioniert nicht, wenn thread läuft
// TODO: progress funktiniert garnicht
// TODO: Exceptions wenn filechooser geschlossen wird

public class FXController implements Observer{

    private Task<Void> task = null;
    private final BackupApplication backUpApplication = new BackupApplication(null, null);
    private final String newModeString = """
                    New Backup:

                    In this mode, a completely new backup of the source
                    directory will be created in the target location.
                    
                    You can choose a name for the new directory
                    after pressing 'start'.""";
    private BackupMode backupMode = BackupMode.NEW;

    @FXML
    private TextArea backupModeInfoText = new TextArea(newModeString);

    @FXML
    private Button cancelButton = new Button();

    @FXML
    public RadioButton consecutiveRadioButton = new RadioButton();

    @FXML
    private Button targetDirectoryChooseButton;

    @FXML
    public RadioButton newRadioButton = new RadioButton();

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Button sourceDirectoryChooseButton;

    @FXML
    private TextArea sourceDirectoryText = new TextArea();

    @FXML
    private Text progressText = new Text();

    @FXML
    private Button startButton = new Button();

    @FXML
    private TextArea targetDirectoryText = new TextArea();

    @FXML
    private CheckBox deleteCheckBox = new CheckBox();

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

    @FXML
    void chooseSourceDirectoryPressed() {
        DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setTitle("choose source directory");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        backUpApplication.setSourceRootFile(fileChooser.showDialog
                ((sourceDirectoryChooseButton.getScene().getWindow())));
        sourceDirectoryText.setText(backUpApplication.getSourceRootFile().toString());
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
    void newRadioPressed() {
        backupMode = BackupMode.NEW;
        backupModeInfoText.setText(newModeString);
        deleteCheckBox.setDisable(true);
        deleteCheckBox.setSelected(false);
        checkIfBackupPossible();
    }

    @FXML
    void startButtonPressed() {
        startBackup();

    }

    @FXML
    void chooseTargetDirectoryPressed() {
        DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setTitle("choose target directory");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        backUpApplication.setTargetRootFile(fileChooser.showDialog
                ((targetDirectoryChooseButton.getScene().getWindow())));
        targetDirectoryText.setText(backUpApplication.getTargetRootFile().toString());
        checkIfBackupPossible();
    }

    public void checkIfBackupPossible() {
        startButton.setDisable(!(this.backupMode != BackupMode.NONE && backUpApplication.getSourceRootFile() != null
                && backUpApplication.getTargetRootFile() != null));
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
    public void update(BackupApplication backupApplication) {
        int progress = (int) (((double) backupApplication.getProgressSize()
                / (double) backupApplication.getSourceDirectorySize()));
        progressBar.setProgress(progress);
        progressText.setText(progress*100 + "%");
    }

    private void startWorkerThread(String newDirectoryName) {
        task = new Task<>() {
            @Override
            protected Void call() {
                System.out.println("starting backup in '" + backupMode + "' mode.");
                switch (backupMode) {
                    case NEW -> backUpApplication.newBackup(newDirectoryName);
                    case CONSECUTIVE -> backUpApplication.consecutiveBackup();
                    case UPDATING -> backUpApplication.updatedBackup();
                }

                while (isRunning()) {
                    updateProgressBarAndText();
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

    private void updateProgressBarAndText() {
        int progress = (int) (((double) backUpApplication.getProgressSize()
                / (double) backUpApplication.getSourceDirectorySize()));
        progressBar.setProgress(progress);
        progressText.setText(progress*100 + "%");
    }
}

