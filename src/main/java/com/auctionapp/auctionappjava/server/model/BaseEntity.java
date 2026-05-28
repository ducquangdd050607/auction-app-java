package com.auctionapp.auctionappjava.server.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public abstract class BaseEntity implements Serializable {
  private UUID id;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  protected BaseEntity() {
    this(UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now());
  }

  protected BaseEntity(UUID id, LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.id = id == null ? UUID.randomUUID() : id;
    this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    this.updatedAt = updatedAt == null ? LocalDateTime.now() : updatedAt;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public void touch() {
    this.updatedAt = LocalDateTime.now();
  }

  @Override
  public boolean equals(Object compare) {
    // Nếu là chính nó thì true
    if (this == compare) {
      return true;
    }

    // Nếu kphai cùng kiểu thì false
    if (!(compare instanceof BaseEntity)) {
      return false;
    } else {
      // Kiểm tra UUID 2 object, bằng nhau là true
      BaseEntity that = (BaseEntity) compare;
      return Objects.equals(this.id, that.id);
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
