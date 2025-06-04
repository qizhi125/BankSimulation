package com.banksimulation.view;

import com.banksimulation.App;
import com.banksimulation.entity.Admin;
import com.banksimulation.entity.OperationLog;
import com.banksimulation.entity.TransactionRecord;
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
import javafx.scene.layout.VBox;
// 移除 TabPane 和 Tab 的导入
// import javafx.scene.control.TabPane;
// import javafx.scene.control.Tab;

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

    // 新增的交易记录表格和列
    @FXML private TableView<TransactionRecord> allTransactionsTable;
    @FXML private TableColumn<TransactionRecord, String> colTransId;
    @FXML private TableColumn<TransactionRecord, String> colTransUserId;
    @FXML private TableColumn<TransactionRecord, String> colTransAccountNumber;
    @FXML private TableColumn<TransactionRecord, String> colTransType;
    @FXML private TableColumn<TransactionRecord, Double> colTransAmount;
    @FXML private TableColumn<TransactionRecord, Double> colTransBalanceAfter;
    @FXML private TableColumn<TransactionRecord, String> colTransTimestamp;
    @FXML private TableColumn<TransactionRecord, String> colTransDescription;
    @FXML private Label allTransactionsMessageLabel;


    // FXML 元素，用于权限控制
    @FXML private Button refreshUserListButton;
    @FXML private Button createUserButton;
    @FXML private Button toggleActiveButton;
    @FXML private Button modifyPermissionsButton;
    @FXML private Button deleteUserButton;
    @FXML private Button refreshLogsButton;
    @FXML private Button exportLogsButton;
    @FXML private Button refreshAllTransactionsButton; // 新增按钮

    // 新增的导航按钮
    @FXML private Button navUserManagementButton;
    @FXML private Button navLogViewButton;
    @FXML private Button navAllTransactionsButton; // 新增导航按钮

    // 新增的面板容器
    @FXML private VBox userManagementPanel;
    @FXML private VBox logViewPanel;
    @FXML private VBox allTransactionsPanel; // 新增面板

    // 移除 TabPane 引用
    // @FXML private TabPane adminTabPane;


    private final AdminService adminService;
    private final UserService userService;
    private final LoggingService loggingService;
    private final Stage primaryStage;
    private Admin loggedInAdmin; // 当前登录的管理员对象 (Admin 实例)

    // 构造函数，通过App类进行依赖注入
    public AdminDashboardController(AdminService adminService, UserService userService,
                                    LoggingService loggingService, Stage primaryStage, Admin loggedInAdmin) {
        this.adminService = adminService;
        this.userService = userService;
        this.loggingService = loggingService;
        this.primaryStage = primaryStage;
        this.loggedInAdmin = loggedInAdmin; // 接收 Admin 对象
    }

    @FXML
    public void initialize() {
        if (loggedInAdmin != null) {
            welcomeLabel.setText("欢迎，管理员 " + loggedInAdmin.getUsername() + "！");
            setupUserTable();
            setupLogTable();
            setupAllTransactionsTable(); // 初始化所有交易表格
            applyPermissionsToUI(); // 根据管理员权限调整UI
            // 默认显示用户管理面板
            showUserManagement(null);
        }
    }

    /**
     * 根据当前登录管理员的权限，动态调整UI元素的可见性和可用性。
     */
    private void applyPermissionsToUI() {
        if (loggedInAdmin == null) {
            System.err.println("Error: loggedInAdmin is null in applyPermissionsToUI.");
            return;
        }

        boolean isTopLevel = loggedInAdmin.isTopLevelAdmin();

        // 用户管理相关权限
        createUserButton.setManaged(true);
        createUserButton.setVisible(true);
        toggleActiveButton.setManaged(true);
        toggleActiveButton.setVisible(true);
        modifyPermissionsButton.setManaged(isTopLevel); // 只有顶级管理员可以修改权限
        modifyPermissionsButton.setVisible(isTopLevel);
        deleteUserButton.setManaged(true);
        deleteUserButton.setVisible(true); // 删除用户，但逻辑层会限制非顶级管理员删除顶级管理员

        // 日志查看和交易记录权限 (所有管理员都可以查看)
        refreshLogsButton.setManaged(true);
        refreshLogsButton.setVisible(true);
        exportLogsButton.setManaged(true);
        exportLogsButton.setVisible(true);
        refreshAllTransactionsButton.setManaged(true);
        refreshAllTransactionsButton.setVisible(true);

        // 导航按钮
        navUserManagementButton.setManaged(true);
        navUserManagementButton.setVisible(true);
        navLogViewButton.setManaged(true);
        navLogViewButton.setVisible(true);
        navAllTransactionsButton.setManaged(true); // 所有管理员都可以查看所有交易
        navAllTransactionsButton.setVisible(true);
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

    private void setupAllTransactionsTable() {
        colTransId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTransactionId()));
        colTransUserId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUserId()));
        colTransAccountNumber.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAccountNumber()));
        colTransType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType().name()));
        colTransAmount.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());
        colTransBalanceAfter.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getBalanceAfterTransaction()).asObject());
        colTransTimestamp.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        colTransDescription.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
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
                refreshUserList(null); // 刷新用户列表
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

        // 检查是否尝试修改顶级管理员
        Optional<Admin> targetAdmin = adminService.getAdminByUsername(selectedUser.getUsername());
        if (targetAdmin.isPresent() && targetAdmin.get().isTopLevelAdmin() && !loggedInAdmin.isTopLevelAdmin()) {
            userManagementMessageLabel.setText("您没有权限修改顶级管理员的状态。");
            userManagementMessageLabel.setTextFill(Color.RED);
            return;
        }

        boolean newStatus = !selectedUser.isActive();
        if (adminService.toggleUserLoginStatus(loggedInAdmin.getUsername(), selectedUser.getUsername(), newStatus)) {
            userManagementMessageLabel.setText("用户 '" + selectedUser.getUsername() + "' 登录状态已切换为 " + (newStatus ? "激活" : "禁用") + "。");
            userManagementMessageLabel.setTextFill(Color.GREEN);
            refreshUserList(null); // 刷新用户列表
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

        // 只有顶级管理员有权限修改用户权限
        if (!loggedInAdmin.isTopLevelAdmin()) {
            userManagementMessageLabel.setText("您没有权限修改用户权限。只有顶级管理员可以执行此操作。");
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
                refreshUserList(null); // 刷新用户列表
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

        // 检查是否尝试删除顶级管理员
        Optional<Admin> targetAdmin = adminService.getAdminByUsername(selectedUser.getUsername());
        if (targetAdmin.isPresent() && targetAdmin.get().isTopLevelAdmin() && !loggedInAdmin.isTopLevelAdmin()) {
            userManagementMessageLabel.setText("您没有权限删除顶级管理员。");
            userManagementMessageLabel.setTextFill(Color.RED);
            return;
        }
        // 检查是否尝试删除自己
        if (selectedUser.getUsername().equals(loggedInAdmin.getUsername())) {
            userManagementMessageLabel.setText("您不能删除您自己的账户。");
            userManagementMessageLabel.setTextFill(Color.RED);
            return;
        }

        Alert confirmDelete = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDelete.setTitle("确认删除");
        confirmDelete.setHeaderText("您确定要删除用户 '" + selectedUser.getUsername() + "' 吗？");
        confirmDelete.setContentText("此操作不可逆转。");

        Optional<ButtonType> result = confirmDelete.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (adminService.deleteUser(loggedInAdmin.getUsername(), selectedUser.getUsername())) {
                userManagementMessageLabel.setText("用户 '" + selectedUser.getUsername() + "' 已删除。");
                userManagementMessageLabel.setTextFill(Color.GREEN);
                refreshUserList(null); // 刷新用户列表
            } else {
                userManagementMessageLabel.setText("删除用户失败。");
                userManagementMessageLabel.setTextFill(Color.RED);
            }
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
            logMessageLabel.setTextFill(Color.GREEN);
        } else {
            logMessageLabel.setText("日志导出失败。");
            logMessageLabel.setTextFill(Color.RED);
        }
    }

    @FXML
    private void refreshAllTransactions(ActionEvent event) {
        List<TransactionRecord> transactions = adminService.getAllTransactions(loggedInAdmin.getUsername());
        ObservableList<TransactionRecord> observableTransactions = FXCollections.observableArrayList(transactions);
        allTransactionsTable.setItems(observableTransactions);
        allTransactionsMessageLabel.setText("");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/banksimulation/view/LoginView.fxml"));
            App app = (App) primaryStage.getUserData(); // 获取App实例

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
     * 显示用户管理面板并隐藏其他面板
     */
    @FXML
    private void showUserManagement(ActionEvent event) {
        userManagementPanel.setVisible(true);
        userManagementPanel.toFront();
        logViewPanel.setVisible(false);
        allTransactionsPanel.setVisible(false);
        refreshUserList(null);
        // 移除了 adminTabPane.getSelectionModel().select(0);
    }

    /**
     * 显示日志查看面板并隐藏其他面板
     */
    @FXML
    private void showLogView(ActionEvent event) {
        logViewPanel.setVisible(true);
        logViewPanel.toFront();
        userManagementPanel.setVisible(false);
        allTransactionsPanel.setVisible(false);
        refreshLogs(null);
        // 移除了 adminTabPane.getSelectionModel().select(1);
    }

    /**
     * 显示所有交易面板并隐藏其他面板
     */
    @FXML
    private void showAllTransactions(ActionEvent event) {
        allTransactionsPanel.setVisible(true);
        allTransactionsPanel.toFront();
        userManagementPanel.setVisible(false);
        logViewPanel.setVisible(false);
        refreshAllTransactions(null);
        // 移除了 adminTabPane.getSelectionModel().select(2);
    }
}
