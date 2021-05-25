import { Button, Container, makeStyles, Typography } from "@material-ui/core";
import { Formik } from "formik";
import Link from "next/link";
import { useRouter } from "next/router";
import * as React from "react";
import * as Yup from "yup";
import ErrorMessage from "../../src/components/ErrorMessage";
import TextField from "../../src/components/TextField";
import { useAuth } from "../../src/contexts/AuthContext";

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
      <Container maxWidth="sm">
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
                  marginTop: "20px",
                }}
                id="button-login"
                variant="contained"
                color="primary"
                type="submit"
              >
                Login
              </Button>
              <ErrorMessage condition={error !== ""} message={error} />
              <Link href="/auth/signup">or signup</Link>
            </form>
          )}
        </Formik>
      </Container>
    </>
  );
};

export default Login;
