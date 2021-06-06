import { useRouter } from "next/router";
import React, { useEffect, useRef, useState } from "react";
import { useAuth } from "../../src/contexts/AuthContext";

export default function Match() {
  const router = useRouter();
  const { id, accessToken } = router.query;

  const ws = useRef<null | WebSocket>(null);

  const [matchState, setMatchState] = useState({});

  useEffect(() => {
    const url = `ws://localhost:8080/matches/${id}/room?access-token=${accessToken}`;
    ws.current = new WebSocket(url);
    ws.current.onopen = (evt) => {
      console.log("Connection established");
      console.log(evt)
      ws.current.send(
        JSON.stringify({
          matchId: id,
          author: "Juan", // por ahora no hace nada
          action: "get",
          payload: JSON.stringify({}),
        })
      );
    };

    ws.current.onclose = () => {
      console.log("Disconnected from server");
    };

    ws.current.onmessage = (evt) => {
      if (evt.data !== "") {
        const d = JSON.parse(evt.data)
        d.payload = JSON.parse(d.payload)
        setMatchState(d);
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
        <pre>
          {/* <code>{JSON.stringify(JSON.parse(matchState).payload, null, 2)}</code> */}
          <code>{JSON.stringify(matchState, null, 2)}</code>
        </pre>
        {ws.current && (
          <>
            <button
              onClick={() => {
                ws.current.send(
                  JSON.stringify({
                    matchId: id,
                    author: "Juan", // por ahora no hace nada
                    action: "battle",
                    payload: JSON.stringify({
                      cardAttribute: "height", // id
                    }),
                  })
                );
              }}
            >
              Pelear
            </button>
          </>
        )}
      </div>
    </>
  );
}
