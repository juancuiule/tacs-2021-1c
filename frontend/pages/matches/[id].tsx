import { useRouter } from "next/router";
import React, { useEffect, useRef, useState } from "react";
import { useAuth } from "../../src/contexts/AuthContext";
import api from "../../src/utils/api";
import { parseMatchState } from "../../src/utils/utils";
import { Hero, MatchData, MatchState } from "../../types";

export default function Match() {
  const router = useRouter();
  const { id } = router.query;

  const {
    authState: { auth, fetched, accessToken, id: userId },
  } = useAuth();

  useEffect(() => {
    if (!auth && fetched) {
      router.push("/auth/login");
    }
  }, [auth, fetched]);

  const ws = useRef<null | WebSocket>(null);

  const [matchState, setMatchState] = useState<MatchData | null>(null);

  const { player1, player2, state } = matchState || {};

  const player =
    matchState !== null
      ? userId == player1
        ? "player1"
        : userId == player2
        ? "player2"
        : undefined
      : undefined;

  const [card, setCard] = useState<Hero | null>(null);

  useEffect(() => {
    if (matchState !== null && state.type === "preBattle" && player) {
      try {
        const cardId = state[`${player}Card`];
        const fetchCards = async () => {
          const res = await api.GET<Hero>(`/cards/${cardId}`);
          console.log(res);
          setCard(res);
        };
        fetchCards();
      } catch (e) {}
    }
  }, [matchState]);

  useEffect(() => {
    const url = `ws://localhost:8080/rooms/${id}/room?access-token=${accessToken}`;
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
        const matchData: MatchData = JSON.parse(evt.data);
        setMatchState(matchData);
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
        state.type !== "finished" &&
        state.type !== "draw" &&
        state.nextToPlay == userId && (
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
          {state.type !== "finished" && state.type !== "draw" ? (
            <>
              <div>
                {state.type === "preBattle" && card && (
                  <div>
                    <img src={card.image} width="100px" height="100px" />
                  </div>
                )}
                <div>Cartas L: {state.player1Cards} </div>
              </div>
              <div>Cartas Deck: {state.cardsInDeck} </div>
              <div>
                <div style={{ width: "100px" }}></div>
                <div>Cartas V: {state.player2Cards} </div>
              </div>
            </>
          ) : (
            <>
              {state.type === "finished" && <>Ganador: {state.winner}</>}
              {state.type === "draw" && <>Empate</>}
            </>
          )}
        </div>
      )}
      <pre>
        <code>{JSON.stringify(matchState, null, 2)}</code>
      </pre>
    </>
  );
}
