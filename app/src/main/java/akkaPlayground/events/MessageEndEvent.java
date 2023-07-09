package akkaPlayground.events;

public record MessageEndEvent(EventType type, long timestamp, long id, String message, String ref) implements Event {}
