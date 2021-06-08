export interface IDeck {
  id: number;
  name: string;
  cards: string;
}

export type Hero = {
  id: number;
  name: string;
  stats: {
    height: number;
    weight: number;
    intelligence: number;
    speed: number;
    power: number;
    combat: number;
    strength: number;
  };
  image: string;
  biography?: { fullName: string; publisher: string };
};

export type Deck = {
  id: number;
  name: string;
  cards: number[];
};

export type MatchState =
  | DrawState
  | FinishedState
  | PreBattleState
  | BattleResultState;

export type DrawState = {
  type: "draw";
};

export type FinishedState = {
  type: "finished";
  winner: string;
};

export type PreBattleState = {
  type: "preBattle";
  nextToPlay: string;
  player1Card: string;
  player2Card: string;
  cardsInDeck: number;
  player1Cards: number;
  player2Cards: number;
};

export type BattleResultState = {
  type: "battleResult";
  nextToPlay: string;
  cardsInDeck: number;
  player1Cards: number;
  player2Cards: number;
};

export type MatchData = {
  deck: number;
  matchId: string;
  player1: string;
  player2: string;
  state: MatchState;
};
