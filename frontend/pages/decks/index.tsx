import {
  Container,
  IconButton,
  Paper,
  TableContainer,
} from "@material-ui/core";
import { makeStyles } from "@material-ui/core/styles";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import { Delete, Edit } from "@material-ui/icons";
import { useRouter } from "next/router";
import React, { useEffect, useState } from "react";
import Header from "../../src/components/Header";
import LinkButton from "../../src/components/LinkButton";
import { useAuth } from "../../src/contexts/AuthContext";
import api from "../../src/utils/api";
import { Deck } from "../../types";

const useStyles = makeStyles((theme) => ({
  seeMore: {
    marginTop: theme.spacing(3),
  },
  content: {
    padding: theme.spacing(8, 0, 6),
  },
  button: {
    margin: theme.spacing(4, 0, 1),
  },
}));

export default function Decks() {
  const classes = useStyles();

  const [decks, setDecks] = useState<Deck[]>([]);

  const {
    authState: { accessToken, auth, fetched },
  } = useAuth();

  const router = useRouter();

  useEffect(() => {
    const fetchDecks = async () => {
      const res = await api.GET<{ decks: Deck[] }>("/decks");
      setDecks(res.decks);
    };

    fetchDecks();
  }, []);

  const deleteDeck = (deckId) => async () => {
    await api.DELETE(`/decks/${deckId}`, accessToken);
    setDecks((prev) => prev.filter((d) => d.id !== deckId));
  };

  return (
    <>
      <Header title="Mazos" subtitle={`${decks.length} mazos`} />
      <Container maxWidth="md" style={{ marginTop: "20px" }}>
        {auth && fetched && (
          <LinkButton
            href="/decks/create"
            color="primary"
            variant="contained"
            className={classes.button}
          >
            New Deck
          </LinkButton>
        )}

        <TableContainer
          component={Paper}
          style={{ marginTop: "20px", marginBottom: "20px" }}
        >
          <Table aria-label="simple table">
            <TableHead>
              <TableRow>
                <TableCell>Nombre</TableCell>
                <TableCell align="right">Cartas</TableCell>
                {auth && fetched && <TableCell>Actions</TableCell>}
              </TableRow>
            </TableHead>
            <TableBody>
              {decks.map((deck) => (
                <TableRow key={deck.name}>
                  <TableCell component="th" scope="row">
                    {deck.name}
                  </TableCell>
                  <TableCell align="right">{deck.cards.length}</TableCell>
                  {auth && fetched && (
                    <TableCell>
                      <IconButton
                        onClick={() => {
                          router.push(`/decks/${deck.id}`);
                        }}
                      >
                        <Edit />
                      </IconButton>
                      <IconButton onClick={deleteDeck(deck.id)}>
                        <Delete />
                      </IconButton>
                    </TableCell>
                  )}
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Container>
    </>
  );
}
