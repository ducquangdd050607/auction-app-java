package com.auctionapp.auctionappjava.server.dao.jdbc;

import com.auctionapp.auctionappjava.common.enums.ItemType;
import com.auctionapp.auctionappjava.common.exception.DatabaseException;
import com.auctionapp.auctionappjava.server.dao.AuctionItemDao;
import com.auctionapp.auctionappjava.server.factory.AuctionItemFactory;
import com.auctionapp.auctionappjava.server.model.Item;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcAuctionItemDao extends JdbcDaoSupport implements AuctionItemDao {
  @Override
  public Item save(Item item) {
    String sql =
        """
                INSERT INTO auction_items (
                    id, seller_id, title, description, starting_price, item_type,
                    attribute_one, attribute_two,image_data, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    seller_id = VALUES(seller_id), title = VALUES(title), description = VALUES(description),
                    starting_price = VALUES(starting_price), item_type = VALUES(item_type),
                    attribute_one = VALUES(attribute_one), attribute_two = VALUES(attribute_two),
                    image_data = VALUES(image_data),
                    updated_at = VALUES(updated_at)
                """;
    try (Connection connection = connection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, uuid(item.getId()));
      statement.setString(2, uuid(item.getSellerId()));
      statement.setString(3, item.getTitle());
      statement.setString(4, item.getDescription());
      statement.setBigDecimal(5, item.getStartingPrice());
      statement.setString(6, item.getItemType().name());
      statement.setString(7, item.getAttributeOne());
      statement.setString(8, item.getAttributeTwo());
      statement.setBytes(9, item.getImageData());
      statement.setTimestamp(10, timestamp(item.getCreatedAt()));
      statement.setTimestamp(11, timestamp(item.getUpdatedAt()));
      statement.executeUpdate();
      return item;
    } catch (SQLException exception) {
      throw new DatabaseException("Khong luu duoc item", exception);
    }
  }

  @Override
  public Optional<Item> findById(UUID itemId) {
    String sql = "SELECT * FROM auction_items WHERE id = ?";
    try (Connection connection = connection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, uuid(itemId));
      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next() ? Optional.of(mapItem(resultSet)) : Optional.empty();
      }
    } catch (SQLException exception) {
      throw new DatabaseException("Khong doc duoc item", exception);
    }
  }

  @Override
  public Optional<Item> findByIdWithoutImage(UUID itemId) {
    String sql =
        """
                SELECT id, seller_id, title, description, starting_price, item_type,
                       attribute_one, attribute_two, created_at, updated_at
                FROM auction_items
                WHERE id = ?
                """;
    try (Connection connection = connection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, uuid(itemId));
      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next() ? Optional.of(mapItemWithoutImage(resultSet)) : Optional.empty();
      }
    } catch (SQLException exception) {
      throw new DatabaseException("Khong doc duoc item", exception);
    }
  }

  @Override
  public Optional<byte[]> findImageByAuctionId(UUID auctionId) {
    String sql =
        """
                SELECT i.image_data
                FROM auction_items i
                JOIN auctions a ON a.item_id = i.id
                WHERE a.id = ?
                """;
    try (Connection connection = connection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setString(1, uuid(auctionId));

      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          byte[] imageData = resultSet.getBytes("image_data");

          if (imageData == null || imageData.length == 0) {
            return Optional.empty();
          }

          return Optional.of(imageData);
        }
        return Optional.empty();
      }
    } catch (SQLException exception) {
      throw new DatabaseException(
          "Khong doc duoc anh san pham cua auction: " + auctionId, exception);
    }
  }

  @Override
  public List<Item> findBySellerId(UUID sellerId) {
    String sql = "SELECT * FROM auction_items WHERE seller_id = ? ORDER BY created_at";
    try (Connection connection = connection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, uuid(sellerId));
      try (ResultSet resultSet = statement.executeQuery()) {
        List<Item> items = new ArrayList<>();
        while (resultSet.next()) {
          items.add(mapItem(resultSet));
        }
        return items;
      }
    } catch (SQLException exception) {
      throw new DatabaseException("Khong doc duoc danh sach item", exception);
    }
  }

  @Override
  public Optional<Item> findByAuctionId(UUID auctionId) {
    // i.* lấy toàn bộ cột của auction_items
    // JOIN dựa trên việc i.id trùng với a.item_id của bảng auctions
    String sql =
        """
            SELECT i.*
            FROM auction_items i
            JOIN auctions a ON i.id = a.item_id
            WHERE a.id = ?
            """;

    try (Connection connection = connection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setString(1, uuid(auctionId)); // Chuyển UUID sang String để query

      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          // mapAuctionItem là hàm dùng để chuyển ResultSet thành Object AuctionItem
          return Optional.of(mapItem(resultSet));
        }
        return Optional.empty();
      }
    } catch (SQLException exception) {
      throw new DatabaseException("Lỗi khi tìm Item từ Auction ID: " + auctionId, exception);
    }
  }

  @Override
  public Optional<Item> findByAuctionIdWithoutImage(UUID auctionId) {
    String sql =
        """
                SELECT i.id, i.seller_id, i.title, i.description, i.starting_price,
                       i.item_type, i.attribute_one, i.attribute_two,
                       i.created_at, i.updated_at
                FROM auction_items i
                JOIN auctions a ON i.id = a.item_id
                WHERE a.id = ?
                """;

    try (Connection connection = connection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setString(1, uuid(auctionId));

      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next() ? Optional.of(mapItemWithoutImage(resultSet)) : Optional.empty();
      }
    } catch (SQLException exception) {
      throw new DatabaseException(
          "Loi khi tim Item khong kem anh tu Auction ID: " + auctionId, exception);
    }
  }

  @Override
  public void deleteById(UUID itemId) {
    String sql = "DELETE FROM auction_items WHERE id = ?";
    try (Connection connection = connection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, uuid(itemId));
      statement.executeUpdate();
    } catch (SQLException exception) {
      throw new DatabaseException("Khong xoa duoc item", exception);
    }
  }

  @Override
  public void nookzzAll() {
    String sql = "DELETE FROM auction_items";
    try (Connection connection = connection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.executeUpdate();
    } catch (SQLException exception) {
      throw new DatabaseException("Khong xoa duoc item", exception);
    }
  }

  private Item mapItem(ResultSet resultSet) throws SQLException {
    return AuctionItemFactory.create(
        ItemType.valueOf(resultSet.getString("item_type")),
        uuid(resultSet.getString("id")),
        localDateTime(resultSet.getTimestamp("created_at")),
        localDateTime(resultSet.getTimestamp("updated_at")),
        uuid(resultSet.getString("seller_id")),
        resultSet.getString("title"),
        resultSet.getString("description"),
        resultSet.getBigDecimal("starting_price"),
        resultSet.getString("attribute_one"),
        resultSet.getString("attribute_two"),
        resultSet.getBytes("image_data"));
  }

  private Item mapItemWithoutImage(ResultSet resultSet) throws SQLException {
    return AuctionItemFactory.create(
        ItemType.valueOf(resultSet.getString("item_type")),
        uuid(resultSet.getString("id")),
        localDateTime(resultSet.getTimestamp("created_at")),
        localDateTime(resultSet.getTimestamp("updated_at")),
        uuid(resultSet.getString("seller_id")),
        resultSet.getString("title"),
        resultSet.getString("description"),
        resultSet.getBigDecimal("starting_price"),
        resultSet.getString("attribute_one"),
        resultSet.getString("attribute_two"),
        null);
  }
}
