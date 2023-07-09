package akkaPlayground.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;

import akkaPlayground.events.Event;

public class EventCollector {
  private ConcurrentLinkedQueue<Event> pendingEvents = new ConcurrentLinkedQueue<>();

  public static EventCollector INSTANCE = new EventCollector();

  public static EventCollector get() {
    return INSTANCE;
  }

  public EventCollector() {
    new Timer().scheduleAtFixedRate(new EventThread(this), 0, 1000);
  }

  public void log(Event event) {
    pendingEvents.add(event);
  }

  public List<Event> takeEvents() {
    final List<Event> events = new ArrayList<>();

    int size = pendingEvents.size();

    for (int i = 0; i < size; i++) {
      Event event = pendingEvents.poll();

      if (event != null) {
        events.add(event);
      }
    }

    return events;
  }
}
