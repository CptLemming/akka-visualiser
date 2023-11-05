package akkaPlayground;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.dispatch.Envelope;
import akka.dispatch.MailboxType;
import akka.dispatch.MessageQueue;
import akka.dispatch.ProducesMessageQueue;
import akkaPlayground.collector.EventCollector;
import akkaPlayground.events.*;
import kamon.Kamon;
import kamon.metric.RangeSampler;
import kamon.metric.Timer.Started;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import scala.Option;
import org.openjdk.jol.vm.VM;

public class HookMailbox implements MailboxType, ProducesMessageQueue<HookMailbox.MyMessageQueue> {

  public static class MyMessageQueue implements MessageQueue, MyUnboundedMessageQueueSemantics {
    private final Queue<Envelope> queue = new ConcurrentLinkedQueue<Envelope>();
    private final Option<ActorRef> owner;
    private final RangeSampler mailboxSize;
    private final Started actorLifetime;
    private Long startTime = null;
    private Long prevMessageId = null;
    private String prevMessageClassName = null;

    public MyMessageQueue(Option<ActorRef> owner) {
      this.owner = owner;

      String actorPath = owner.map(ref -> ref.path().toString()).getOrElse(() -> "user/unknown");

      // Kamon.spanBuilder("actor.typed.started").tag("actor", actorPath).start().finish();
      this.actorLifetime = Kamon.timer("actor.typed.lifetime").withTag("actor", actorPath).start();
      this.mailboxSize = Kamon.rangeSampler("actor.typed.mailbox-size").withTag("actor", actorPath);

      if (!owner.isEmpty()) {
        EventCollector.get().log(new ActorStartEvent(
          EventType.ACTOR_START,
          System.nanoTime(),
          owner.get().path().toString()
        ));
      }
    }

    @Override
    public void enqueue(ActorRef receiver, Envelope handle) {
      // System.out.println("HookMailbox :: enqueue to -> "+ owner.map(ref -> ref.path()));
      // System.out.println("HookMailbox :: enqueue -> "+ receiver.path() + " = "+ handle.message());
      // System.out.println("HookMailbox :: enqueue from "+ handle.sender().path() + " to "+ receiver.path());
      // System.out.println("HookMailbox :: enqueue -> "+ VM.current().addressOf(handle.message()));
      mailboxSize.increment();
      queue.offer(handle);
    }

    @Override
    public Envelope dequeue() {
      int numMessages = queue.size();

      if (owner.isDefined()) {
        // System.out.println(owner.get().path() + " -> " + numMessages);
        EventCollector
          .get()
          .log(
            new ActorMailboxSizeEvent(
              EventType.ACTOR_MAILBOX_SIZE,
              System.nanoTime(),
              owner.get().path().toString(),
              numMessages
            )
          );
      }
      
      // System.out.println("HookMailbox :: dequeue");
      Envelope maybe = queue.poll();

      calcTimer();

      if (maybe != null && !owner.isEmpty()) {
        mailboxSize.decrement();
        long id = VM.current().addressOf(maybe.message());
        // System.out.println("HookMailbox :: dequeue -> "+ VM.current().addressOf(maybe.message()));
        startTimer(id, maybe.message().getClass().getName(), maybe.sender(), owner.get());
      }

      return maybe;
    }

    @Override
    public int numberOfMessages() {
      // System.out.println("HookMailbox :: numberOfMessages -> "+ queue.size());
      return queue.size();
    }

    @Override
    public boolean hasMessages() {
      // System.out.println("HookMailbox :: hasMessages -> "+ !queue.isEmpty());

      if (queue.isEmpty()) {
        resetTimer();
      }

      return !queue.isEmpty();
    }

    @Override
    public void cleanUp(ActorRef owner, MessageQueue deadLetters) {
      // System.out.println("HookMailbox :: cleanUp -> "+ owner.path());

      // Kamon.spanBuilder("actor.typed.stopped").tag("actor", owner.path().toString()).start().finish();
      actorLifetime.stop();

      calcTimer(); // Actor was stopped

      EventCollector.get().log(new ActorStopEvent(
          EventType.ACTOR_STOP,
          System.nanoTime(),
          owner.path().toString()
        ));

      while (!queue.isEmpty()) {
        deadLetters.enqueue(owner, dequeue());
      }
    }

    private void startTimer(long id, String message, ActorRef ref, ActorRef sender) {
      this.prevMessageClassName = message;
      this.prevMessageId = id;
      this.startTime = System.nanoTime();

      EventCollector.get().log(new MessageBeginEvent(
          EventType.MESSAGE_BEGIN,
          startTime,
          id,
          message,
          sender.path().toString(),
          ref.path().toString()
        ));
    }

    private void calcTimer() {
      if (startTime != null && prevMessageId != null) {
        long startedTime = startTime;
        long endTime = System.nanoTime();

        long diff_us = (endTime - startedTime) / 1000;

        // System.out.println("[TIMER] Message took "+ diff_us + "us to process");
        if (!owner.isEmpty()) {
          EventCollector.get().log(new MessageEndEvent(
            EventType.MESSAGE_END,
            endTime,
            prevMessageId,
            prevMessageClassName,
            owner.get().path().toString()
          ));
        }
      }
    }

    private void resetTimer() {
      this.prevMessageId = null;
      this.prevMessageClassName = null;
      this.startTime = null;
    }
  }

  // This constructor signature must exist, it will be called by Akka
  public HookMailbox(ActorSystem.Settings settings, Config config) {
    // System.out.println("HookMailbox :: init -> "+ config);
  }

  // The create method is called to create the MessageQueue
  public MessageQueue create(Option<ActorRef> owner, Option<ActorSystem> system) {
    // System.out.println("HookMailbox :: create -> "+ owner.map(ref -> ref.path()));

    return new MyMessageQueue(owner);
  }

  // Marker interface used for mailbox requirements mapping
  public static interface MyUnboundedMessageQueueSemantics {}
}
