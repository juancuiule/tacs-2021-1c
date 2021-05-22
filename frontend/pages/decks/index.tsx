import React, { useEffect, useState } from "react";
import { makeStyles } from "@material-ui/core/styles";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import Typography from "@material-ui/core/Typography";
import CssBaseline from "@material-ui/core/CssBaseline";
import Container from "@material-ui/core/Container";
import Navbar from "../../src/components/navbar/Navbar";
import { Edit, Delete } from "@material-ui/icons";

import Link from "../../src/Link";
import LinkButton from "../../src/LinkButton";
import api from "../../src/utils/api";
import { Deck } from "../../types";
import { useAuth } from "../../src/contexts/AuthContext";

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
    authState: { accessToken, auth },
  } = useAuth();

  useEffect(() => {
    const fetchDecks = async () => {
      const res = await api.GET<{ decks: Deck[] }>("/decks", accessToken);
      setDecks(res.decks);
    };

    fetchDecks();
  }, []);

  return (
    auth && (
      <>
        <Typography component="h1">Decks</Typography>

        <LinkButton
          href="/decks/create"
          color="primary"
          variant="contained"
          className={classes.button}
        >
          New Deck
        </LinkButton>

        <Table size="small">
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
                  {/* <Delete /> */}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </>
    )
  );
}
