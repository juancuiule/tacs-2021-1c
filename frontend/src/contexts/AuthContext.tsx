import React, { ReactChild, useContext, useEffect } from "react";
import api from "../utils/api";

export type AuthContextData = {
  auth: boolean;
  userName?: string;
  accessToken?: string;
  fetched: boolean;
};

const initialAuthContextData: () => AuthContextData = () => {
  return {
    auth: false,
    fetched: false,
  };
};

type Login = {
  type: "LOGIN";
  accessToken: string;
  userName: string;
};

type Logout = {
  type: "LOGOUT";
};

type Fetched = {
  type: "FETCHED";
};

type ReducerAction = Login | Logout | Fetched;

function reducer(
  state: AuthContextData,
  action: ReducerAction
): AuthContextData {
  switch (action.type) {
    case "LOGIN": {
      localStorage.setItem("accessToken", action.accessToken);
      localStorage.setItem("userName", action.userName);
      return {
        ...state,
        auth: true,
        fetched: true,
        accessToken: action.accessToken,
        userName: action.userName,
      };
    }
    case "FETCHED": {
      return {
        ...state,
        fetched: true,
      };
    }
  }
}

export const AuthContext = React.createContext({
  authState: initialAuthContextData(),
  dispatch: (value: ReducerAction) => {},
});

export function AuthContextProvider(props: { children: ReactChild }) {
  const [state, dispatch] = React.useReducer(reducer, initialAuthContextData());

  return (
    <AuthContext.Provider value={{ authState: state, dispatch }}>
      {props.children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const { authState, dispatch } = useContext(AuthContext);

  useEffect(() => {
    const localStorageAccessToken = localStorage.getItem("accessToken");
    const localStorageUserName = localStorage.getItem("userName");
    if (localStorageAccessToken !== null && localStorageUserName !== null) {
      dispatch({
        type: "LOGIN",
        accessToken: localStorageAccessToken,
        userName: localStorageUserName,
      });
    } else {
      dispatch({ type: "FETCHED" });
    }
  }, []);

  const login = async (userName: string, password: string) => {
    const { accessToken } = await api.login({
      userName,
      password,
    });

    dispatch({
      type: "LOGIN",
      accessToken,
      userName,
    });
  };

  const signup = async (
    userName: string,
    password: string,
    role: "Admin" | "Player"
  ) => {
    const { accessToken } = await api.signup({
      userName,
      password,
      role,
    });
    dispatch({
      type: "LOGIN",
      accessToken,
      userName,
    });
  };

  const logout = () => {
    dispatch({
      type: "LOGOUT",
    });
  };

  return { authState, login, signup, logout };
}
