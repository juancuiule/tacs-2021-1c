import React, { useEffect, useState } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Typography from '@material-ui/core/Typography';
import CssBaseline from '@material-ui/core/CssBaseline';
import Container from '@material-ui/core/Container';
import { useRouter } from 'next/router';
import Navbar from '../../components/navbar/Navbar';
import Link from '../../src/Link';
import { Edit, Delete } from '@material-ui/icons';

const useStyles = makeStyles(theme => ({
  seeMore: {
    marginTop: theme.spacing(3),
  },
  content: {
    padding: theme.spacing(8, 0, 6),
  },
}));

export default function Decks() {
  const classes = useStyles();
  const router = useRouter();

  const { id } = router.query;

  const [deck, setDeck] = useState({
    id,
    name: '',
    cards: [],
  });

  useEffect(() => {
    const fetchDeck = async () => {
      const res = await fetch(`${process.env.apiBase}/decks/${id}`);
      setDeck(await res.json());
    };

    fetchDeck();
  }, []);

  return (
    <>
      <CssBaseline />

      <Navbar />

      <Container maxWidth="md" component="main" className={classes.content}>
        <Typography component="h2" variant="h5" color="primary" gutterBottom>
          Deck {deck.name}
        </Typography>

        <Typography component="h3" variant="h6" color="primary" gutterBottom>
          Cards
        </Typography>

        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {deck.cards.map(row => (
              <TableRow key={row.id}>
                <TableCell>{row.name}</TableCell>
                <TableCell align="right">
                  <Delete />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Container>
    </>
  );
}
