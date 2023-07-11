import { Actor, Event, Message, MessagesGroup } from "./types";

export const getActorsFromEvents = (events: Event[]) => {
  const results = new Map<
    string,
    Actor
  >();

  events.forEach((event) => {
    if (event.path) {
      const isStart = event.type === "ACTOR_START" || event.type === "ACTOR_MAILBOX_SIZE";
      const nextState = isStart ? "STARTED" : "STOPPED";

      const oldValue = results.get(event.path) ?? { path: event.path, state: nextState, mailbox: 0, maxMailbox: 0 };
      results.set(event.path, {
        ...oldValue,
        state: nextState,
      });

      if (event.size != null) {
        const oldValue = results.get(event.path);
        if (oldValue != null) {
          results.set(event.path, {
            ...oldValue,
            mailbox: event.size,
            maxMailbox: Math.max(oldValue.maxMailbox, event.size),
          });
        }
      }
    }
  });

  const actors = [...results.values()];
  actors.sort((a, b) => {
    return a.path.localeCompare(b.path);
  });
  return actors;
};

export const getMessagesFromEvents = (events: Event[]) => {
  const results = new Map<
    number,
    Message
  >();

  events
    .forEach((event) => {
      if (event.id != null) {
        let prev = results.get(event.id);

        if (prev == null) {
          prev = {
            ref: event.ref ?? "unk",
            sender: event.sender ?? "unk",
            message: event.message ?? "unk",
          };
        }

        if (event.type === "MESSAGE_BEGIN" && event.timestamp != null) {
          prev = { ...prev, startTime: event.timestamp };
        } else if (
          event.type === "MESSAGE_END" &&
          event.timestamp != null
        ) {
          prev = { ...prev, endTime: event.timestamp };
        }

        if (prev.startTime != null && prev.endTime != null) {
          prev.duration = (prev.endTime - prev.startTime) / 1000;
        }

        results.set(event.id, prev);
      }
    });

  return results;
};

export const getSelectedActorMessages = (selected: string | null, groupedMessages: Map<string, MessagesGroup>) => {
  if (selected != null) {
    return groupedMessages.get(selected ?? "")?.events;
  }

  return null;
};

export const groupMessagesBySender = (messages: Map<number, Message>) => {
  const results = new Map<string, MessagesGroup>();

  messages.forEach(event => {
    const result = results.get(event.ref) ?? { averageTime: 0, maxTime: 0, events: [] };
    result.events.push(event);
    results.set(event.ref, result);
  });

  results.forEach(entry => {
    if (entry.events.length > 0) {
      const totalTime = entry.events.reduce((total, event) => total + (event.duration ?? 0), 0);
      entry.averageTime = totalTime / entry.events.length;
      entry.maxTime = entry.events.reduce((maxTime, event) => Math.max(maxTime, event.duration ?? 0), 0);
    }
  });

  return results;
};