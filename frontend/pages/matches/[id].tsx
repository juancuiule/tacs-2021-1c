import { useRouter } from "next/router";
import React, { useEffect, useRef, useState } from "react";
import { useAuth } from "../../src/contexts/AuthContext";
import api from "../../src/utils/api";
import { parseMatchState } from "../../src/utils/utils";
import { Hero, MatchData } from "../../types";

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

  const player =
    matchState !== null
      ? userId == matchState.player1
        ? "player1"
        : userId == matchState.player2
        ? "player2"
        : undefined
      : null;

  const [card, setCard] = useState<Hero | null>(null);
  useEffect(() => {
    if (
      matchState !== null &&
      matchState.state.state === "preBattle" &&
      player
    ) {
      try {
        const cardId = matchState.state[`${player}Card`];
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
        matchState.state.nextToPlay == userId && (
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
            <>
              {matchState.state.state === "finished" && (
                <>Winner {matchState.state.winner}</>
              )}
              {matchState.state.state === "draw" && <>Draw</>}
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
