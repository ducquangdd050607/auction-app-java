package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.session.AuctionSession;
import com.auctionapp.auctionappjava.client.session.UserSession;
import com.auctionapp.auctionappjava.common.exception.AppException;
import com.auctionapp.auctionappjava.common.util.AlertUtils;
import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NavigatorController implements Initializable {

    private static final String FAQ_INTRO_MESSAGE =
            "Xin chào, tôi là trợ lý FAQ. Tôi chỉ trả lời các câu hỏi liên quan tới app đấu giá này và không trả lời các vấn đề khác.";

    private Stage stage;
    private static NavigatorController instance;
    public static String modeName;

    @FXML
    private StackPane rootStackPane;
    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnExit;
    @FXML
    private Button btnGotoUsersManager;
    @FXML
    private Button btnHistory;
    @FXML
    private Button btnItemListAdmin;
    @FXML
    private Button btnItemListBidder;
    @FXML
    private Button btnItemListSeller;
    @FXML
    private Button btnItemManager;
    @FXML
    private Button btnTrend;
    @FXML
    private Button btnTransactionList;
    @FXML
    private Button btnSignout;
    @FXML
    private VBox groupAccount;
    @FXML
    private VBox groupAdmin;
    @FXML
    private VBox groupBidder;
    @FXML
    private VBox groupHome;
    @FXML
    private VBox groupSeller;
    @FXML
    private Button identity;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private Button setting;
    @FXML
    private VBox chatbotPanel;
    @FXML
    private Button chatbotToggleButton;
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private VBox chatMessagesBox;
    @FXML
    private VBox quickQuestionsBox;
    @FXML
    private TextField chatInput;

    private final List<ChatbotAnswer> chatbotAnswers = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        loadChatbotAnswers();
        setupChatbot();
        try {
            show();
        } catch (IOException e) {
            throw new AppException("Không thể khởi tạo điều hướng", e);
        } finally {
            // Hàm fire() có tác dụng sẽ bấm thẳng vào nút được fire ngay khi load (initialize) scene hiện tại
            btnDashboard.fire();
        }
    }

    private void setupChatbot() {
        chatbotPanel.setVisible(false);
        chatbotPanel.setManaged(false);
        chatbotToggleButton.setText("☁");
        addBotMessage(FAQ_INTRO_MESSAGE);
        renderQuickQuestions();
    }

    @FXML
    void toggleChatbot(ActionEvent event) {
        boolean open = !chatbotPanel.isVisible();
        chatbotPanel.setVisible(open);
        chatbotPanel.setManaged(open);
        chatbotToggleButton.setText(open ? "X" : "☁");
        if (open) {
            chatInput.requestFocus();
            scrollChatToBottom();
        }
    }

    @FXML
    void sendChatMessage(ActionEvent event) {
        String question = chatInput.getText();
        if (question == null || question.trim().isEmpty()) {
            addBotMessage(FAQ_INTRO_MESSAGE);
            return;
        }

        chatInput.clear();
        addUserMessage(question.trim());
        addBotMessage(findAnswer(question.trim()));
    }

    private void askQuickQuestion(ChatbotAnswer answer) {
        addUserMessage(answer.question());
        addBotMessage(answer.answer());
    }

    private void renderQuickQuestions() {
        quickQuestionsBox.getChildren().clear();
        chatbotAnswers.stream().limit(3).forEach(answer -> {
            Button button = new Button(answer.question());
            button.setMaxWidth(Double.MAX_VALUE);
            button.getStyleClass().add("chatbot-quick-btn");
            button.setOnAction(event -> askQuickQuestion(answer));
            quickQuestionsBox.getChildren().add(button);
        });
    }

    private void addUserMessage(String message) {
        addChatMessage(message, "chatbot-user-message", true);
    }

    private void addBotMessage(String message) {
        addChatMessage(message, "chatbot-bot-message", false);
    }

    private void addChatMessage(String message, String styleClass, boolean alignRight) {
        Label bubble = new Label(message);
        bubble.setWrapText(true);
        bubble.setMaxWidth(260);
        bubble.getStyleClass().add(styleClass);

        Region spacer = new Region();
        HBox row = new HBox(8);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        if (alignRight) {
            row.getChildren().addAll(spacer, bubble);
        } else {
            row.getChildren().addAll(bubble, spacer);
        }

        chatMessagesBox.getChildren().add(row);
        scrollChatToBottom();
    }

    private void scrollChatToBottom() {
        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }

    private String findAnswer(String question) {
        if (question == null || question.trim().isEmpty()) {
            return FAQ_INTRO_MESSAGE;
        }

        String normalizedQuestion = normalize(question);
        ChatbotAnswer bestAnswer = null;
        int bestScore = 0;

        for (ChatbotAnswer answer : chatbotAnswers) {
            int score = scoreAnswer(normalizedQuestion, answer);
            if (score > bestScore) {
                bestScore = score;
                bestAnswer = answer;
            }
        }

        if (bestAnswer != null && bestScore > 0) {
            return bestAnswer.answer();
        }

        return FAQ_INTRO_MESSAGE;
    }

    private int scoreAnswer(String normalizedQuestion, ChatbotAnswer answer) {
        int score = 0;
        if (normalizedQuestion.contains(normalize(answer.question()))) {
            score += 5;
        }
        for (String keyword : answer.keywords()) {
            if (normalizedQuestion.contains(normalize(keyword))) {
                score += 2;
            }
        }
        return score;
    }

    private void loadChatbotAnswers() {
        URL resource = getClass().getResource("/com/auctionapp/auctionappjava/data/chatbot-questions.json");
        if (resource == null) {
            chatbotAnswers.add(new ChatbotAnswer(
                    "Chatbot hỗ trợ gì?",
                    "Hiện chưa tìm thấy file dữ liệu chatbot.",
                    List.of("chatbot", "hỗ trợ")));
            return;
        }

        try (InputStream inputStream = resource.openStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line).append('\n');
            }
            chatbotAnswers.addAll(parseChatbotAnswers(json.toString()));
        } catch (IOException e) {
            chatbotAnswers.add(new ChatbotAnswer(
                    "Chatbot hỗ trợ gì?",
                    "Không thể đọc file dữ liệu chatbot.",
                    List.of("chatbot", "hỗ trợ")));
        }
    }

    private List<ChatbotAnswer> parseChatbotAnswers(String json) {
        List<ChatbotAnswer> answers = new ArrayList<>();
        Pattern objectPattern = Pattern.compile("\\{\\s*\"question\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"\\s*,\\s*\"answer\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"\\s*,\\s*\"keywords\"\\s*:\\s*\\[(.*?)\\]\\s*}", Pattern.DOTALL);
        Matcher objectMatcher = objectPattern.matcher(json);

        while (objectMatcher.find()) {
            String question = unescapeJson(objectMatcher.group(1));
            String answer = unescapeJson(objectMatcher.group(2));
            List<String> keywords = parseKeywords(objectMatcher.group(3));
            answers.add(new ChatbotAnswer(question, answer, keywords));
        }
        return answers;
    }

    private List<String> parseKeywords(String jsonArrayContent) {
        List<String> keywords = new ArrayList<>();
        Matcher keywordMatcher = Pattern.compile("\"((?:\\\\.|[^\"])*)\"").matcher(jsonArrayContent);
        while (keywordMatcher.find()) {
            keywords.add(unescapeJson(keywordMatcher.group(1)));
        }
        return keywords;
    }

    private String unescapeJson(String value) {
        return value
                .replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\\\", "\\");
    }

    private String normalize(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        return Arrays.stream(normalized.split("[^a-z0-9]+"))
                .filter(token -> !token.isBlank())
                .reduce("", (left, right) -> left + " " + right);
    }

    public static BorderPane getMainBorderPane() {
        return instance.mainBorderPane;
    }

    public void show() throws IOException {
        // Ẩn tất cả đi trước
        groupAdmin.setVisible(false);
        groupAdmin.setManaged(false);
        groupSeller.setVisible(false);
        groupSeller.setManaged(false);
        groupBidder.setVisible(false);
        groupBidder.setManaged(false);

        // Lấy giá trị boolean từ class LoginController ra kiểm tra
        if (LoginController.adminRoute) {
            groupAdmin.setVisible(true);
            groupAdmin.setManaged(true);
            identity.setText("ADMIN");
            identity.setDisable(true);

        } else if (LoginController.sellerRoute) {
            groupSeller.setVisible(true);
            groupSeller.setManaged(true);
            identity.setText("SELLER");
            identity.setDisable(true);

        } else if (LoginController.bidderRoute) {
            groupBidder.setVisible(true);
            groupBidder.setManaged(true);
            identity.setText("BIDDER");
            identity.setDisable(true);
        }
    }

    @FXML
    void handleNotify() {

    }

    @FXML
    void handleAccount(ActionEvent event) throws IOException {
        SceneSwitcherUtils.NavSceneController(event, mainBorderPane, "/com/auctionapp/auctionappjava/views/AccountScreen.fxml");
        activateAccountButton();
    }

    @FXML
    void handleItemsList(ActionEvent event) throws IOException {
        modeName = ((Button) event.getSource()).getText();
        setActiveButton((Button) event.getSource());
        SceneSwitcherUtils.NavSceneController(event, mainBorderPane, "/com/auctionapp/auctionappjava/views/AuctionListScreen.fxml");
    }

    @FXML
    void handleTrend(ActionEvent event) throws IOException {
        setActiveButton(btnTrend);
        SceneSwitcherUtils.NavSceneController(event, mainBorderPane, "/com/auctionapp/auctionappjava/views/AuctionTrendScreen.fxml");
    }

    @FXML
    void handleHistory(ActionEvent event) throws IOException {
        SceneSwitcherUtils.NavSceneController(event, mainBorderPane, "/com/auctionapp/auctionappjava/views/HistoryScreen.fxml");
        activateHistory();
    }

    @FXML
    void handleBackToDash(ActionEvent event) throws IOException {
        activateDashboardButton();
        SceneSwitcherUtils.NavSceneController(event, mainBorderPane, "/com/auctionapp/auctionappjava/views/DashboardScreen.fxml");
    }

    @FXML
    void handleGotoUsersManager(ActionEvent event) throws IOException {
        NavigatorController.activateUserManager();
        SceneSwitcherUtils.NavSceneController(event, mainBorderPane, "/com/auctionapp/auctionappjava/views/UsersManagerScreen.fxml");
    }

    @FXML
    void handleSignOut(ActionEvent event) throws IOException {

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        Runnable switchScene = () -> {
            try {
                // Xóa thông tin user trong session này
                UserSession.getInstance().cleanUserSession();
                AuctionSession.getInstance().cleanAuctionSession();
                RegisterController.isRegister = false;
                SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/MainScreen.fxml", "Blue88");
            } catch (IOException e) {
                throw new AppException("Không thể đăng xuất", e);
            }
        };

        AlertUtils.AnnouncementController(
                "Chắc chưa?",
                "Bạn có chắc muốn đăng xuất không?",
                switchScene,
                null);
    }


    @FXML
    void handleExit (ActionEvent event) throws IOException {

        stage = (Stage)((Node)event.getSource()).getScene().getWindow();

        Runnable closeStage = () -> {
            stage.close();
        };

        AlertUtils.AnnouncementController(
                "Xác nhận thoát",
                "Bạn có chắc chắn muốn thoát ứng dụng không?",
                closeStage,
                null);
    }

    private Button currentActiveButton = null;

    // Gọi nội bộ mỗi khi điều hướng trong Navigator
    private void setActiveButton(Button btn) {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("nav-menu-btn-active");
        }
        if (btn != null) {
            btn.getStyleClass().add("nav-menu-btn-active");
        }
        currentActiveButton = btn;
    }

    // Expose tĩnh để Dashboard gọi được
    public static void activateAccountButton() {
        if (instance != null) {
            instance.setActiveButton(instance.setting);
        }
    }

    public static void activateHistory() {
        if (instance != null) {
            instance.setActiveButton(instance.btnHistory);
        }
    }

    public static void activateItemListBidder() {
        if (instance != null) {
            instance.setActiveButton(instance.btnItemListBidder);
        }
    }

    public static void activateItemListSeller() {
        if (instance != null) {
            instance.setActiveButton(instance.btnItemListSeller);
        }
    }

    public static void activateTrend() {
        if (instance != null) {
            instance.setActiveButton(instance.btnTrend);
        }
    }

    public static void activateItemListAdmin() {
        if (instance != null) {
            instance.setActiveButton(instance.btnItemListAdmin);
        }
    }

    public static void activateDashboardButton() {
        if (instance != null) {
            instance.setActiveButton(instance.btnDashboard);
        }
    }

    public static void activateUserManager() {
        if (instance != null) {
            instance.setActiveButton(instance.btnGotoUsersManager);
        }
    }

    private record ChatbotAnswer(String question, String answer, List<String> keywords) {
    }
}
