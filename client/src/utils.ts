import { Actor, Event, Message, MessagesGroup } from "./types";

export const getActorsFromEvents = (events: Event[]) => {
  const results = new Map<
    string,
    Actor
  >();

  events.forEach((event) => {
    if (event.path) {
      const isStart = event.type === "ACTOR_START";
      const nextState = isStart ? "STARTED" : "STOPPED";
      if (!results.has(event.path)) {
        results.set(event.path, { path: event.path, state: nextState });
      } else {
        const oldValue = results.get(event.path);
        results.set(event.path, {
          ...oldValue,
          path: event.path,
          state: nextState,
        });
      }
    }
  });

  return [...results.values()];
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
    const result = results.get(event.ref) ?? { averageTime: 0, events: [] };
    result.events.push(event);
    results.set(event.ref, result);
  });

  results.forEach(entry => {
    if (entry.events.length > 0) {
      const totalTime = entry.events.reduce((total, event) => total + (event.duration ?? 0), 0);
      entry.averageTime = totalTime / entry.events.length;
    }
  });

  return results;
};