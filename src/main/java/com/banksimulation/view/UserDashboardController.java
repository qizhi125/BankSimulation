package com.banksimulation.view;

import com.banksimulation.App; // Import App class
import com.banksimulation.entity.TransactionRecord;
import com.banksimulation.entity.User;
import com.banksimulation.service.LoggingService;
import com.banksimulation.service.UserService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.layout.VBox; // Import VBox

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * 用户仪表板控制器
 * Controller for the user dashboard view.
 */
public class UserDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label usernameLabel;
    @FXML private Label accountNumberLabel;
    @FXML private Label balanceLabel;
    @FXML private Label statusLabel;

    @FXML private TextField depositAmountField;
    @FXML private Label depositMessageLabel;

    @FXML private TextField withdrawAmountField;
    @FXML private Label withdrawMessageLabel;

    @FXML private TableView<TransactionRecord> transactionTable;
    @FXML private TableColumn<TransactionRecord, String> colTimestamp;
    @FXML private TableColumn<TransactionRecord, String> colType;
    @FXML private TableColumn<TransactionRecord, Double> colAmount;
    @FXML private TableColumn<TransactionRecord, Double> colBalanceAfter;
    @FXML private TableColumn<TransactionRecord, String> colDescription;
    @FXML private Label transactionMessageLabel;

    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmNewPasswordField;
    @FXML private Label passwordMessageLabel;

    // 新增的导航按钮
    @FXML private javafx.scene.control.Button navAccountOverviewButton;
    @FXML private javafx.scene.control.Button navDepositButton;
    @FXML private javafx.scene.control.Button navWithdrawButton;
    @FXML private javafx.scene.control.Button navTransactionHistoryButton;
    @FXML private javafx.scene.control.Button navChangePasswordButton;

    // 新增的面板容器
    @FXML private VBox accountOverviewPanel;
    @FXML private VBox depositPanel;
    @FXML private VBox withdrawPanel;
    @FXML private VBox transactionHistoryPanel;
    @FXML private VBox changePasswordPanel;


    private final UserService userService;
    private final LoggingService loggingService;
    private final Stage primaryStage;
    private User currentUser; // 当前登录的用户

    // 构造函数，通过App类进行依赖注入
    public UserDashboardController(UserService userService, LoggingService loggingService, Stage primaryStage, User currentUser) {
        this.userService = userService;
        this.loggingService = loggingService;
        this.primaryStage = primaryStage;
        this.currentUser = currentUser;
    }

    @FXML
    public void initialize() {
        // 初始化账户概览
        if (currentUser != null) {
            welcomeLabel.setText("欢迎，" + currentUser.getUsername() + "！");
            setupTransactionTable();
            // 默认显示账户概览面板
            showAccountOverview(null);
        }
    }

    private void setupTransactionTable() {
        colTimestamp.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        colType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType().name()));
        colAmount.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());
        colBalanceAfter.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getBalanceAfterTransaction()).asObject());
        colDescription.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
    }

    @FXML
    private void refreshAccountInfo() {
        Optional<User> updatedUser = userService.getUserDetails(currentUser.getUsername());
        if (updatedUser.isPresent()) {
            currentUser = updatedUser.get(); // 更新当前用户对象
            usernameLabel.setText(currentUser.getUsername());
            accountNumberLabel.setText(currentUser.getAccountNumber());
            balanceLabel.setText(String.format("%.2f", currentUser.getBalance()));
            statusLabel.setText(currentUser.isActive() ? "激活" : "禁用");
        } else {
            // 如果用户不存在，可能表示账户被删除或出现严重错误，应该返回登录界面
            System.err.println("Error: Current user not found after refresh. Logging out.");
            handleLogout(null);
        }
    }

    @FXML
    private void handleDeposit(ActionEvent event) {
        try {
            double amount = Double.parseDouble(depositAmountField.getText());
            if (userService.deposit(currentUser.getUsername(), amount)) {
                depositMessageLabel.setText("存款成功！新余额：" + String.format("%.2f", currentUser.getBalance()));
                depositMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                depositAmountField.clear();
                refreshAccountInfo(); // 刷新余额显示
                refreshTransactionHistory(); // 刷新交易记录
            } else {
                depositMessageLabel.setText("存款失败。");
                depositMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            }
        } catch (NumberFormatException e) {
            depositMessageLabel.setText("请输入有效的金额。");
            depositMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    private void handleWithdraw(ActionEvent event) {
        try {
            double amount = Double.parseDouble(withdrawAmountField.getText());
            if (userService.withdraw(currentUser.getUsername(), amount)) {
                withdrawMessageLabel.setText("取款成功！新余额：" + String.format("%.2f", currentUser.getBalance()));
                withdrawMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                withdrawAmountField.clear();
                refreshAccountInfo(); // 刷新余额显示
                refreshTransactionHistory(); // 刷新交易记录
            } else {
                withdrawMessageLabel.setText("取款失败。");
                withdrawMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            }
        } catch (NumberFormatException e) {
            withdrawMessageLabel.setText("请输入有效的金额。");
            withdrawMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    private void refreshTransactionHistory() {
        // Access DAO directly through loggingService.getDao() to get transaction records
        List<TransactionRecord> transactions = loggingService.getDao().getTransactionsByUserId(currentUser.getUserId());
        ObservableList<TransactionRecord> observableTransactions = FXCollections.observableArrayList(transactions);
        transactionTable.setItems(observableTransactions);
        if (transactions.isEmpty()) {
            transactionMessageLabel.setText("没有交易记录。");
            transactionMessageLabel.setTextFill(javafx.scene.paint.Color.GRAY);
        } else {
            transactionMessageLabel.setText("");
        }
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmNewPassword = confirmNewPasswordField.getText();

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            passwordMessageLabel.setText("所有密码字段都不能为空。");
            passwordMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }
        if (!newPassword.equals(confirmNewPassword)) {
            passwordMessageLabel.setText("新密码和确认密码不匹配。");
            passwordMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }
        if (newPassword.length() < 6) { // Simple password strength check
            passwordMessageLabel.setText("新密码至少需要6个字符。");
            passwordMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        if (userService.updatePassword(currentUser.getUsername(), oldPassword, newPassword)) {
            passwordMessageLabel.setText("密码修改成功！");
            passwordMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            oldPasswordField.clear();
            newPasswordField.clear();
            confirmNewPasswordField.clear();
        } else {
            passwordMessageLabel.setText("密码修改失败：旧密码错误或发生其他错误。");
            passwordMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/banksimulation/view/LoginView.fxml"));
            // Get App instance to inject services
            com.banksimulation.App app = (com.banksimulation.App) primaryStage.getUserData();

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
            loggingService.logUserAction(currentUser.getUsername(), "Logout", "User logged out.");
        } catch (IOException e) {
            System.err.println("Error loading login view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Show account overview panel and hide other panels
     */
    @FXML
    private void showAccountOverview(ActionEvent event) {
        accountOverviewPanel.setVisible(true);
        accountOverviewPanel.toFront();
        depositPanel.setVisible(false);
        withdrawPanel.setVisible(false);
        transactionHistoryPanel.setVisible(false);
        changePasswordPanel.setVisible(false);
        refreshAccountInfo(); // Refresh account info
    }

    /**
     * Show deposit panel and hide other panels
     */
    @FXML
    private void showDeposit(ActionEvent event) {
        depositPanel.setVisible(true);
        depositPanel.toFront();
        accountOverviewPanel.setVisible(false);
        withdrawPanel.setVisible(false);
        transactionHistoryPanel.setVisible(false);
        changePasswordPanel.setVisible(false);
        depositAmountField.clear(); // Clear input
        depositMessageLabel.setText(""); // Clear message
    }

    /**
     * Show withdrawal panel and hide other panels
     */
    @FXML
    private void showWithdraw(ActionEvent event) {
        withdrawPanel.setVisible(true);
        withdrawPanel.toFront();
        accountOverviewPanel.setVisible(false);
        depositPanel.setVisible(false);
        transactionHistoryPanel.setVisible(false);
        changePasswordPanel.setVisible(false);
        withdrawAmountField.clear(); // Clear input
        withdrawMessageLabel.setText(""); // Clear message
    }

    /**
     * Show transaction history panel and hide other panels
     */
    @FXML
    private void showTransactionHistory(ActionEvent event) {
        transactionHistoryPanel.setVisible(true);
        transactionHistoryPanel.toFront();
        accountOverviewPanel.setVisible(false);
        depositPanel.setVisible(false);
        withdrawPanel.setVisible(false);
        changePasswordPanel.setVisible(false);
        refreshTransactionHistory(); // Refresh transaction history
    }

    /**
     * Show change password panel and hide other panels
     */
    @FXML
    private void showChangePassword(ActionEvent event) {
        changePasswordPanel.setVisible(true);
        changePasswordPanel.toFront();
        accountOverviewPanel.setVisible(false);
        depositPanel.setVisible(false);
        withdrawPanel.setVisible(false);
        transactionHistoryPanel.setVisible(false);
        oldPasswordField.clear();
        newPasswordField.clear();
        confirmNewPasswordField.clear();
        passwordMessageLabel.setText("");
    }
}
