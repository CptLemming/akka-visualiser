package akkaPlayground.events;

public record ActorStartEvent(EventType type, long timestamp, String path) implements Event {}
