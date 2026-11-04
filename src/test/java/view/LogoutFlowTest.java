package view;

import data_access.InMemoryUserDataAccessObject;
import entity.UserFactory;
import interface_adapter.ViewManagerModel;
import interface_adapter.logged_in.LoggedInViewModel;
import interface_adapter.login.LoginController;
import interface_adapter.login.LoginPresenter;
import interface_adapter.login.LoginViewModel;
import interface_adapter.logout.LogoutController;
import interface_adapter.logout.LogoutPresenter;
import org.junit.jupiter.api.Test;
import use_case.login.LoginInteractor;
import use_case.logout.LogoutInteractor;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class LogoutFlowTest {

    @Test
    void logoutReturnsToLoginAndAllowsLoginAgain() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            final InMemoryUserDataAccessObject users = new InMemoryUserDataAccessObject();
            users.save(new UserFactory().create("Paul", "password"));

            final LoginViewModel loginModel = new LoginViewModel();
            final LoggedInViewModel loggedInModel = new LoggedInViewModel();
            final ViewManagerModel managerModel = new ViewManagerModel();
            final LoginView loginView = new LoginView(loginModel);
            final LoggedInView loggedInView = new LoggedInView(loggedInModel);

            loginView.setLoginController(new LoginController(new LoginInteractor(users,
                    new LoginPresenter(managerModel, loggedInModel, loginModel))));
            loggedInView.setLogoutController(new LogoutController(new LogoutInteractor(users,
                    new LogoutPresenter(managerModel, loggedInModel, loginModel))));

            final CardLayout layout = new CardLayout();
            final JPanel screens = new JPanel(layout);
            screens.add(loginView, LoginViewModel.VIEW_NAME);
            screens.add(loggedInView, LoggedInViewModel.VIEW_NAME);
            new ViewManager(screens, layout, managerModel);
            managerModel.setState(LoginViewModel.VIEW_NAME);
            managerModel.notifyViewChanged();

            final JTextField username = findComponent(loginView, JTextField.class,
                    field -> !(field instanceof JPasswordField));
            final JPasswordField password = findComponent(loginView, JPasswordField.class, field -> true);
            final JButton logIn = findComponent(loginView, JButton.class,
                    button -> "log in".equals(button.getText()));
            final JButton logOut = findComponent(loggedInView, JButton.class,
                    button -> "Log Out".equals(button.getText()));
            final JTextField passwordDraft = findComponent(loggedInView, JTextField.class, field -> true);

            username.setText("Paul");
            password.setText("password");
            logIn.doClick();
            assertEquals("Paul", users.getCurrentUsername());
            assertTrue(loggedInView.isVisible());

            passwordDraft.setText("unfinished-password-change");
            logOut.doClick();

            assertNull(users.getCurrentUsername());
            assertTrue(users.existsByName("Paul"), "Logout must preserve the account.");
            assertTrue(loginView.isVisible());
            assertFalse(loggedInView.isVisible());
            assertEquals(LoginViewModel.VIEW_NAME, managerModel.getCurrentViewName());
            assertEquals("Paul", username.getText());
            assertEquals(0, password.getPassword().length);
            assertEquals("", loggedInModel.getState().getUsername());
            assertEquals("", loggedInModel.getState().getPassword());
            assertEquals("", passwordDraft.getText());

            // A fresh password is required after logout.
            logIn.doClick();
            assertNull(users.getCurrentUsername());
            assertNotNull(loginModel.getState().getLoginError());
            assertTrue(loginView.isVisible());

            password.setText("password");
            logIn.doClick();
            assertEquals("Paul", users.getCurrentUsername());
            assertTrue(loggedInView.isVisible());

            // The flow remains usable on the next logout too.
            logOut.doClick();
            assertNull(users.getCurrentUsername());
            assertTrue(loginView.isVisible());
            assertEquals("Paul", username.getText());
            assertEquals(0, password.getPassword().length);
            assertNull(loginModel.getState().getLoginError());
        });
    }

    private static <T extends Component> T findComponent(Container root, Class<T> type,
                                                         Predicate<T> matches) {
        for (Component component : root.getComponents()) {
            if (type.isInstance(component) && matches.test(type.cast(component))) {
                return type.cast(component);
            }
            if (component instanceof Container) {
                final T found = findComponent((Container) component, type, matches);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
