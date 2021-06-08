import React, { useEffect, useState } from "react";
import { makeStyles } from "@material-ui/core/styles";
import Typography from "@material-ui/core/Typography";
import { useRouter } from "next/router";

import { Formik } from "formik";

import * as Yup from "yup";

import { useAuth } from "../../src/contexts/AuthContext";

import TextField from "../../src/components/TextField";
import Button from "../../src/components/Button";
import api from "../../src/utils/api";
import { Deck, MatchData } from "../../types";
import { FormControl, InputLabel, MenuItem, Select } from "@material-ui/core";

const useStyles = makeStyles({
  formControl: {
    width: "100%",
    margin: "20px 0px",
  },
});

const CreateDeckSchema = Yup.object().shape({
  player: Yup.string().required(),
  deck: Yup.string().required(),
});

export default function CreateDeck() {
  const classes = useStyles();

  const {
    authState: { auth, fetched, accessToken },
  } = useAuth();

  const router = useRouter();

  useEffect(() => {
    if (!auth && fetched) {
      router.push("/auth/login");
    }
  }, [auth, fetched]);

  const [decks, setDecks] = useState<Deck[]>([]);
  useEffect(() => {
    const fetchDecks = async () => {
      const res = await api.GET<{ decks: Deck[] }>("/decks");
      setDecks(res.decks);
    };

    fetchDecks();
  }, []);

  return (
    <div>
      {auth && (
        <>
          <Typography
            variant="h1"
            style={{
              width: "100%",
              textAlign: "left",
            }}
          >
            Create match
          </Typography>
          <Formik
            initialValues={{
              player: "",
              deck: "",
            }}
            onSubmit={async ({ player, deck }) => {
              try {
                const res = await api.POST<
                  { player2: string; deckId: string },
                  MatchData
                >(
                  "/matches",
                  accessToken
                )({
                  deckId: deck,
                  player2: player,
                });
                router.push(`/matches/${res.matchId}`);
              } catch ({ status, response }) {}
            }}
            validationSchema={CreateDeckSchema}
          >
            {({
              values,
              handleChange,
              handleBlur,
              handleSubmit,
              errors,
              submitCount,
              setFieldValue,
              ...rest
            }) => (
              <form onSubmit={handleSubmit} id="match-form" className={classes.loginContainer}>
                <TextField
                  className={classes.formControl}
                  value={values.player}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  label={"Player"}
                  type="text"
                  id="player"
                  error={errors.player !== undefined && rest.touched.player}
                  errorMessage={errors.player}
                  handleError
                />
                <FormControl className={classes.formControl}>
                  <InputLabel id="deck-select-label">Deck</InputLabel>
                  <Select
                    labelId="deck-select-label"
                    id="deck"
                    value={values.deck}
                    onChange={(e) => {
                      const value = e.target.value;
                      setFieldValue("deck", value);
                    }}
                  >
                    {decks.map((deck) => (
                      <MenuItem value={deck.id}>{deck.name}</MenuItem>
                    ))}
                  </Select>
                </FormControl>
                <Button
                  style={{
                    marginTop: "40px",
                  }}
                  color="primary"
                  label={"Create"}
                  type="submit"
                  form="match-form"
                  onClick={(e) => {
                    console.log(e)
                  }}
                />
              </form>
            )}
          </Formik>
        </>
      )}
    </div>
  );
}
