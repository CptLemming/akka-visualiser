import { type Component, Accessor, Setter, For, createMemo } from "solid-js";

import styles from "../App.module.css";
import { Actor, Event, MessagesGroup } from "../types";

interface Props {
  numEvents: number;
  selectedEvent: Accessor<number>;
  setSelectedEvent: Setter<number>;
}

const ControlsList: Component<Props> = (props) => {
  const controls = createMemo(() => {
    return new Array(props.numEvents).fill(null).map((_, index) => index);
  });
  return (
    <div class={styles.controls}>
      <div class={styles.controls__inner}>
        <For each={controls()}>{(event, index) => (
          <button
            class={styles.controls__control}
            onClick={() => props.setSelectedEvent(index)}
            data-selected={index() === props.selectedEvent()}
          >
            {index() + 1}
          </button>
        )}</For>
      </div>
    </div>
  );
}

export default ControlsList;
