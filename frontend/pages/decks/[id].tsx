import { Container, Grid } from "@material-ui/core";
import { useRouter } from "next/router";
import React, { useEffect, useState } from "react";
import Button from "../../src/components/Button";
import Header from "../../src/components/Header";
import HeroCard from "../../src/components/HeroCard";
import { useAuth } from "../../src/contexts/AuthContext";
import api from "../../src/utils/api";
import { Deck, Hero } from "../../types";

export default function Decks() {
  const {
    authState: { auth, accessToken },
  } = useAuth();

  const router = useRouter();

  const { id } = router.query;

  const [deck, setDeck] = useState<Deck | undefined>();

  useEffect(() => {
    const fetchDeck = async () => {
      const res = await api.GET<Deck>(`/decks/${id}`, accessToken);
      setDeck(res);
    };

    fetchDeck();
  }, []);

  const [cards, setCards] = useState<Hero[]>([]);

  useEffect(() => {
    const fetchCards = async () => {
      const res = await api.GET<{ cards: Hero[] }>("/cards");
      setCards(res.cards);
    };
    fetchCards();
  }, []);

  const addToDeck = (id: number) => async () => {
    try {
      const res = await api.PATCH<{ cardId: number }, Deck>(
        `/decks/${deck.id}`,
        accessToken
      )({
        cardId: id,
      });
      setDeck(res);
    } catch ({ status, response }) {}
  };

  const removeFromDeck = (id: number) => async () => {
    try {
      const res = await api.DELETE<Deck>(
        `/decks/${deck.id}/card/${id}`,
        accessToken
      );
      setDeck(res);
    } catch ({ status, response }) {}
  };

  return auth && deck ? (
    <>
      <Header title={deck.name} />
      <Container maxWidth="md" style={{ marginTop: "20px" }}>
        <Grid
          container
          wrap="wrap"
          justify="space-between"
          style={{
            marginTop: "20px",
          }}
          spacing={2}
        >
          {cards.map((card) => {
            const isInDeck =
              deck.cards.find((cardId) => cardId === card.id) !== undefined;
            return (
              <Grid item xs={2} sm={4} md={3} key={card.id}>
                <HeroCard hero={card} />
                {isInDeck ? (
                  <Button
                    label={"Eliminar del mazo"}
                    onClick={removeFromDeck(card.id)}
                    color="primary"
                  />
                ) : (
                  <Button
                    label={"Agregar"}
                    onClick={addToDeck(card.id)}
                    color="accent"
                  />
                )}
              </Grid>
            );
          })}
        </Grid>
      </Container>
    </>
  ) : (
    "No se encontró ese mazo"
  );
}
