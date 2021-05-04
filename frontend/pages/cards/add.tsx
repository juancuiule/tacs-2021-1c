import useSWR from "swr";
import { useState } from "react";

const fetcher = (route: string, options) =>
  fetch(route, options).then((res) => res.json());

type Superhero = {
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

function SuperHeroCard({ superhero }: { superhero: Superhero }) {
  return (
    <div
      key={superhero.id}
      style={{
        padding: "10px",
        border: "1px solid gray",
        borderRadius: "10px",
        flex: "1",
        maxWidth: "200px",
      }}
    >
      <div
        style={{
          width: "100%",
          minWidth: "120px",
        }}
      >
        <img width="100%" alt={superhero.name} src={superhero.image} />
      </div>
      <h2
        style={{
          marginBottom: "2px",
        }}
      >
        {superhero.name}
      </h2>
      {superhero.biography ? (
        <>
          {superhero.biography.fullName !== "" && (
            <>
              <span>{superhero.biography.fullName}</span>
              <br />
            </>
          )}
          <span style={{ color: "rgb(150, 150, 150)" }}>
            {superhero.biography.publisher}
          </span>
        </>
      ) : null}
      <ul
        style={{
          paddingLeft: "20px",
        }}
      >
        {Object.entries(superhero.stats).map(([powerstat, value]) => {
          return (
            <li key={powerstat}>
              {powerstat}: {value}
            </li>
          );
        })}
      </ul>
    </div>
  );
}

const AddCard = () => {
  const [state, setState] = useState({
    name: "",
  });

  const { data, error } = useSWR(
    () =>
      state.name !== ""
        ? "http://localhost:8080/superheros/name/" + state.name
        : null,
    fetcher
  );

  console.log(data);

  return (
    <div className="container">
      <main>
        <h1 className="title">Agregar cartas</h1>
        <input
          value={state.name}
          onChange={(e) => {
            const value = e.target.value;
            setState({ name: value });
          }}
        />
        {state.name !== "" && !data && !error ? "Loading..." : null}
        <div
          style={{
            display: "flex",
            fontSize: "12px",
            textAlign: "left",
            gap: "10px",
            maxWidth: "1200px",
            padding: "10px 24px",
            flexWrap: "wrap",
            margin: '0 auto'
          }}
        >
          {data && data.superheros
            ? data.superheros.map((superhero) => (
                <SuperHeroCard superhero={superhero} />
              ))
            : null}
        </div>
      </main>
    </div>
  );
};

export default AddCard;
