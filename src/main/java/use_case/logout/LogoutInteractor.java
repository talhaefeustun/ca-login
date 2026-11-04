package use_case.logout;

/**
 * The Logout Interactor.
 */
public class LogoutInteractor implements LogoutInputBoundary {
    private final LogoutUserDataAccessInterface userDataAccessObject;
    private final LogoutOutputBoundary logoutPresenter;

    public LogoutInteractor(LogoutUserDataAccessInterface userDataAccessInterface,
                            LogoutOutputBoundary logoutOutputBoundary) {
        this.userDataAccessObject = userDataAccessInterface;
        this.logoutPresenter = logoutOutputBoundary;
    }



        this.userDataAccessObject = userDataAccessInterface;
        this.logoutPresenter = logoutOutputBoundary;
    }
    @Override
    public void execute() {
        final String username = userDataAccessObject.getCurrentUsername();
        // * set the current username to null in the DAO
        this.userDataAccessObject.setCurrentUsername(null);
        // * instantiate the `LogoutOutputData`, which needs to contain the username
        LogoutOutputData Logout = new LogoutOutputData(username);
        // * tell the presenter to prepare a success view.
        logoutPresenter.prepareSuccessView(Logout);
        userDataAccessObject.setCurrentUsername(null);

        final LogoutOutputData logoutOutputData = new LogoutOutputData(username);
        logoutPresenter.prepareSuccessView(logoutOutputData);
    }
}

