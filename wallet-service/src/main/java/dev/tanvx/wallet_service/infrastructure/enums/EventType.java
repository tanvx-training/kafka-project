package dev.tanvx.wallet_service.infrastructure.enums;

public enum EventType {
  COMMAND("COMMAND"),
  EVENT("EVENT");

  private final String type;

  EventType(String type) {
    this.type = type;
  }

  public String getType() {
    return this.type;
  }
}
