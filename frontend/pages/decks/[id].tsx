import {
  Container,
  Grid,
  IconButton,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from "@material-ui/core";
import { Add, Delete } from "@material-ui/icons";
import { useRouter } from "next/router";
import React, { useEffect, useState } from "react";
import Header from "../../src/components/Header";
import { useAuth } from "../../src/contexts/AuthContext";
import api from "../../src/utils/api";
import { Deck, Hero } from "../../types";

export default function Decks() {
  const {
    authState: { auth, accessToken, fetched },
  } = useAuth();

  const router = useRouter();

  useEffect(() => {
    if (!auth && fetched) {
      router.push("/auth/login");
    }
  }, [auth, fetched]);

  const { id } = router.query;

  const [deck, setDeck] = useState<Deck | undefined>();

  useEffect(() => {
    if (id !== undefined) {
      const fetchDeck = async () => {
        const res = await api.GET<Deck>(`/decks/${id}`, accessToken);
        setDeck(res);
      };

      fetchDeck();
    }
  }, [id]);

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

  return auth && fetched && deck ? (
    <>
      <Header title={deck.name} subtitle={`${deck.cards.length} cartas`} />
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
          {cards.length !== 0 && (
            <TableContainer
              component={Paper}
              style={{ marginTop: "20px", marginBottom: "20px" }}
            >
              <Table aria-label="simple table">
                <TableHead>
                  <TableRow>
                    <TableCell></TableCell>
                    <TableCell>Nombre</TableCell>
                    <TableCell align="right">Altura</TableCell>
                    <TableCell align="right">Peso</TableCell>
                    <TableCell align="right">Inteligencia</TableCell>
                    <TableCell align="right">Velocidad</TableCell>
                    <TableCell align="right">Poder</TableCell>
                    <TableCell align="right">Combate</TableCell>
                    <TableCell align="right">Fuerza</TableCell>
                    {auth && fetched && (
                      <TableCell align="right">Action</TableCell>
                    )}
                  </TableRow>
                </TableHead>
                <TableBody>
                  {cards.map((card) => {
                    const isInDeck =
                      deck.cards.find((cardId) => cardId === card.id) !==
                      undefined;
                    return (
                      <TableRow key={`${card.id}`}>
                        <TableCell>
                          <div
                            style={{
                              height: "50px",
                              width: "50px",
                              backgroundImage: `url(${card.image})`,
                              backgroundSize: "cover",
                              backgroundRepeat: "no-repeat",
                              backgroundPosition: "center",
                            }}
                          ></div>
                        </TableCell>
                        <TableCell component="th" scope="row">
                          {card.name}
                        </TableCell>
                        <TableCell align="right">
                          {card.stats.height} cm
                        </TableCell>
                        <TableCell align="right">
                          {card.stats.weight} kg
                        </TableCell>
                        <TableCell align="right">
                          {card.stats.intelligence}
                        </TableCell>
                        <TableCell align="right">{card.stats.speed}</TableCell>
                        <TableCell align="right">{card.stats.power}</TableCell>
                        <TableCell align="right">{card.stats.combat}</TableCell>
                        <TableCell align="right">
                          {card.stats.strength}
                        </TableCell>
                        {auth && fetched && (
                          <TableCell align="center">
                            {isInDeck ? (
                              <IconButton onClick={removeFromDeck(card.id)}>
                                <Delete />
                              </IconButton>
                            ) : (
                              <IconButton onClick={addToDeck(card.id)}>
                                <Add />
                              </IconButton>
                            )}
                          </TableCell>
                        )}
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </Grid>
      </Container>
    </>
  ) : (
    "No se encontrpo el mazo"
  );
}
