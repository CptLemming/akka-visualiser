package akkaPlayground;

import java.time.Duration;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.MailboxSelector;
import akka.actor.typed.javadsl.Behaviors;

public class RootActor {
  public static interface Command {}
  public static record StopSpeaker() implements Command {}
  public static record StopSpeaking() implements Command {}

  public static Behavior<Command> create() {
    return Behaviors.withTimers(timers -> Behaviors.setup(context -> {

      ActorRef<Speaker.Command> speaker1 = context.spawn(Speaker.create(), "speaker-1", MailboxSelector.fromConfig("my-app.my-special-mailbox"));
      context.spawn(Speaker.create(), "speaker-2", MailboxSelector.fromConfig("my-app.my-special-mailbox"));
      context.spawn(Speaker.create(), "speaker-3", MailboxSelector.fromConfig("my-app.my-special-mailbox"));
      ActorRef<Speaker.Command> speaker4 = context.spawn(Speaker.create(), "speaker-4", MailboxSelector.fromConfig("my-app.my-special-mailbox"));

      String actorTree = context.getSystem().printTree();

      System.out.println(actorTree);

      timers.startTimerAtFixedRate("SPEAK", new Speaker.Speak("HelloWorld"), Duration.ofMillis(250));
      timers.startSingleTimer(new StopSpeaker(), Duration.ofSeconds(3));
      timers.startSingleTimer(new StopSpeaking(), Duration.ofSeconds(10));

      return Behaviors.receive(Command.class)
        .onMessage(Speaker.Speak.class, message -> {
          speaker1.tell(new Speaker.Speak("HelloWorld1"));
          speaker1.tell(new Speaker.Speak("HelloWorld2"));
          speaker1.tell(new Speaker.Speak("HelloWorld3"));
          speaker1.tell(new Speaker.Speak("HelloWorld4"));
          return Behaviors.same();
        })
        .onMessage(StopSpeaking.class, message -> {
          timers.cancel("SPEAK");
          return Behaviors.same();
        })
        .onMessage(StopSpeaker.class, message -> {
          speaker4.tell(new Speaker.Stop());
          return Behaviors.same();
        })
      .build();
    }));
  }
}
