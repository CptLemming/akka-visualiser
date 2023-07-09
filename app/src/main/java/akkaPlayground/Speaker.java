package akkaPlayground;

import akka.actor.ActorPath;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

public class Speaker {
  public static interface Command {}

  public static record Speak(String words) implements Command, RootActor.Command {}

  public static record Stop() implements Command {}

  public static Behavior<Command> create() {
    return Behaviors.withTimers(timers -> Behaviors.setup(context -> {

      return Behaviors.receive(Command.class)
      .onMessage(Speak.class, message -> {
        // Thread.sleep(20);
        ActorPath path = context.getSelf().path();
        // context.getLog().info("HelloWorld({}) -> {}", path, message.words());
        return Behaviors.same();
      })
      .onMessage(Stop.class, ignore -> Behaviors.stopped())
      .build();
    }));
  }
}
