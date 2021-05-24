import { makeStyles } from "@material-ui/core";
import * as React from "react";

import { Hero } from "../../types/index";

const useStyles = makeStyles({
  cardContainer: {
    padding: "10px",
    border: "1px solid gray",
    flex: "1",
    // maxWidth: "200px",
    textAlign: "left",
  },
  cardImage: {
    width: "100%",
    minWidth: "120px",
  },
  cardName: {
    marginBottom: "2px",
  },
});

function HeroCard({ hero }: { hero: Hero }) {
  const classes = useStyles();

  return (
    <div key={hero.id} className={classes.cardContainer}>
      <div
        style={{
          backgroundImage: `url(${hero.image})`,
          width: "100%",
          height: "300px",
          backgroundSize: "cover",
          backgroundRepeat: "no-repeat",
          backgroundPosition: 'center'
        }}
      ></div>
      <h2 className={classes.cardName}>{hero.name}</h2>
      {hero.biography ? (
        <>
          <span>{hero.biography.fullName || ''}</span>
          <br />
          <span style={{ color: "rgb(150, 150, 150)" }}>
            {hero.biography.publisher}
          </span>
        </>
      ) : null}
      <ul
        style={{
          paddingLeft: "0px",
          listStyle: 'none',
        }}
      >
        {Object.entries(hero.stats).map(([powerstat, value]) => {
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

export default HeroCard;
