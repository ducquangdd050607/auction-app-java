package com.auctionapp.auctionappjava.common.util;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class NotificationUtils {
  // Hàm Factory tạo ra một dòng thông báo (HBox)
  public static HBox createNotificationItem(
      String message, String notiType, Runnable onClickAction) {
    HBox notiItem = new HBox();
    notiItem.setSpacing(12);
    notiItem.getStyleClass().add("noti-item");

    // Giao diện mặc định
    String icon = "🔔";
    String iconStyle =
        "-fx-font-size: 14; -fx-padding: 8; -fx-background-radius: 50; -fx-alignment: center; -fx-pref-width: 32; -fx-pref-height: 32; ";

    // Tùy biến Icon và Màu sắc theo từng loại thông báo
    if (notiType != null) {
      switch (notiType) {
        case "WALLET":
          icon = "💰";
          iconStyle +=
              "-fx-background-color: #E8F5E9; -fx-text-fill: #4CAF50;"; // Nền xanh lá nhạt, chữ
          // xanh lá
          break;
        case "OUTBID":
          icon = "⚠";
          iconStyle +=
              "-fx-background-color: #FFEBEE; -fx-text-fill: #F44336;"; // Nền đỏ nhạt, chữ đỏ
          break;
        case "SELLER_BID":
          icon = "🔥";
          iconStyle +=
              "-fx-background-color: #FFF8E1; -fx-text-fill: #FF9800;"; // Nền cam nhạt, chữ cam
          break;
        case "BID_SUCCESS":
          icon = "✅";
          iconStyle += "-fx-background-color: #E3F2FD; -fx-text-fill: #1976D2;"; // Xanh dương
          break;
        case "WELCOME":
          icon = "🎉";
          iconStyle += "-fx-background-color: #F3E5F5; -fx-text-fill: #9C27B0;"; // Tím nhạt mộng mơ
          break;
        default:
          iconStyle +=
              "-fx-background-color: #E7F3FF; -fx-text-fill: #1877F2;"; // Xanh dương mặc định
          break;
      }
    }

    Label iconLabel = new Label(icon);
    iconLabel.setStyle(iconStyle);

    Label txtMessage = new Label(message);
    txtMessage.setWrapText(true);
    txtMessage.setMaxWidth(260);
    txtMessage.setStyle("-fx-font-size: 13; -fx-text-fill: #050505; -fx-line-spacing: 1.15;");

    notiItem.getChildren().addAll(iconLabel, txtMessage);

    // Nếu thông báo này cho phép click (onClickAction != null) thì mới gắn sự kiện
    if (onClickAction != null) {
      notiItem.setStyle("-fx-cursor: hand;");
      notiItem.setOnMouseClicked(e -> onClickAction.run());
    }

    return notiItem;
  }
}
