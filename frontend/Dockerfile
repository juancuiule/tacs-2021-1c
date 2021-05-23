FROM node:alpine

RUN mkdir -p /usr/src/app

WORKDIR /usr/src/app

ADD . /usr/src/app

RUN npm install && npm run build

EXPOSE 3000

CMD npm start