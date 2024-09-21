package dev.tanvx.wallet_service.kafka.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
  COMMAND("COMMAND"),
  EVENT("EVENT");

  private final String type;
}
