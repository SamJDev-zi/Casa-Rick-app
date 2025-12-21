package com.casarick.app.util;

import com.casarick.app.model.User;
import com.casarick.app.model.Branch;

public final class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();
    private User loggedInUser;
    private Branch currentBranch;

    private SessionManager() {
        // Inicializar con null
        this.loggedInUser = null;
        this.currentBranch = null;
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }
    public void startSession(User user, Branch branch) {
        this.loggedInUser = user;
        this.currentBranch = branch;
    }

    public void clearSession() {
        this.loggedInUser = null;
        this.currentBranch = null;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public Branch getCurrentBranch() {
        return currentBranch;
    }

    // Métodos de utilidad para verificar el estado
    public boolean isUserLoggedIn() {
        return this.loggedInUser != null;
    }
}