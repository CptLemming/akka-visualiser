import { type Component, Accessor, Setter, createSignal, Show } from "solid-js";

import styles from "../App.module.css";
import { Actor, Event, Message, MessagesGroup } from "../types";

interface Props {
  events: Accessor<Event[]>;
}

const EventList: Component<Props> = (props) => {
  const [showEvents, setShowEvents] = createSignal(false);
  return (
    <div class={styles.events}>
      <h2>Raw events ({props.events().length})</h2>
      <button onClick={() => setShowEvents(!showEvents())}>Toggle</button>

      <Show when={showEvents()}>
        <div class={styles.events__list}>
          {props.events().map((event) => (
            <div class={styles.events__list_item}>
              <p>Event: {event.type}</p>
              <p>Time: {event.timestamp}</p>

              {/* Actor event */}
              {event.path && <p>Path: {event.path}</p>}

              {/* Message event */}
              {event.id && <p>Memory ref: {event.id}</p>}
            </div>
          ))}
        </div>
      </Show>
    </div>
  );
}

export default EventList;
