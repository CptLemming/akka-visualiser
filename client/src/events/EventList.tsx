import { type Component, Accessor, Setter } from "solid-js";

import styles from "../App.module.css";
import { Actor, Event, Message, MessagesGroup } from "../types";

interface Props {
  events: Accessor<Event[]>;
}

const EventList: Component<Props> = (props) => {
  return (
    <div class={styles.events}>
      <h2>Raw events ({props.events().length})</h2>

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
    </div>
  );
}

export default EventList;
