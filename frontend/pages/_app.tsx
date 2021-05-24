import { CssBaseline, ThemeProvider, Container } from "@material-ui/core";
import { AppProps } from "next/app";
import Head from "next/head";
import { AuthContextProvider } from "../src/contexts/AuthContext";

import "./styles.css";
import theme from "../src/theme";

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
          <ThemeProvider theme={theme}>
            <Component {...pageProps} />
          </ThemeProvider>
        </AuthContextProvider>
      </div>
    </>
  );
}

export default MyApp;
