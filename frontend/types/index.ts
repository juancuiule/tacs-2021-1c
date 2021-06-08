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

export type Action = Withdraw | Battle | NoOp | InitMatch | DealCards;

export type Withdraw = { Withdraw: { loser: number } };
export type Battle = { Battle: { cardAttribute: string } };
export type NoOp = { NoOp: {} };
export type InitMatch = { InitMatch: {} };
export type DealCards = { DealCards: {} };

export type MatchState = BattleResult | PreBattle | Finished | Draw;

export type PlayingBase = {
  cardsInDeck: number[];
  player1Cards: number[];
  player2Cards: number[];
};
export type BattleResult = { BattleResult: {} & PlayingBase };
export type PreBattle = {
  PreBattle: { player1Card: number; player2Card: number } & PlayingBase;
};
export type Finished = { Finished: { winner: string } };
export type Draw = { Draw: PlayingBase };

export type Step = [Action, MatchState];

export type MatchData = {
  matchId: string;
  deck: string;
  player1: string;
  player2: string;
  steps: Step[];
  state: ParsedState;
};

export type ParsedState =
  | ({
      nextToPlay: string;
      state: "battleResult";
    } & PlayingBase)
  | ({
      nextToPlay: string;
      state: "preBattle";
      player1Card: number;
      player2Card: number;
    } & PlayingBase)
  | ({ state: "draw" } & PlayingBase)
  | ({ state: "finished", winner: string } & PlayingBase);
