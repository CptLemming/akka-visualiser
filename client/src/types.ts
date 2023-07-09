export type ActorState = "STARTED" | "STOPPED";
export type EventType = "ACTOR_START" | "ACTOR_STOP" | "MESSAGE_BEGIN" | "MESSAGE_END";

export type Actor = {
  path: string,
  state: ActorState,
};

export type Message = {
  ref: string;
  sender: string;
  message: string;
  startTime?: number;
  endTime?: number;
  duration?: number;
};

export type MessagesGroup = {
  averageTime: number,
  events: Message[],
};

export type Event = {
  type: string;
  timestamp: number;
  path?: string;
  id?: number;
  message?: string;
  ref?: string;
  sender?: string;
};
