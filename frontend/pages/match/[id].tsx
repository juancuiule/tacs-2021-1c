import { useRouter } from "next/router";
import React, { useEffect, useRef, useState } from "react";
import api from "../../src/utils/api";
import { Hero } from "../../types";

type Action = Withdraw | Battle | NoOp | InitMatch | DealCards;

type Withdraw = { Withdraw: { loser: number } };
type Battle = { Battle: { cardAttribute: string } };
type NoOp = { NoOp: {} };
type InitMatch = { InitMatch: {} };
type DealCards = { DealCards: {} };

type MatchState = BattleResult | PreBattle | Finished | Draw;

type PlayingBase = {
  cardsInDeck: number[];
  player1Cards: number[];
  player2Cards: number[];
};
type BattleResult = { BattleResult: {} & PlayingBase };
type PreBattle = {
  PreBattle: { player1Card: number; player2Card: number } & PlayingBase;
};
type Finished = { Finished: { winner: string } };
type Draw = { Draw: PlayingBase };

type Step = [Action, MatchState];

type ParsedState =
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
  | ({ state: "finished" } & PlayingBase);

const parseMatchState = (matchState: MatchState): ParsedState => {
  if (matchState["BattleResult"]) {
    return { state: "battleResult", ...matchState["BattleResult"] };
  } else if (matchState["PreBattle"]) {
    return { state: "preBattle", ...matchState["PreBattle"] };
  } else if (matchState["Draw"]) {
    return { state: "draw", ...matchState["Draw"] };
  } else if (matchState["Finished"]) {
    return { state: "finished", ...matchState["Finished"] };
  }
};

type MatchData = {
  matchId: string;
  deck: string;
  player1: string;
  player2: string;
  steps: Step[];
  state: ParsedState;
};

export default function Match() {
  const router = useRouter();
  const { id, accessToken, player = "player1" } = router.query;

  const ws = useRef<null | WebSocket>(null);

  const [matchState, setMatchState] = useState<MatchData | null>(null);

  const [card, setCard] = useState<Hero | null>(null);
  useEffect(() => {
    if (matchState !== null && matchState.state.state === "preBattle") {
      console.log("ALGO");
      try {
        const cardId = matchState.state.player1Card;
        const fetchCards = async () => {
          const res = await api.GET<Hero>(`/cards/${cardId}`);
          console.log(res);
          setCard(res);
        };
        fetchCards();
      } catch (e) {
        console.log(3);
      }
    }
  }, [matchState]);

  useEffect(() => {
    const url = `ws://localhost:8080/matches/${id}/room?access-token=${accessToken}`;
    ws.current = new WebSocket(url);
    ws.current.onopen = (evt) => {
      console.log("Connection established");
      ws.current.send(
        JSON.stringify({
          action: "getMatch",
        })
      );
    };

    ws.current.onclose = () => {
      console.log("Disconnected from server");
    };

    ws.current.onmessage = (evt) => {
      if (evt.data !== "") {
        const { steps, ...d } = JSON.parse(evt.data);
        console.log(steps);
        setMatchState({
          ...d,
          state: parseMatchState(steps[steps.length - 1][1]),
        });
      }
    };

    ws.current.onerror = (evt) => {
      console.log(
        "There was a communications error, check the console for details"
      );
    };
  }, [id, accessToken]);

  return (
    <>
      Match {id}
      {ws.current &&
        matchState !== null &&
        matchState.state.state !== "finished" &&
        matchState.state.state !== "draw" &&
        matchState.state.nextToPlay ===
          matchState[player as "player1" | "player2"] && (
          <>
            <button
              onClick={() => {
                ws.current.send(
                  JSON.stringify({
                    action: "battle",
                    payload: JSON.stringify({
                      key: "height",
                    }),
                  })
                );
              }}
            >
              Pelear
            </button>
          </>
        )}
      {matchState !== null && (
        <div
          style={{
            maxWidth: "1200px",
            padding: "24px",
            margin: "20px auto",
            display: "flex",
            flexDirection: "row",
            justifyContent: "space-between",
          }}
        >
          {matchState.state.state !== "finished" &&
          matchState.state.state !== "draw" ? (
            <>
              <div>
                {matchState.state.state === "preBattle" && card && (
                  <div>
                    <img src={card.image} width="100px" height="100px" />
                  </div>
                )}
                <div>Cartas L: {matchState.state.player1Cards.length} </div>
              </div>
              <div>Cartas Deck: {matchState.state.cardsInDeck.length} </div>
              <div>
                <div style={{ width: "100px" }}></div>
                <div>Cartas V: {matchState.state.player2Cards.length} </div>
              </div>
            </>
          ) : (
            <></>
          )}
        </div>
      )}
      <pre>
        <code>{JSON.stringify(matchState, null, 2)}</code>
      </pre>
    </>
  );
}
