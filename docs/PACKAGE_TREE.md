# Package Tree

```text
├── src
│   ├── main
│   │   ├── java/com/auctionhub
│   │   │   ├── client
│   │   │   ├── common
│   │   │   └── server
│   │   └── resources
│   │       ├── client/fxml
│   │       ├── client/css
│   │       └── db
│   └── test/java/com/auctionhub/server/service
├── docs
├── .github/workflows
├── pom.xml
└── .gitignore
```

## Giải thích nhanh
- `client`: JavaFX application, controller UI, session, network client, chatbot.
- `common`: DTO, entity, enum, factory, strategy, observer, util dùng chung.
- `server`: cấu hình server, DAO JDBC, service business, socket handler, scheduler.
- `resources/client/fxml`: toàn bộ layout giao diện.
- `resources/client/css`: style dùng chung cho app.
- `resources/db`: schema H2 và MySQL.
- `test`: unit test cho bid validation, auto-bid, anti-sniping, lifecycle.
