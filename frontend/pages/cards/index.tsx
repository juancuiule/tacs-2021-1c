import {
  Container,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from "@material-ui/core";
import { makeStyles } from "@material-ui/core/styles";
import { useRouter } from "next/router";
import React, { useEffect, useState } from "react";
import Header from "../../src/components/Header";
import LinkButton from "../../src/components/LinkButton";
import { useAuth } from "../../src/contexts/AuthContext";
import api from "../../src/utils/api";
import { Hero } from "../../types/index";

const useStyles = makeStyles((theme) => ({
  button: {
    margin: theme.spacing(4, 0, 1),
  },
}));

const Cards = () => {
  const [cards, setCards] = useState<Hero[]>([]);

  const {
    authState: { auth, fetched },
  } = useAuth();

  useEffect(() => {
    const fetchCards = async () => {
      const res = await api.GET<{ cards: Hero[] }>("/cards");
      setCards(res.cards);
    };
    fetchCards();
  }, []);

  const classes = useStyles();

  return (
    <>
      <Header title="Cartas" subtitle={`${cards.length} cartas`} />
      <Container
        maxWidth="md"
        style={{ marginTop: "20px", marginBottom: "20px" }}
      >
        {auth && fetched && (
          <LinkButton
            href="/cards/add"
            color="primary"
            variant="contained"
            className={classes.button}
          >
            Agregar cartas
          </LinkButton>
        )}

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
              </TableRow>
            </TableHead>
            <TableBody>
              {cards.map((card) => (
                <TableRow key={card.name}>
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
                  <TableCell align="right">{card.stats.height} cm</TableCell>
                  <TableCell align="right">{card.stats.weight} kg</TableCell>
                  <TableCell align="right">{card.stats.intelligence}</TableCell>
                  <TableCell align="right">{card.stats.speed}</TableCell>
                  <TableCell align="right">{card.stats.power}</TableCell>
                  <TableCell align="right">{card.stats.combat}</TableCell>
                  <TableCell align="right">{card.stats.strength}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Container>
    </>
  );
};

export default Cards;
