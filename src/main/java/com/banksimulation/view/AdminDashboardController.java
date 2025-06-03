package com.banksimulation.view;

import com.banksimulation.entity.ActorType;
import com.banksimulation.entity.Admin;
import com.banksimulation.entity.OperationLog;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField; // 导入 PasswordField
import javafx.scene.layout.GridPane; // 导入 GridPane
import javafx.scene.control.CheckBox; // 导入 CheckBox
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 管理员仪表板控制器
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


    private final AdminService adminService;
    private final UserService userService; // 尽管主要用于用户操作，但在这里传递以便后续导航
    private final LoggingService loggingService;
    private final Stage primaryStage;
    private Admin currentAdmin; // 当前登录的管理员

    // 构造函数，通过App类进行依赖注入
    public AdminDashboardController(AdminService adminService, UserService userService,
                                    LoggingService loggingService, Stage primaryStage, Admin currentAdmin) {
        this.adminService = adminService;
        this.userService = userService;
        this.loggingService = loggingService;
        this.primaryStage = primaryStage;
        this.currentAdmin = currentAdmin;
    }

    @FXML
    public void initialize() {
        if (currentAdmin != null) {
            welcomeLabel.setText("欢迎，管理员 " + currentAdmin.getUsername() + "！");
            setupUserTable();
            refreshUserList(null);
            setupLogTable();
            refreshLogs(null);
        }
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
        List<User> users = adminService.getAllUsers(currentAdmin.getUsername());
        ObservableList<User> observableUsers = FXCollections.observableArrayList(users);
        userTable.setItems(observableUsers);
        userManagementMessageLabel.setText("");
    }

    @FXML
    private void handleCreateUser(ActionEvent event) {
        // 弹出对话框让管理员输入新用户的信息
        // 简化处理：直接在控制台模拟输入或弹出一个简单的输入框
        // 实际应用中，这里会弹出一个新的 Stage 或 Dialog
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
                userManagementMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                return;
            }
            if (accountNumber.length() != 10 || !accountNumber.matches("\\d+")) {
                userManagementMessageLabel.setText("创建用户失败：账号必须是10位数字。");
                userManagementMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                return;
            }

            String hashedPassword = PasswordHasher.hashPassword(password);
            User newUser = new User(username, hashedPassword, firstName, lastName, accountNumber);

            if (adminService.createUser(currentAdmin.getUsername(), newUser)) {
                userManagementMessageLabel.setText("用户 '" + username + "' 创建成功！");
                userManagementMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                refreshUserList(null); // 刷新用户列表
            } else {
                userManagementMessageLabel.setText("创建用户失败：用户名或账号可能已存在。");
                userManagementMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            }
        }
    }

    @FXML
    private void handleToggleActive(ActionEvent event) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            userManagementMessageLabel.setText("请选择一个用户。");
            userManagementMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        boolean newStatus = !selectedUser.isActive();
        if (adminService.toggleUserLoginStatus(currentAdmin.getUsername(), selectedUser.getUsername(), newStatus)) {
            userManagementMessageLabel.setText("用户 '" + selectedUser.getUsername() + "' 登录状态已切换为 " + (newStatus ? "激活" : "禁用") + "。");
            userManagementMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            refreshUserList(null); // 刷新用户列表
        } else {
            userManagementMessageLabel.setText("切换登录状态失败。");
            userManagementMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    private void handleModifyPermissions(ActionEvent event) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            userManagementMessageLabel.setText("请选择一个用户。");
            userManagementMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        Alert modifyPermissionsDialog = new Alert(Alert.AlertType.CONFIRMATION);
        modifyPermissionsDialog.setTitle("修改用户权限");
        modifyPermissionsDialog.setHeaderText("修改用户 '" + selectedUser.getUsername() + "' 的权限");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        javafx.scene.control.CheckBox canDepositCheckbox = new javafx.scene.control.CheckBox("允许存款");
        canDepositCheckbox.setSelected(selectedUser.canDeposit());
        javafx.scene.control.CheckBox canWithdrawCheckbox = new javafx.scene.control.CheckBox("允许取款");
        canWithdrawCheckbox.setSelected(selectedUser.canWithdraw());

        grid.add(canDepositCheckbox, 0, 0);
        grid.add(canWithdrawCheckbox, 0, 1);

        modifyPermissionsDialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = modifyPermissionsDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Map<String, Boolean> newPermissions = new HashMap<>();
            newPermissions.put("canDeposit", canDepositCheckbox.isSelected());
            newPermissions.put("canWithdraw", canWithdrawCheckbox.isSelected());

            if (adminService.modifyUserPermissions(currentAdmin.getUsername(), selectedUser.getUsername(), newPermissions)) {
                userManagementMessageLabel.setText("用户 '" + selectedUser.getUsername() + "' 权限修改成功！");
                userManagementMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                refreshUserList(null); // 刷新用户列表
            } else {
                userManagementMessageLabel.setText("修改权限失败。");
                userManagementMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            }
        }
    }

    @FXML
    private void handleDeleteUser(ActionEvent event) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            userManagementMessageLabel.setText("请选择一个用户。");
            userManagementMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        Alert confirmDelete = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDelete.setTitle("确认删除");
        confirmDelete.setHeaderText("您确定要删除用户 '" + selectedUser.getUsername() + "' 吗？");
        confirmDelete.setContentText("此操作不可逆转。");

        Optional<ButtonType> result = confirmDelete.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 直接通过loggingService.getDao()访问DAO来删除用户
            loggingService.getDao().deleteUser(selectedUser.getUserId());
            userManagementMessageLabel.setText("用户 '" + selectedUser.getUsername() + "' 已删除。");
            userManagementMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            refreshUserList(null); // 刷新用户列表
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
        // 简单导出到项目根目录下的 logs.csv 文件
        String filePath = "logs.csv";
        if (loggingService.exportLogs(filePath, "CSV")) {
            logMessageLabel.setText("日志已成功导出到 " + filePath);
            logMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
        } else {
            logMessageLabel.setText("日志导出失败。");
            logMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/banksimulation/view/LoginView.fxml"));
            com.banksimulation.App app = (com.banksimulation.App) primaryStage.getUserData(); // 获取App实例

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
            loggingService.logAdminAction(currentAdmin.getUsername(), "Logout", "Admin logged out.");
        } catch (IOException e) {
            System.err.println("Error loading login view: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
