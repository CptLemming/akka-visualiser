package akkaPlayground.events;

public record MessageBeginEvent(EventType type, long timestamp, long id, String message, String ref, String sender) implements Event {}
