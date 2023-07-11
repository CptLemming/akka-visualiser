import { type Component, Accessor, Setter } from "solid-js";

import styles from "../App.module.css";
import { Actor, MessagesGroup } from "../types";

interface Props {
  actors: Accessor<Actor[]>;
  selectedActor: Accessor<string | null>;
  messagesBySender: Accessor<Map<string, MessagesGroup>>;
  setSelectedActor: Setter<string | null>;
}

const ActorList: Component<Props> = (props) => {
  return (
    <div class={styles.actors}>
      <h2>Actors</h2>

      <div class={styles.actors__list}>
        {props.actors().map((actor) => (
          <div class={styles.actors__list_item}>
            <button
              class={styles.actors__list_item__select}
              onClick={() => props.setSelectedActor(actor.path)}
              data-selected={actor.path === props.selectedActor()}
              data-state={actor.state}
            />
            <p>{actor.path} ({props.messagesBySender().get(actor.path)?.events.length ?? 0} @ {(props.messagesBySender().get(actor.path)?.averageTime ?? 0).toFixed(2)}us, {(props.messagesBySender().get(actor.path)?.maxTime ?? 0).toFixed(2)}us)</p>
            <p>MB: {actor.mailbox} MAX: {actor.maxMailbox}</p>
          </div>
        ))}
      </div>

      <button
        class={styles.actors__clear}
        onClick={() => props.setSelectedActor(null)}
        disabled={props.selectedActor() == null}
      >
        Clear
      </button>
    </div>
  );
}

export default ActorList;
