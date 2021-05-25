import { CssBaseline, ThemeProvider, Container } from "@material-ui/core";
import { AppProps } from "next/app";
import Head from "next/head";
import { AuthContextProvider } from "../src/contexts/AuthContext";
import Drawer from "../src/components/Drawer";

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
            <Drawer>
              <Component {...pageProps} />
            </Drawer>
          </ThemeProvider>
        </AuthContextProvider>
      </div>
    </>
  );
}

export default MyApp;
