import React, { useEffect, useState } from "react";
import { makeStyles } from "@material-ui/core/styles";
import Typography from "@material-ui/core/Typography";
import { useRouter } from "next/router";

import { Formik } from "formik";

import * as Yup from "yup";

import { useAuth } from "../../src/contexts/AuthContext";

import TextField from "../../src/components/TextField";
import Button from "../../src/components/Button";
import api from "../../src/utils/api";

import { Hero } from "../../types/index";
import { Grid } from "@material-ui/core";
import HeroCard from "../../src/components/HeroCard";

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
    authState: { auth, accessToken },
  } = useAuth();

  const router = useRouter();

  useEffect(() => {
    if (!auth) {
      router.push("/auth/login");
    }
  }, [auth]);

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

  return (
    <div>
      {auth && (
        <>
          <Typography
            variant="h1"
            style={{
              width: "100%",
              textAlign: "left",
            }}
          >
            Agregar cartas
          </Typography>
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
                    marginTop: "40px",
                  }}
                  color="primary"
                  label={"Crear"}
                  type="submit"
                />
              </form>
            )}
          </Formik>
          <Grid
            container
            wrap="wrap"
            justify="space-between"
            style={{
              marginTop: "20px",
            }}
            spacing={2}
          >
            {heros.map((hero) => {
              return (
                <Grid item xs={2} sm={4} md={3} key={hero.id}>
                  <HeroCard hero={hero} />
                  {added.includes(hero.id) ? (
                    "Agregada"
                  ) : (
                    <Button
                      label="Buscar"
                      onClick={addCard(hero.id)}
                      color="accent"
                    />
                  )}
                </Grid>
              );
            })}
          </Grid>
        </>
      )}
    </div>
  );
};

export default AddCard;
