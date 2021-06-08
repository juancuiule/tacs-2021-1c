const handleResponse = async <U>(response: Response) => {
  if (response.ok) {
    if (response.status !== 204) {
      return (await response.json()) as U;
    } else {
      return {} as U;
    }
  } else {
    const error = {
      status: response.status,
      response: await response.json(),
    };
    throw error;
  }
};

const handleReqWithBody =
  (method: "POST" | "PUT" | "PATCH") =>
  (api_url: string) =>
  <T, U>(path: string, token?: string) =>
  async (bodyData: T, extraHeaders?: HeadersInit) =>
    await fetch(`${api_url}${path}`, {
      method,
      // credentials: "include",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
        ...(token !== undefined ? { Authorization: `Bearer ${token}` } : {}),
        ...extraHeaders,
      },
      body: JSON.stringify(bodyData),
    }).then((r) => handleResponse<U>(r));

export const handleGet =
  (api_url: string) =>
  async <U>(path: string, token?: string) =>
    await fetch(`${api_url}${path}`, {
      method: "GET",
      // credentials: "include",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
        ...(token !== undefined ? { Authorization: `Bearer ${token}` } : {}),
      },
    }).then((r) => handleResponse<U>(r));

export const handleDelete =
  (api_url: string) =>
  async <U>(path: string, token?: string) =>
    await fetch(`${api_url}${path}`, {
      method: "DELETE",
      // credentials: "include",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
        ...(token !== undefined ? { Authorization: `Bearer ${token}` } : {}),
      },
    }).then((r) => handleResponse<U>(r));

export const handlePost = handleReqWithBody("POST");
export const handlePatch = handleReqWithBody("PATCH");
export const handlePut = handleReqWithBody("PUT");

type LoginData = {
  userName: string;
  password: string;
};

type AuthResponse = {
  id: string;
  userName: string;
  accessToken: string;
};

type SignupData = {
  userName: string;
  password: string;
  role: "Admin" | "Player";
};

class API {
  API_URL: string = process.env.NEXT_PUBLIC_API_URL;
  GET: <U>(path: string, token?: string) => Promise<U>;
  DELETE: <U>(path: string, token?: string) => Promise<U>;
  POST: <T, U>(
    path: string,
    token?: string
  ) => (bodyData: T, extraHeaders?: HeadersInit) => Promise<U>;
  PATCH: <T, U>(
    path: string,
    token?: string
  ) => (bodyData: T, extraHeaders?: HeadersInit) => Promise<U>;

  constructor() {
    this.GET = handleGet(this.API_URL);
    this.POST = handlePost(this.API_URL);
    this.PATCH = handlePatch(this.API_URL);
    this.DELETE = handleDelete(this.API_URL);
  }

  configure(api_url: string) {
    this.API_URL = api_url;

    this.GET = handleGet(this.API_URL);
    this.POST = handlePost(this.API_URL);
    this.PATCH = handlePatch(this.API_URL);
    this.DELETE = handleDelete(this.API_URL);
  }

  async signup(bodyData: SignupData, extraHeaders?: HeadersInit) {
    return await this.POST<SignupData, AuthResponse>("/users/")(
      bodyData,
      extraHeaders
    );
  }

  login(bodyData: LoginData, extraHeaders?: HeadersInit) {
    return this.POST<LoginData, AuthResponse>(`/users/login`)(
      bodyData,
      extraHeaders
    );
  }

  async logout() {
    return await this.GET(`/user/logout`);
  }
}

export default new API();
