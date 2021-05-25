import {
  Container,
  IconButton,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from "@material-ui/core";
import { makeStyles } from "@material-ui/core/styles";
import { AddCircle, Delete as DeleteIcon } from "@material-ui/icons";
import { Formik } from "formik";
import { useRouter } from "next/router";
import React, { useEffect, useState } from "react";
import * as Yup from "yup";
import Button from "../../src/components/Button";
import Header from "../../src/components/Header";
import TextField from "../../src/components/TextField";
import { useAuth } from "../../src/contexts/AuthContext";
import api from "../../src/utils/api";
import { Hero } from "../../types/index";

const AddCardSchema = Yup.object().shape({
  name: Yup.string().required(),
});

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
  const classes = useStyles();
  const {
    authState: { auth, fetched, accessToken },
  } = useAuth();

  const router = useRouter();

  useEffect(() => {
    if (!auth && fetched) {
      router.push("/auth/login");
    }
  }, [auth, fetched]);

  const [heros, setHeros] = useState<Hero[]>([]);

  const [added, setAdded] = useState<number[]>([]);

  const addCard = (id: number) => async () => {
    try {
      await api.POST<{ id: number }, Hero>(
        "/cards",
        accessToken
      )({
        id,
      });
      setAdded((prev) => [...prev, id]);
    } catch ({ status, response }) {}
  };

  useEffect(() => {
    const fetchCards = async () => {
      const res = await api.GET<{ cards: Hero[] }>("/cards");
      setAdded((prev) => [...prev, ...res.cards.map((_) => _.id)]);
    };
    fetchCards();
  }, []);

  return (
    auth && (
      <>
        <Header title="Agregar cartas" />
        <Container
          maxWidth="md"
          style={{ marginTop: "20px", marginBottom: "20px" }}
        >
          <Formik
            initialValues={{
              name: "",
            }}
            onSubmit={async ({ name }) => {
              try {
                const res = await api.GET<{ superheros: Hero[] }>(
                  `/superheros/name/${name}`
                );
                setHeros(res.superheros);
              } catch ({ status, response }) {}
            }}
            validationSchema={AddCardSchema}
          >
            {({
              values,
              handleChange,
              handleBlur,
              handleSubmit,
              errors,
              submitCount,
              ...rest
            }) => (
              <form onSubmit={handleSubmit} className={classes.loginContainer}>
                <TextField
                  className={classes.formControl}
                  value={values.name}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  label={"Superhero name"}
                  type="text"
                  id="name"
                  error={errors.name !== undefined && rest.touched.name}
                  errorMessage={errors.name}
                  handleError
                />
                <Button
                  style={{
                    marginTop: "20px",
                  }}
                  color="primary"
                  label={"Search"}
                  type="submit"
                />
              </form>
            )}
          </Formik>
          {heros.length !== 0 && (
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
                    <TableCell align="right">Agregar</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {heros.map((card) => (
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
                      <TableCell align="right">{card.stats.strength}</TableCell>
                      <TableCell align="center">
                        {added.includes(card.id) ? (
                          <IconButton disabled>
                            <DeleteIcon />
                          </IconButton>
                        ) : (
                          <IconButton onClick={addCard(card.id)}>
                            <AddCircle />
                          </IconButton>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </Container>
      </>
    )
  );
};

export default AddCard;
