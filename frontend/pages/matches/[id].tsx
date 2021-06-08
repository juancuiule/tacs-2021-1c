import { FormControl, InputLabel, MenuItem, Select } from "@material-ui/core";
import { Formik } from "formik";
import { useRouter } from "next/router";
import React, { useEffect, useRef, useState } from "react";
import Button from "../../src/components/Button";
import HeroCard from "../../src/components/HeroCard";
import { useAuth } from "../../src/contexts/AuthContext";
import api from "../../src/utils/api";
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
        state.nextToPlay == userId &&
        card && (
          <>
            <Formik
              initialValues={{
                key: "height",
              }}
              onSubmit={async ({ key }) => {
                ws.current.send(
                  JSON.stringify({
                    action: "battle",
                    payload: JSON.stringify({
                      key,
                    }),
                  })
                );
              }}
            >
              {({ values, handleSubmit, setFieldValue }) => (
                <form onSubmit={handleSubmit} id="battle-form">
                  <FormControl>
                    <InputLabel id="key-select-label">Atributo</InputLabel>
                    <Select
                      labelId="key-select-label"
                      id="key"
                      value={values.key}
                      onChange={(e) => {
                        const value = e.target.value;
                        setFieldValue("key", value);
                      }}
                    >
                      {Object.keys(card.stats).map((key) => (
                        <MenuItem value={key}>{key}</MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                  <button type="submit" form="battle-form">
                    Pelear
                  </button>
                  <button
                    onClick={() => {
                      ws.current.send(
                        JSON.stringify({
                          action: "withdraw",
                        })
                      );
                    }}
                  >
                    Abandonar
                  </button>
                </form>
              )}
            </Formik>
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
                {state.type === "preBattle" && card && <HeroCard hero={card} />}
              </div>
              <div>
                <div>
                  Cartas Local ({player1}): {state.player1Cards}{" "}
                </div>
                <div>Cartas Deck: {state.cardsInDeck} </div>
                <div>
                  Cartas Visitante ({player2}): {state.player2Cards}{" "}
                </div>
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
