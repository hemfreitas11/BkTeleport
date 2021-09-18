package me.bkrmt.bkteleport.teleportable;

public class PagedOptions {
    private boolean editMode;
    private boolean isSpy;

    public PagedOptions() {
        this.editMode = false;
        this.isSpy = false;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public boolean isSpy() {
        return isSpy;
    }

    public PagedOptions setEditMode(boolean editMode) {
        this.editMode = editMode;
        return this;
    }

    public PagedOptions setSpy(boolean isSpy) {
        this.isSpy = isSpy;
        return this;
    }
}
