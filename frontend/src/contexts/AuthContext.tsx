import React, { ReactChild, useContext, useEffect } from "react";
import api from "../utils/api";

export type AuthContextData = {
  auth: boolean;
  userName?: string;
  id?: string;
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
  id: string;
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
      localStorage.setItem("id", action.id);
      return {
        ...state,
        auth: true,
        fetched: true,
        accessToken: action.accessToken,
        userName: action.userName,
        id: action.id,
      };
    }
    case "FETCHED": {
      return {
        ...state,
        fetched: true,
      };
    }
    case "LOGOUT": {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("userName");
      localStorage.removeItem("id");
      return {
        ...state,
        auth: false,
        accessToken: undefined,
        userName: undefined,
        id: undefined,
        fetched: false,
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
    const localStorageId = localStorage.getItem("id");
    if (localStorageAccessToken !== null && localStorageUserName !== null) {
      dispatch({
        type: "LOGIN",
        accessToken: localStorageAccessToken,
        userName: localStorageUserName,
        id: localStorageId,
      });
    } else {
      dispatch({ type: "FETCHED" });
    }
  }, []);

  const login = async (userName: string, password: string) => {
    const { id, accessToken } = await api.login({
      userName,
      password,
    });

    dispatch({
      type: "LOGIN",
      accessToken,
      userName,
      id,
    });
  };

  const signup = async (
    userName: string,
    password: string,
    role: "Admin" | "Player"
  ) => {
    const { id, accessToken } = await api.signup({
      userName,
      password,
      role,
    });
    dispatch({
      type: "LOGIN",
      accessToken,
      userName,
      id,
    });
  };

  const logout = () => {
    dispatch({
      type: "LOGOUT",
    });
  };

  return { authState, login, signup, logout };
}
