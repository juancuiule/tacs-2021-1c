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

const CreateDeckSchema = Yup.object().shape({
  name: Yup.string().required(),
});

export default function CreateDeck() {
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
            Create deck
          </Typography>
          <Formik
            initialValues={{
              name: "",
            }}
            onSubmit={async ({ name }) => {
              try {
                const res = await api.POST<
                  { name: string },
                  { id: number; name: string }
                >(
                  "/decks",
                  accessToken
                )({
                  name,
                });
                console.log(res);
                router.push(`/decks/${res.id}`);
              } catch ({ status, response }) {}
            }}
            validationSchema={CreateDeckSchema}
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
                  label={"Deck name"}
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
        </>
      )}
    </div>
  );
}
