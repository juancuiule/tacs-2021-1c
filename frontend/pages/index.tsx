import { Container, Grid } from "@material-ui/core";
import { useRouter } from "next/router";
import { useEffect } from "react";
import Header from "../src/components/Header";
import OutlinedCard from "../src/components/OutlinedCard";
import { useAuth } from "../src/contexts/AuthContext";

const CARDS = [
  {
    title: "Cartas",
    description: "Administrar las cartas en el sistema",

    route: "/cards",
  },
  {
    title: "Mazos",
    description: "Crear o modificar mazos de cartas",

    route: "/decks",
  },
  // { title: "Estadísticas", description: "...", route: "" },
  // { title: "Scoreboard", description: "...", route: "" },
  // { title: "Usuarios", description: "...", route: "" },
];

export default function Home() {
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
    auth && (
      <>
        <Header title="Super Amigos" />
        <Container maxWidth={"md"} style={{ marginTop: "20px" }}>
          <Grid container justify="flex-start" spacing={2} wrap="wrap">
            {CARDS.map((card) => (
              <Grid item xs={6} sm={4} md={3} key={card.title}>
                <OutlinedCard
                  title={card.title}
                  description={card.description}
                  actionText={"Ir"}
                  onClick={() => {
                    router.push(card.route);
                  }}
                />
              </Grid>
            ))}
          </Grid>
        </Container>
      </>
    )
  );
}
