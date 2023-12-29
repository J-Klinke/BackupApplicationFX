package backupapplication;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextInputDialog;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ProgressWindow implements Observer{
    Task<Void> task;
    @FXML
    private ProgressBar progressBar;

    @FXML
    private Text progressText = new Text();

    @FXML
    private Button cancelButton;

    private BackupApplication backupApplication;


    @FXML
    void cancelButtonPressed() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();

    }
    void setBackupApplication(BackupApplication backupApplication) {
        this.backupApplication = backupApplication;
    }

    @FXML
    void initialize() {
        startBackup();
    }
    public void startBackup() {
        backupApplication.setObserver(this);
        backupApplication.setProgressSize(4096);
        backupApplication.setSourceDirectorySize(backupApplication.getDirectorySizeCalculator().calculateSize(
                backupApplication.getSourceRootFile().toPath(), backupApplication.getDirectorySizeCalculator()));
        switch (backupApplication.getBackupMode()) {
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
                String warningMessage = """
                        This will delete all files in the target directory,
                        which are not present in the source directory.

                        If there are any files in the target directory which
                        should not be deleted, safe them somewhere else.

                        Are you sure you want to continue?
                        """;
                if (showWarningDialog(warningMessage)) {
                    startWorkerThread(null);
                }
            }
        }
    }

    private void startWorkerThread(String newDirectoryName) {
        task = new Task<>() {
            @Override
            protected Void call() {
                //startButton.setDisable(true);
                BackupMode backupMode = backupApplication.getBackupMode();
                System.out.println("Starting backup in '" + backupMode + "' mode.");
                switch (backupMode) {
                    case NEW -> backupApplication.newBackup(newDirectoryName);
                    case CONSECUTIVE -> backupApplication.consecutiveBackup();
                    case UPDATING -> backupApplication.updatedBackup();
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
                    //checkIfBackupPossible();
                }
            }
        };
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void updateProgressBarAndText(BackupApplication backupApplication) {
        double progress = (((double) backupApplication.getProgressSize()
                / (double) backupApplication.getSourceDirectorySize()));
        progressBar.setProgress(progress);
        progressText.setText(((int)(progress*100)) + "%");
    }

    private boolean showWarningDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait();
        return alert.getResult() == ButtonType.OK;
    }
}
