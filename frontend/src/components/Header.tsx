import * as React from "react";

import { makeStyles } from "@material-ui/styles";
import { Typography } from "@material-ui/core";

const useStyles = makeStyles({
  header: {
    height: "300px",
    width: "100%",
    backgroundColor: "gray",
    color: "white",
    display: "flex",
    flexDirection: "column",
    justifyContent: "center",
    alignItems: "center",
  },
});

const Button = (props: { title: string }) => {
  const classes = useStyles();
  return (
    <header className={classes.header}>
      <Typography variant="h1">{props.title}</Typography>
    </header>
  );
};

export default Button;
