import React, { useEffect, useState } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import CssBaseline from '@material-ui/core/CssBaseline';
import Container from '@material-ui/core/Container';
import { useRouter } from 'next/router';
import Navbar from '../../components/navbar/Navbar';
import Link from '../../src/Link';
import { Edit, Delete } from '@material-ui/icons';
import { useSession } from 'next-auth/client';

const useStyles = makeStyles(theme => ({
  paper: {
    marginTop: theme.spacing(8),
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
  },
  content: {
    padding: theme.spacing(8, 0, 6),
  },
  avatar: {
    margin: theme.spacing(1),
    backgroundColor: theme.palette.secondary.main,
  },
  form: {
    width: '100%', // Fix IE 11 issue.
    marginTop: theme.spacing(1),
  },
  submit: {
    margin: theme.spacing(3, 0, 2),
  },
}));

export default function CreateDeck() {
  const [session, loading] = useSession();
  const classes = useStyles();
  const router = useRouter();

  const [name, setName] = useState('');

  const submit = async e => {
    e.preventDefault();

    console.log(loading, session)

    if (!name.length) return;

    const response = await fetch(`${process.env.apiBase}/decks`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ name }),
    });

    const newDeck = await response.json();

    router.push('/decks');
  };

  return (
    <>
      <CssBaseline />

      <Navbar />

      <Container maxWidth="sm" component="main" className={classes.content}>
        <CssBaseline />
        <div className={classes.paper}>
          <Typography component="h1" variant="h5">
            New Deck
          </Typography>
          <form className={classes.form} noValidate onSubmit={submit}>
            <TextField
              variant="outlined"
              margin="normal"
              required
              fullWidth
              id="name"
              label="Name"
              name="name"
              autoFocus
              onChange={e => setName(e.target.value)}
            />

            <Button
              type="submit"
              fullWidth
              variant="contained"
              color="primary"
              className={classes.submit}
            >
              Create Deck
            </Button>
          </form>
        </div>
      </Container>
    </>
  );
}
