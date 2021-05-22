import * as React from "react";

import { makeStyles } from "@material-ui/styles";

const useStyles = makeStyles({
  root: {
    position: "relative",
    width: "100%",
    color: "var(--secondary)",
    backgroundColor: "var(--gray-mid)",
    padding: "24px 12px",
    border: "none",
    outline: "none !important",
    cursor: "pointer",
    fontFamily: "Gotham, sans-serif",
    textTransform: "uppercase",
    fontSize: 18,
    fontWeight: 700,
    "&:before": {
      content: "''",
      position: "absolute",
      left: "50%",
      right: "50%",
      bottom: 0,
      background: "#AAA",
      height: 4,
      transitionProperty: "left, right",
      transitionDuration: "0.15s",
      transitionTimingFunction: "ease-out",
    },
    "&:hover:before": {
      left: 0,
      right: 0,
    },
    "@media only screen and (max-width : 800px)": {
      fontSize: 16,
      lineHeight: "19px",
      letterSpacing: "-0.376417px",
    },
  },
  primary: {
    color: "#FFF",
    backgroundColor: "var(--primary)",
    "&:before": {
      backgroundColor: "var(--primary-dark)",
    },
  },
  primaryOutlined: {
    color: "var(--primary)",
    backgroundColor: "#fff",
    border: "2px solid var(--primary)",
    "&:before": {
      backgroundColor: "var(--primary)",
    },
    "@media only screen and (max-width : 437px)": {
      "&:before": {
        backgroundColor: "transparent",
      },
    },
  },
  accent: {
    color: "#FFF",
    backgroundColor: "var(--accent)",
    "&:before": {
      backgroundColor: "rgba(0,0,0,.25)",
    },
  },
  large: {
    padding: "32px 24px",
    fontSize: 24,
    "@media only screen and (max-width : 800px)": {
      fontSize: 22,
    },
  },
  disabled: {
    backgroundColor: "var(--gray-mid)",
    "&:before": {
      backgroundColor: "var(--gray-mid)",
    },
  },
});

interface Props
  extends React.DetailedHTMLProps<
    React.ButtonHTMLAttributes<HTMLButtonElement>,
    HTMLButtonElement
  > {
  color?: "primary" | "accent";
  size?: "large";
  label: string | React.ReactChild;
  disabled?: boolean;
  unactive?: boolean;
  onClick?: (event: React.MouseEvent<HTMLButtonElement, MouseEvent>) => void;
  type?: "reset" | "button" | "submit";
  style?: React.CSSProperties;
  className?: string;
  id?: string;
}

const Button = (props: Props) => {
  const {
    color,
    size,
    disabled = false,
    className = "",
    unactive,
    type = "button",
    id = "",
    label,
    ...rest
  } = props;
  const classes = useStyles();
  return (
    <button
      className={`
    ${classes.root}
    ${color ? classes[color] : ""}
    ${size ? classes[size] : ""}
    ${disabled || unactive ? classes.disabled : ""}
    ${className}
  `}
      id={id}
      disabled={disabled}
      onClick={props.onClick}
      type={type}
      style={{
        cursor: disabled ? "not-allowed" : "pointer",
        userSelect: "none",
        ...props.style,
      }}
      {...rest}
    >
      {label}
    </button>
  );
};

export default Button;
