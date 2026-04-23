package com.auctionapp.auctionappjava.server.dao.jdbc;

import com.auctionapp.auctionappjava.common.model.Wallet;
import com.auctionapp.auctionappjava.server.dao.WalletDao;
import com.auctionapp.auctionappjava.server.db.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class JdbcWalletDao implements WalletDao {
    private final DatabaseManager databaseManager;
    public JdbcWalletDao(){ this(new DatabaseManager()); }
    public JdbcWalletDao(DatabaseManager databaseManager){ this.databaseManager=databaseManager; }
    @Override public void save(Wallet w){ String sql="INSERT INTO Wallet (id,user_id,balance,currency,status,created_at,updated_at) VALUES (?,?,?,'VND','active',?,?)"; try(Connection c=databaseManager.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){ ps.setString(1,w.getId().toString()); ps.setString(2,w.getUserId().toString()); ps.setBigDecimal(3,w.getBalance()); ps.setObject(4,w.getCreatedAt()); ps.setObject(5,w.getUpdatedAt()); ps.executeUpdate(); } catch(SQLException e){ throw new IllegalStateException("Không thể lưu ví",e); } }
    @Override public void update(Wallet w){ w.touch(); try(Connection c=databaseManager.getConnection(); PreparedStatement ps=c.prepareStatement("UPDATE Wallet SET balance=?, updated_at=? WHERE id=?")){ ps.setBigDecimal(1,w.getBalance()); ps.setObject(2,w.getUpdatedAt()); ps.setString(3,w.getId().toString()); ps.executeUpdate(); } catch(SQLException e){ throw new IllegalStateException("Không thể cập nhật ví",e); } }
    @Override public Optional<Wallet> findById(UUID id){ List<Wallet> list=many("SELECT * FROM Wallet WHERE id=?", id.toString()); return list.isEmpty()?Optional.empty():Optional.of(list.get(0)); }
    @Override public Optional<Wallet> findByUserId(UUID userId){ List<Wallet> list=many("SELECT * FROM Wallet WHERE user_id=? AND currency='VND'", userId.toString()); return list.isEmpty()?Optional.empty():Optional.of(list.get(0)); }
    @Override public List<Wallet> findAll(){ return many("SELECT * FROM Wallet ORDER BY created_at DESC"); }
    @Override public void updateBalance(UUID userId, BigDecimal newBalance){ try(Connection c=databaseManager.getConnection(); PreparedStatement ps=c.prepareStatement("UPDATE Wallet SET balance=?, updated_at=? WHERE user_id=? AND currency='VND'")){ ps.setBigDecimal(1,newBalance); ps.setObject(2, LocalDateTime.now()); ps.setString(3,userId.toString()); ps.executeUpdate(); } catch(SQLException e){ throw new IllegalStateException("Không thể cập nhật số dư",e); } }
    @Override public void deleteByUserId(UUID userId){ try(Connection c=databaseManager.getConnection(); PreparedStatement ps=c.prepareStatement("DELETE FROM Wallet WHERE user_id=?")){ ps.setString(1,userId.toString()); ps.executeUpdate(); } catch(SQLException e){ throw new IllegalStateException("Không thể xóa ví",e); } }
    private List<Wallet> many(String sql,Object... params){ try(Connection c=databaseManager.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){ for(int i=0;i<params.length;i++) ps.setObject(i+1,params[i]); try(ResultSet rs=ps.executeQuery()){ List<Wallet> list=new ArrayList<>(); while(rs.next()) list.add(map(rs)); return list; } } catch(SQLException e){ throw new IllegalStateException("Không thể tải ví",e); } }
    private Wallet map(ResultSet rs) throws SQLException { return new Wallet(JdbcSupport.uuid(rs,"id"), JdbcSupport.dateTime(rs,"created_at"), JdbcSupport.dateTime(rs,"updated_at"), JdbcSupport.uuid(rs,"user_id"), rs.getBigDecimal("balance")); }
}
