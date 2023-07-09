import { type Component, Accessor, Setter, Show } from "solid-js";

import styles from "../App.module.css";
import { Actor, Event, Message, MessagesGroup } from "../types";

interface Props {
  messages: Accessor<Message[] | null | undefined>;
}

const MessagesList: Component<Props> = (props) => {
  return (
    <Show when={props.messages() != null}>
      <div class={styles.messages}>
        <h3>Messages ({props.messages()?.length})</h3>

        <div class={styles.messages__list}>
          {props.messages()?.map((message) => (
            <div class={styles.messages__list_item}>
              <p>Message: {message.message}</p>
              <p>From: {message.sender}</p>
              <p>To: {message.ref}</p>
              {/* <p>Start: {message.startTime}</p>
              <p>End: {message.endTime}</p> */}
              <p>Duration: {message.duration?.toFixed(2)}us</p>
            </div>
          ))}
        </div>
      </div>
    </Show>
  );
};

export default MessagesList;
