import {
  Container,
  IconButton,
  Paper,
  TableContainer
} from "@material-ui/core";
import { makeStyles } from "@material-ui/core/styles";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import { Edit } from "@material-ui/icons";
import { useRouter } from "next/router";
import React, { useEffect, useState } from "react";
import Header from "../../src/components/Header";
import LinkButton from "../../src/components/LinkButton";
import { useAuth } from "../../src/contexts/AuthContext";
import api from "../../src/utils/api";
import { parseMatchState } from "../../src/utils/utils";

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

  const [matches, setMatches] = useState<any[]>([]);

  const {
    authState: { accessToken, auth, fetched },
  } = useAuth();

  const router = useRouter();

  useEffect(() => {
    if (accessToken) {
      const fetchMatches = async () => {
        const res = await api.GET<{ matches: any[] }>(
          "/matches",
          accessToken
        );
        const matches = res.matches.map((match) => {
          const { steps, ...d } = match;
          return {
            ...d,
            steps,
            state: parseMatchState(steps[steps.length - 1][1]),
          };
        });
        setMatches(matches);
      };

      fetchMatches();
    }
  }, [accessToken]);

  return (
    <>
      <Header title="Mazos" subtitle={`${matches.length} mazos`} />
      <Container maxWidth="md" style={{ marginTop: "20px" }}>
        {auth && fetched && (
          <LinkButton
            href="/matches/create"
            color="primary"
            variant="contained"
            className={classes.button}
          >
            New Match
          </LinkButton>
        )}

        <TableContainer
          component={Paper}
          style={{ marginTop: "20px", marginBottom: "20px" }}
        >
          <Table aria-label="simple table">
            <TableHead>
              <TableRow>
                <TableCell>Jugadores</TableCell>
                <TableCell align="right">Cartas</TableCell>
                {auth && fetched && <TableCell>Actions</TableCell>}
              </TableRow>
            </TableHead>
            <TableBody>
              {matches.map((match) => (
                <TableRow key={match.matchId}>
                  <TableCell component="th" scope="row">
                    {match.player1} vs. {match.player2}
                  </TableCell>
                  <TableCell align="right">
                    {match.state.cardsInDeck.length}
                  </TableCell>
                  {auth && fetched && (
                    <TableCell>
                      <IconButton
                        onClick={() => {
                          router.push(`/matches/${match.matchId}`);
                        }}
                      >
                        <Edit />
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
