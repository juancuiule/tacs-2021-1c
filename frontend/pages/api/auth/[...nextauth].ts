import NextAuth from 'next-auth';
import { signIn } from 'next-auth/client';
import Providers from 'next-auth/providers';

const providers = [
  Providers.Credentials({
    name: 'Credentials',
    authorize: async credentials => {
      const response = await fetch(`${process.env.API_BASE}/users/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userName: credentials.userName,
          password: credentials.password,
        }),
      });

      const user = await response.json();

      if (user) {
        return user;
      } else {
        return null;
      }
    },
  }),
];

export default NextAuth({
  providers,
  secret: process.env.SECRET,
  session: {
    jwt: true,
  },

  jwt: {
    // A secret to use for key generation (you should set this explicitly)
    // secret: 'INp8IvdIyeMcoGAgFGoA61DdBglwwSqnXJZkgz8PSnw',
    // Set to true to use encryption (default: false)
    // encryption: true,
    // You can define your own encode/decode functions for signing and encryption
    // if you want to override the default behaviour.
    // encode: async ({ secret, token, maxAge }) => {},
    // decode: async ({ secret, token, maxAge }) => {},
  },

  pages: {
    signIn: '/auth/signin',
    // signOut: '/auth/signout', // Displays form with sign out button
    // error: '/auth/error', // Error code passed in query string as ?error=
    // newUser: null // If set, new users will be directed here on first sign in
  },

  // Callbacks are asynchronous functions you can use to control what happens
  // when an action is performed.
  // https://next-auth.js.org/configuration/callbacks
  callbacks: {
    // async signIn(user, account, profile) { return true },
    // async redirect(url, baseUrl) { return baseUrl },
    // async session(session, user) { return session },
    // async jwt(token, user, account, profile, isNewUser) { return token }
  },

  // Events are useful for logging
  // https://next-auth.js.org/configuration/events
  events: {},

  // Enable debug messages in the console if you are having problems
  debug: false,
});
