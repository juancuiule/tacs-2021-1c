import * as React from "react";

import { makeStyles, Typography } from "@material-ui/core";

import { Formik } from "formik";
import * as Yup from "yup";
import { useAuth } from "../../src/contexts/AuthContext";

import TextField from "../../src/components/TextField";
import Button from "../../src/components/Button";
import { useRouter } from "next/router";
import ErrorMessage from "../../src/components/ErrorMessage";

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

const LoginSchema = Yup.object().shape({
  userName: Yup.string().required(),
  password: Yup.string().required(),
});

const Login = () => {
  const classes = useStyles();

  const {
    login,
    authState: { auth },
  } = useAuth();

  const router = useRouter();

  React.useEffect(() => {
    if (auth) {
      router.push("/");
    }
  }, [auth]);

  const [error, setError] = React.useState("");

  return (
    <>
      <div>
        <Typography
          variant="h1"
          style={{
            width: "100%",
            textAlign: "left",
          }}
        >
          Login
        </Typography>
        <Formik
          initialValues={{
            userName: "",
            password: "",
          }}
          onSubmit={async ({ userName, password }) => {
            // startLoading();
            try {
              await login(userName, password);
            } catch ({ status, response }) {
              console.log(status, response);
              setError(response.error);
              // stopLoading();
            }
          }}
          validationSchema={LoginSchema}
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
                value={values.userName}
                onChange={handleChange}
                onBlur={handleBlur}
                label={"Username"}
                type="userName"
                autoComplete="userName"
                id="userName"
                error={errors.userName !== undefined && rest.touched.userName}
                errorMessage={errors.userName}
                handleError
              />
              <TextField
                className={classes.formControl}
                value={values.password}
                onChange={handleChange}
                onBlur={handleBlur}
                label={"Password"}
                type={"password"}
                id="password"
                autoComplete="current-password"
                error={errors.password !== undefined && rest.touched.password}
                errorMessage={errors.password}
                handleError
              />
              <Button
                style={{
                  marginTop: "40px",
                }}
                id="button-login"
                color="primary"
                label={"Login"}
                type="submit"
              />
              <ErrorMessage condition={error !== ""} message={error} />
            </form>
          )}
        </Formik>
      </div>
    </>
  );
};

export default Login;
