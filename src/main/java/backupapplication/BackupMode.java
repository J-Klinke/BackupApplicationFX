package backupapplication;

public enum BackupMode {
    NONE("Choose a backup mode"),
    CONSECUTIVE("Consecutive"),
    NEW("New"),
    UPDATING("Updating");

    public final String label;

    @Override
    public String toString() {
        return this.label;
    }

    BackupMode(String label) {
        this.label = label;
    }
}
