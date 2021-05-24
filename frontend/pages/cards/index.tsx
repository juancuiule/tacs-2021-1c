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
import Link from "next/link";
import { useRouter } from "next/router";
import React, { useEffect, useState } from "react";
import Header from "../../src/components/Header";
import { useAuth } from "../../src/contexts/AuthContext";
import api from "../../src/utils/api";
import { Hero } from "../../types/index";

const useStyles = makeStyles({
  loginContainer: {
    display: "flex",
    flexDirection: "column",
  },
  formControl: {
    width: "100%",
    margin: "20px 0px",
  },
});

const AddCard = () => {
  const {
    authState: { auth, fetched, accessToken },
  } = useAuth();

  const router = useRouter();

  useEffect(() => {
    if (!auth && fetched) {
      router.push("/auth/login");
    }
  }, [auth]);

  const [cards, setCards] = useState<Hero[]>([]);

  useEffect(() => {
    const fetchCards = async () => {
      const res = await api.GET<{ cards: Hero[] }>("/cards");
      setCards(res.cards);
    };
    fetchCards();
  }, []);

  return (
    auth && (
      <>
        <Header title="Cartas" />
        <Container
          maxWidth="md"
          style={{ marginTop: "20px", marginBottom: "20px" }}
        >
          <Link href="/cards/add">Agregar cartas</Link>

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
                    <TableCell align="right">
                      {card.stats.intelligence}
                    </TableCell>
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
    )
  );
};

export default AddCard;
