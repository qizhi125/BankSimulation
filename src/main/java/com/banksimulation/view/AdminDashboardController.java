package com.banksimulation.view;

import com.banksimulation.App;
import com.banksimulation.entity.Admin;
import com.banksimulation.entity.OperationLog;
import com.banksimulation.entity.TransactionRecord; // Import TransactionRecord
import com.banksimulation.entity.User;
import com.banksimulation.service.AdminService;
import com.banksimulation.service.LoggingService;
import com.banksimulation.service.UserService;
import com.banksimulation.util.PasswordHasher;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.layout.VBox; // Import VBox

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Administrator Dashboard Controller
 * Controller for the admin dashboard view.
 */
public class AdminDashboardController {

    @FXML private Label welcomeLabel;

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colUserId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colFirstName;
    @FXML private TableColumn<User, String> colLastName;
    @FXML private TableColumn<User, String> colAccountNumber;
    @FXML private TableColumn<User, Double> colBalance;
    @FXML private TableColumn<User, Boolean> colIsActive;
    @FXML private TableColumn<User, Boolean> colCanDeposit;
    @FXML private TableColumn<User, Boolean> colCanWithdraw;
    @FXML private Label userManagementMessageLabel;

    @FXML private TableView<OperationLog> logTable;
    @FXML private TableColumn<OperationLog, String> colLogTimestamp;
    @FXML private TableColumn<OperationLog, String> colActorUsername;
    @FXML private TableColumn<OperationLog, String> colActorType;
    @FXML private TableColumn<OperationLog, String> colAction;
    @FXML private TableColumn<OperationLog, String> colDetails;
    @FXML private Label logMessageLabel;

    // FXML elements for permission control
    @FXML private Button refreshUserListButton;
    @FXML private Button createUserButton;
    @FXML private Button toggleActiveButton;
    @FXML private Button modifyPermissionsButton;
    @FXML private Button deleteUserButton;
    @FXML private Button refreshLogsButton;
    @FXML private Button exportLogsButton;

    // New navigation buttons
    @FXML private javafx.scene.control.Button navUserManagementButton;
    @FXML private javafx.scene.control.Button navLogViewButton;

    // New panel containers
    @FXML private VBox userManagementPanel;
    @FXML private VBox logViewPanel;


    private final AdminService adminService;
    private final UserService userService;
    private final LoggingService loggingService;
    private final Stage primaryStage;
    private Admin loggedInAdmin; // Current logged-in administrator object (Admin instance)

    // Constructor, dependency injection via App class
    public AdminDashboardController(AdminService adminService, UserService userService,
                                    LoggingService loggingService, Stage primaryStage, Admin loggedInAdmin) {
        this.adminService = adminService;
        this.userService = userService;
        this.loggingService = loggingService;
        this.primaryStage = primaryStage;
        this.loggedInAdmin = loggedInAdmin; // Receive Admin object
    }

    @FXML
    public void initialize() {
        if (loggedInAdmin != null) {
            welcomeLabel.setText("欢迎，管理员 " + loggedInAdmin.getUsername() + "！");
            applyPermissionsToUI(); // Temporarily not implementing fine-grained permission control
            setupUserTable();
            setupLogTable();
            // Show user management panel by default
            showUserManagement(null);
        }
    }

    /**
     * Dynamically adjusts the visibility and availability of UI elements based on the current logged-in administrator's permissions.
     * Note: The current Admin entity does not have fine-grained permission fields, this is just a placeholder.
     * Actual permission control should be implemented in AdminService.
     */
    private void applyPermissionsToUI() {
        if (loggedInAdmin == null) {
            System.err.println("Error: loggedInAdmin is null in applyPermissionsToUI.");
            return;
        }

        // By default, all administrators have these permissions, unless corresponding boolean fields are added to the Admin entity
        // For example:
        // createUserButton.setManaged(loggedInAdmin.canCreateUsers());
        // createUserButton.setVisible(loggedInAdmin.canCreateUsers());

        // For simplicity, all administrator UI elements are currently visible, actual permission control is in the service layer
        createUserButton.setManaged(true);
        createUserButton.setVisible(true);
        toggleActiveButton.setManaged(true);
        toggleActiveButton.setVisible(true);
        modifyPermissionsButton.setManaged(true);
        modifyPermissionsButton.setVisible(true);
        deleteUserButton.setManaged(true);
        deleteUserButton.setVisible(true);
        refreshLogsButton.setManaged(true);
        refreshLogsButton.setVisible(true);
        exportLogsButton.setManaged(true);
        exportLogsButton.setVisible(true);
        logTable.setManaged(true);
        logTable.setVisible(true);

        navUserManagementButton.setManaged(true);
        navUserManagementButton.setVisible(true);
        navLogViewButton.setManaged(true);
        navLogViewButton.setVisible(true);
    }


    private void setupUserTable() {
        colUserId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUserId()));
        colUsername.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        colFirstName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstName()));
        colLastName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastName()));
        colAccountNumber.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAccountNumber()));
        colBalance.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getBalance()).asObject());
        colIsActive.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isActive()).asObject());
        colCanDeposit.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().canDeposit()).asObject());
        colCanWithdraw.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().canWithdraw()).asObject());
    }

    private void setupLogTable() {
        colLogTimestamp.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        colActorUsername.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getActorUsername()));
        colActorType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getActorType().name()));
        colAction.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAction()));
        colDetails.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDetails()));
    }

    @FXML
    private void refreshUserList(ActionEvent event) {
        List<User> users = adminService.getAllUsers(loggedInAdmin.getUsername());
        ObservableList<User> observableUsers = FXCollections.observableArrayList(users);
        userTable.setItems(observableUsers);
        userManagementMessageLabel.setText("");
    }

    @FXML
    private void handleCreateUser(ActionEvent event) {
        Alert createUserDialog = new Alert(Alert.AlertType.CONFIRMATION);
        createUserDialog.setTitle("创建新用户");
        createUserDialog.setHeaderText("请输入新用户详情");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField usernameField = new TextField();
        usernameField.setPromptText("用户名");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("密码");
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("名");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("姓");
        TextField accountNumberField = new TextField();
        accountNumberField.setPromptText("账号 (例如: 10位数字)");

        grid.add(new Label("用户名:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("密码:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("名:"), 0, 2);
        grid.add(firstNameField, 1, 2);
        grid.add(new Label("姓:"), 0, 3);
        grid.add(lastNameField, 1, 3);
        grid.add(new Label("账号:"), 0, 4);
        grid.add(accountNumberField, 1, 4);

        createUserDialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = createUserDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String accountNumber = accountNumberField.getText();

            if (username.isEmpty() || password.isEmpty() || accountNumber.isEmpty()) {
                userManagementMessageLabel.setText("创建用户失败：用户名、密码和账号不能为空。");
                userManagementMessageLabel.setTextFill(Color.RED);
                return;
            }
            if (accountNumber.length() != 10 || !accountNumber.matches("\\d+")) {
                userManagementMessageLabel.setText("创建用户失败：账号必须是10位数字。");
                userManagementMessageLabel.setTextFill(Color.RED);
                return;
            }

            String hashedPassword = PasswordHasher.hashPassword(password);
            User newUser = new User(username, hashedPassword, firstName, lastName, accountNumber);

            if (adminService.createUser(loggedInAdmin.getUsername(), newUser)) {
                userManagementMessageLabel.setText("用户 '" + username + "' 创建成功！");
                userManagementMessageLabel.setTextFill(Color.GREEN);
                refreshUserList(null); // Refresh user list
            } else {
                userManagementMessageLabel.setText("创建用户失败：用户名或账号可能已存在。");
                userManagementMessageLabel.setTextFill(Color.RED);
            }
        }
    }

    @FXML
    private void handleToggleActive(ActionEvent event) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            userManagementMessageLabel.setText("请选择一个用户。");
            userManagementMessageLabel.setTextFill(Color.RED);
            return;
        }

        boolean newStatus = !selectedUser.isActive();
        if (adminService.toggleUserLoginStatus(loggedInAdmin.getUsername(), selectedUser.getUsername(), newStatus)) {
            userManagementMessageLabel.setText("用户 '" + selectedUser.getUsername() + "' 登录状态已切换为 " + (newStatus ? "激活" : "禁用") + "。");
            userManagementMessageLabel.setTextFill(Color.GREEN);
            refreshUserList(null); // Refresh user list
        } else {
            userManagementMessageLabel.setText("切换登录状态失败。");
            userManagementMessageLabel.setTextFill(Color.RED);
        }
    }

    @FXML
    private void handleModifyPermissions(ActionEvent event) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            userManagementMessageLabel.setText("请选择一个用户。");
            userManagementMessageLabel.setTextFill(Color.RED);
            return;
        }

        Alert modifyPermissionsDialog = new Alert(Alert.AlertType.CONFIRMATION);
        modifyPermissionsDialog.setTitle("修改用户权限");
        modifyPermissionsDialog.setHeaderText("修改用户 '" + selectedUser.getUsername() + "' 的权限");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        CheckBox canDepositCheckbox = new CheckBox("允许存款");
        canDepositCheckbox.setSelected(selectedUser.canDeposit());
        CheckBox canWithdrawCheckbox = new CheckBox("允许取款");
        canWithdrawCheckbox.setSelected(selectedUser.canWithdraw());

        grid.add(canDepositCheckbox, 0, 0);
        grid.add(canWithdrawCheckbox, 0, 1);

        modifyPermissionsDialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = modifyPermissionsDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Map<String, Boolean> newPermissions = new HashMap<>();
            newPermissions.put("canDeposit", canDepositCheckbox.isSelected());
            newPermissions.put("canWithdraw", canWithdrawCheckbox.isSelected());

            if (adminService.modifyUserPermissions(loggedInAdmin.getUsername(), selectedUser.getUsername(), newPermissions)) {
                userManagementMessageLabel.setText("用户 '" + selectedUser.getUsername() + "' 权限修改成功！");
                userManagementMessageLabel.setTextFill(Color.GREEN);
                refreshUserList(null); // Refresh user list
            } else {
                userManagementMessageLabel.setText("修改权限失败。");
                userManagementMessageLabel.setTextFill(Color.RED);
            }
        }
    }

    @FXML
    private void handleDeleteUser(ActionEvent event) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            userManagementMessageLabel.setText("请选择一个用户。");
            userManagementMessageLabel.setTextFill(Color.RED);
            return;
        }

        Alert confirmDelete = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDelete.setTitle("确认删除");
        confirmDelete.setHeaderText("您确定要删除用户 '" + selectedUser.getUsername() + "' 吗？");
        confirmDelete.setContentText("此操作不可逆转。");

        Optional<ButtonType> result = confirmDelete.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Access DAO directly through loggingService.getDao() to delete user
            loggingService.getDao().deleteUser(selectedUser.getUserId());
            userManagementMessageLabel.setText("用户 '" + selectedUser.getUsername() + "' 已删除。");
            userManagementMessageLabel.setTextFill(Color.GREEN);
            refreshUserList(null); // Refresh user list
        }
    }

    @FXML
    private void refreshLogs(ActionEvent event) {
        List<OperationLog> logs = loggingService.getLogs();
        ObservableList<OperationLog> observableLogs = FXCollections.observableArrayList(logs);
        logTable.setItems(observableLogs);
        logMessageLabel.setText("");
    }

    @FXML
    private void handleExportLogs(ActionEvent event) {
        // Export to logs.csv file in the project root directory
        String filePath = "logs.csv";
        if (loggingService.exportLogs(filePath, "CSV")) {
            logMessageLabel.setText("日志已成功导出到 " + filePath);
            logMessageLabel.setTextFill(Color.GREEN);
        } else {
            logMessageLabel.setText("日志导出失败。");
            logMessageLabel.setTextFill(Color.RED);
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/banksimulation/view/LoginView.fxml"));
            App app = (App) primaryStage.getUserData(); // Get App instance

            fxmlLoader.setControllerFactory(controllerClass -> {
                if (controllerClass == com.banksimulation.view.LoginController.class) {
                    return new com.banksimulation.view.LoginController(
                            app.getAuthenticationService(),
                            app.getUserService(),
                            app.getAdminService(),
                            app.getLoggingService(),
                            primaryStage
                    );
                }
                try {
                    return controllerClass.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            primaryStage.setTitle("银行模拟系统 - 登录");
            primaryStage.setScene(scene);
            primaryStage.show();
            loggingService.logAdminAction(loggedInAdmin.getUsername(), "Logout", "Admin logged out.");
        } catch (IOException e) {
            System.err.println("Error loading login view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Show user management panel and hide other panels
     * Shows the user management panel and hides other panels.
     */
    @FXML
    private void showUserManagement(ActionEvent event) {
        userManagementPanel.setVisible(true);
        userManagementPanel.toFront(); // Bring this panel to the front
        logViewPanel.setVisible(false);
        refreshUserList(null); // Refresh user list
    }

    /**
     * Show log view panel and hide other panels
     * Shows the log view panel and hides other panels.
     */
    @FXML
    private void showLogView(ActionEvent event) {
        logViewPanel.setVisible(true);
        logViewPanel.toFront(); // Bring this panel to the front
        userManagementPanel.setVisible(false);
        refreshLogs(null); // Refresh logs
    }
}
