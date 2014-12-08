package edu.snu.cms.remon.collector.parameter;

public enum Event {
  START(0),
  END(1);

  private final int value;

  Event(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
