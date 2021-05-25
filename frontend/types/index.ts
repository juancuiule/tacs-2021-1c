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
