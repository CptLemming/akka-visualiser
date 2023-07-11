import { createSignal, type Component, createMemo } from "solid-js";

import styles from "./App.module.css";
// import sourceData from "./sampleData.json";
import importData from "./stream.log.json";

import ActorList from "./actors/ActorList";
import { Event } from "./types";
import ControlsList from "./controls/ControlsList";
import MessagesList from "./messages/MessagesList";
import EventList from "./events/EventList";
import { getActorsFromEvents, getMessagesFromEvents, getSelectedActorMessages, groupMessagesBySender } from "./utils";

const App: Component = () => {
  const sourceData = importData as Event[];
  const numEvents = sourceData.length;
  const [selectedEvent, setSelectedEvent] = createSignal(numEvents - 1);
  const [selectedActor, setSelectedActor] = createSignal<string | null>(null);

  const events = createMemo(() => sourceData.slice(0, selectedEvent() + 1));
  const actors = createMemo(() => getActorsFromEvents(events()));
  const allMessages = createMemo(() => getMessagesFromEvents(events()));
  const messagesBySender = createMemo(() => groupMessagesBySender(allMessages()));
  const messages = createMemo(() => getSelectedActorMessages(selectedActor(), messagesBySender()));

  return (
    <div class={styles.App}>
      <ControlsList numEvents={numEvents} selectedEvent={selectedEvent} setSelectedEvent={setSelectedEvent} />
      <ActorList actors={actors} selectedActor={selectedActor} messagesBySender={messagesBySender} setSelectedActor={setSelectedActor} />
      <MessagesList messages={messages} />
      <EventList events={events} />
    </div>
  );
};

export default App;
