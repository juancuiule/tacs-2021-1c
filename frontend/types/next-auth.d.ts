import NextAuth from "next-auth"

declare module "next-auth" {
  interface User {
    id: number;
    userName: string;
    role: string;
  }

  interface Session {
    user: User;
  }

}