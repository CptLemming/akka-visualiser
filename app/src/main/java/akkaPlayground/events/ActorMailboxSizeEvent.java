package akkaPlayground.events;

public record ActorMailboxSizeEvent(EventType type, long timestamp, String path, int size) implements Event {}
