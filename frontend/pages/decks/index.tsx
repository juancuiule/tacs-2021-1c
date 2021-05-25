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
import React, { useEffect, useState } from "react";
import Header from "../../src/components/Header";
import Link from "../../src/components/Link";
import { useAuth } from "../../src/contexts/AuthContext";
import LinkButton from "../../src/components/LinkButton";
import api from "../../src/utils/api";
import { Deck } from "../../types";
import { useRouter } from "next/router";

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
    if (auth && fetched) {
      const fetchDecks = async () => {
        const res = await api.GET<{ decks: Deck[] }>("/decks", accessToken);
        setDecks(res.decks);
      };

      fetchDecks();
    }
  }, [auth, fetched]);

  // const deleteDeck

  return (
    auth &&
    fetched && (
      <>
        <Header title="Mazos" />
        <Container maxWidth="md" style={{ marginTop: "20px" }}>
          <LinkButton
            href="/decks/create"
            color="primary"
            variant="contained"
            className={classes.button}
          >
            New Deck
          </LinkButton>

          <TableContainer
            component={Paper}
            style={{ marginTop: "20px", marginBottom: "20px" }}
          >
            <Table aria-label="simple table">
              <TableHead>
                <TableRow>
                  <TableCell>Nombre</TableCell>
                  <TableCell align="right">Cartas</TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {decks.map((deck) => (
                  <TableRow key={deck.name}>
                    <TableCell component="th" scope="row">
                      {deck.name}
                    </TableCell>
                    <TableCell align="right">{deck.cards.length}</TableCell>
                    <TableCell>
                      <IconButton
                        onClick={() => {
                          router.push(`/decks/${deck.id}`);
                        }}
                      >
                        <Edit />
                      </IconButton>
                      <IconButton onClick={() => {}} disabled>
                        <Delete />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

          {/* <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Name</TableCell>
                <TableCell>Cartas</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {decks.map((row) => (
                <TableRow key={row.id}>
                  <TableCell>{row.name}</TableCell>
                  <TableCell>{row.cards.length}</TableCell>
                  <TableCell align="right">
                    <Link href={`/decks/${row.id}`}>
                      <Edit />
                    </Link>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table> */}
        </Container>
      </>
    )
  );
}
