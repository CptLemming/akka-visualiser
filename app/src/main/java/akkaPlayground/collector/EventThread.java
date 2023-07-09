package akkaPlayground.collector;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import akkaPlayground.events.Event;

public class EventThread extends TimerTask {
  private Logger logger = LoggerFactory.getLogger("events");
  private String logFilename = "/stream.log";
  private final EventCollector collector;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private FileOutputStream outputStream;

  public EventThread(EventCollector collector) {
    this.collector = collector;

    Path source = Paths.get(this.getClass().getResource("/").getPath());
    Path eventsFolder = Paths.get(source.toAbsolutePath() + "/events");
    try {
      Files.createDirectories(eventsFolder);
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      String filename = eventsFolder.toAbsolutePath() + logFilename;

      logger.info("Writing to file -> "+ filename);

      outputStream = new FileOutputStream(filename);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    
    final List<Event> events = collector.takeEvents();
    logger.debug("Threading -> "+ events.size());

    events.stream().forEach(this::saveEvent);

    try {
      TimeUnit.SECONDS.sleep(2);
    } catch (InterruptedException e) {
      // ?
    }
  }

  private void saveEvent(Event event) {
    if (outputStream == null) return;

    String result;
    try {
      result = objectMapper.writeValueAsString(event);

      // TODO output a "processed" log instead of raw events
      outputStream.write((result + "\n").getBytes());
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
