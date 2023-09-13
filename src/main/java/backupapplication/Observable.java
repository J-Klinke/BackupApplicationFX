package backupapplication;

public abstract class Observable {
    private Observer observer;

    protected void notifyObserver(BackupApplication backupApplication) {
        observer.updateProgressBarAndText(backupApplication);
    }

    public void setObserver(Observer observer) {
        this.observer = observer;
    }
}
