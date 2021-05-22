import { CssBaseline, Grid } from "@material-ui/core";
import type { AppProps } from "next/app";
import Head from "next/head";
import Link from "next/link";
import { AuthContextProvider } from "../src/contexts/AuthContext";

import "./styles.css";

function MyApp({ Component, pageProps }: AppProps) {
  return (
    <>
      <Head>
        <title>Super Amigos</title>
        <link rel="icon" href="/favicon.ico" />
      </Head>
      <div className="app">
        <CssBaseline />
        <AuthContextProvider>
          <Grid container justify="center">
            <Grid item className="container">
              <Link href="/decks/create">Crear mazo</Link>
              <Link href="/cards/add">Agregar cartas</Link>
              <Link href="/decks/">Mazos</Link>
              <Component {...pageProps} />
            </Grid>
          </Grid>
        </AuthContextProvider>
      </div>
    </>
  );
}

export default MyApp;
