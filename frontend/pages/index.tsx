import { makeStyles } from "@material-ui/core/styles";
import { useAuth } from "../src/contexts/AuthContext";
import { useEffect } from "react";
import { useRouter } from "next/router";
import Link from "next/link";

const useStyles = makeStyles((theme) => ({}));

export default function Home() {
  const {
    authState: { auth, userName },
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
          Hola {userName} <br />
          <Link href="/decks/create">Crear mazo</Link> <br />
          <Link href="/cards/add">Agregar cartas</Link>
        </>
      )}
    </div>
  );
}
