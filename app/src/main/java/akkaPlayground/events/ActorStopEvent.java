package akkaPlayground.events;

public record ActorStopEvent(EventType type, long timestamp, String path) implements Event {}
