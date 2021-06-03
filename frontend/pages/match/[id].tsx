import { useRouter } from "next/router";
import React from "react";

const CardInDeck = ({
  first,
  card,
}: {
  first: boolean;
  card?: typeof cards[0];
}) => {
  return (
    <div
      style={{
        height: "500px",
        width: "300px",
        background: "gray",
        border: "1px solid black",
        borderRadius: "20px",
        marginTop: first ? "20px" : "-480px",
        overflow: "hidden",
      }}
    >
      {card && (
        <>
          <div
            style={{
              width: "100%",
              height: "200px",
              background: `url(${card.image.url})`,
              backgroundPosition: "center",
              backgroundSize: "cover",
              backgroundRepeat: "no-repeat",
            }}
          ></div>
          <div style={{padding: "8px"}}>
            <span>{card.name}</span>
            <br />
            {card.biography ? (
              <>
                <span>{card.biography["full-name"] || ""}</span>
                <br />
                <span>{card.biography.publisher}</span>
              </>
            ) : null}
            <ul
              style={{
                paddingLeft: "0px",
                listStyle: "none",
              }}
            >
              {Object.entries(card.powerstats).map(([powerstat, value]) => {
                return (
                  <li key={powerstat}>
                    <a href="#">{powerstat}: {value}</a>
                  </li>
                );
              })}
            </ul>
          </div>
        </>
      )}
    </div>
  );
};

const Deck = () => (
  <div>
    {cards.map((c, i) => {
      return <CardInDeck first={i === 0} />;
    })}
  </div>
);

export default function Match() {
  const router = useRouter();
  const { id } = router.query;
  return (
    <>
      Match {id}
      <div
        style={{
          maxWidth: "1200px",
          padding: "24px",
          margin: "20px auto",
          display: "flex",
          flexDirection: "row",
          justifyContent: "space-between",
        }}
      >
        <CardInDeck card={cards[0]} first />
        <Deck />
        <CardInDeck first />
      </div>
    </>
  );
}

const cards = [
  {
    id: "577",
    name: "Scarlet Spider",
    powerstats: {
      intelligence: "75",
      strength: "53",
      speed: "60",
      durability: "74",
      power: "46",
      combat: "56",
    },
    biography: {
      "full-name": "Benjamin Reilly",
      "alter-egos": "Spider-Carnage",
      aliases: ["Spider-Clone", "Spider-Man"],
      "place-of-birth": "Place of birth unknown",
      "first-appearance":
        "Amazing Spider-Man # 149 (as Spider-Clone); Web of Spider-Man # 117 (as Ben Reilly)",
      publisher: "Spider-Carnage",
      alignment: "good",
    },
    appearance: {
      gender: "Male",
      race: "Human",
      height: ["5'10'", "178 cm"],
      weight: ["165 lb", "74 kg"],
      "eye-color": "Blue",
      "hair-color": "Blond",
    },
    work: {
      occupation: "Crime-fighter",
      base: "-",
    },
    connections: {
      "group-affiliation": "None; formerly New Warriors",
      relatives:
        "Peter Parker (clone of), May Parker (Aunt), Ben Parker (Uncle)",
    },
    image: {
      url: "https://www.superherodb.com/pictures2/portraits/10/100/174.jpg",
    },
  },
  {
    id: "578",
    name: "Scarlet Spider II",
    powerstats: {
      intelligence: "88",
      strength: "55",
      speed: "60",
      durability: "40",
      power: "37",
      combat: "56",
    },
    biography: {
      "full-name": "Kaine Parker",
      "alter-egos": "No alter egos found.",
      aliases: ["Tarantula", "Kaine", "Ara�a Escarlata"],
      "place-of-birth": "-",
      "first-appearance": "Web of Spider-Man #119 (December, 1994)",
      publisher: "Marvel Comics",
      alignment: "good",
    },
    appearance: {
      gender: "Male",
      race: "Clone",
      height: ["6'4", "193 cm"],
      weight: ["250 lb", "113 kg"],
      "eye-color": "Brown",
      "hair-color": "Brown",
    },
    work: {
      occupation: "Fugitive",
      base: "-",
    },
    connections: {
      "group-affiliation": "-",
      relatives:
        "Miles Warren (creator), Peter Parker (Spider-Man, genetic template), Ben Reilly (Scarlet Spider, fellow clone, deceased), Spidercide (fellow clone, allegedly deceased), Guardian (fellow clone, deceased), Jack (fellow clone, deceased)",
    },
    image: {
      url: "https://www.superherodb.com/pictures2/portraits/10/100/1536.jpg",
    },
  },
  {
    id: "617",
    name: "Spider-Carnage",
    powerstats: {
      intelligence: "null",
      strength: "69",
      speed: "null",
      durability: "null",
      power: "null",
      combat: "null",
    },
    biography: {
      "full-name": "Benjamin Reilly",
      "alter-egos": "Scarlet Spider",
      aliases: ["-"],
      "place-of-birth": "-",
      "first-appearance": "-",
      publisher: "Scarlet Spider",
      alignment: "bad",
    },
    appearance: {
      gender: "Male",
      race: "Symbiote",
      height: ["-", "0 cm"],
      weight: ["- lb", "0 kg"],
      "eye-color": "-",
      "hair-color": "-",
    },
    work: {
      occupation: "-",
      base: "-",
    },
    connections: {
      "group-affiliation": "-",
      relatives: "-",
    },
    image: {
      url: "https://www.superherodb.com/pictures2/portraits/10/100/957.jpg",
    },
  },
  {
    id: "618",
    name: "Spider-Girl",
    powerstats: {
      intelligence: "63",
      strength: "38",
      speed: "60",
      durability: "65",
      power: "53",
      combat: "75",
    },
    biography: {
      "full-name": "May 'Mayday' Parker",
      "alter-egos": "No alter egos found.",
      aliases: ["-"],
      "place-of-birth": "New York City, New York",
      "first-appearance": "What If? Vol 2 #105 (February, 1998)",
      publisher: "Marvel Comics",
      alignment: "good",
    },
    appearance: {
      gender: "Female",
      race: "Human",
      height: ["5'7", "170 cm"],
      weight: ["119 lb", "54 kg"],
      "eye-color": "Blue",
      "hair-color": "Brown",
    },
    work: {
      occupation: "-",
      base: "New York City, New York",
    },
    connections: {
      "group-affiliation": "-",
      relatives: "-",
    },
    image: {
      url: "https://www.superherodb.com/pictures2/portraits/10/100/480.jpg",
    },
  },
  {
    id: "619",
    name: "Spider-Gwen",
    powerstats: {
      intelligence: "75",
      strength: "55",
      speed: "63",
      durability: "70",
      power: "66",
      combat: "70",
    },
    biography: {
      "full-name": "Gwen Stacy",
      "alter-egos": "No alter egos found.",
      aliases: ["Spider-Woman"],
      "place-of-birth": "-",
      "first-appearance": "Edge of Spider-Verse #2",
      publisher: "Marvel Comics",
      alignment: "good",
    },
    appearance: {
      gender: "Female",
      race: "Human",
      height: ["5'5", "165 cm"],
      weight: ["125 lb", "56 kg"],
      "eye-color": "Blue",
      "hair-color": "Blond",
    },
    work: {
      occupation: "-",
      base: "-",
    },
    connections: {
      "group-affiliation":
        "Warriors of the Great Web; formerly Mary Janes, Spider-Army",
      relatives: "George Stacy (father), Helen Stacy (mother, deceased)",
    },
    image: {
      url: "https://www.superherodb.com/pictures2/portraits/10/100/10507.jpg",
    },
  },
  {
    id: "620",
    name: "Spider-Man",
    powerstats: {
      intelligence: "90",
      strength: "55",
      speed: "67",
      durability: "75",
      power: "74",
      combat: "85",
    },
    biography: {
      "full-name": "Peter Parker",
      "alter-egos": "No alter egos found.",
      aliases: [
        "Spiderman",
        "Bag-Man",
        "Black Marvel",
        "Captain Universe",
        "Dusk",
        "Green Hood",
        "Hornet",
        "Mad Dog 336",
        "Peter Palmer",
        "Prodigy",
        "Ricochet",
        "Scarlet Spider",
        "Spider-Boy",
        "Spider-Hulk",
        "Spider-Morphosis",
      ],
      "place-of-birth": "New York, New York",
      "first-appearance": "Amazing Fantasy #15",
      publisher: "Marvel Comics",
      alignment: "good",
    },
    appearance: {
      gender: "Male",
      race: "Human",
      height: ["5'10", "178 cm"],
      weight: ["165 lb", "74 kg"],
      "eye-color": "Hazel",
      "hair-color": "Brown",
    },
    work: {
      occupation: "Freelance photographer, teacher",
      base: "New York, New York",
    },
    connections: {
      "group-affiliation":
        "Member of the Avengers, formerly member of Outlaws, alternate Fantastic Four",
      relatives:
        "Richard Parker (father, deceased), Mary Parker(mother, deceased), Benjamin Parker (uncle, deceased), May Parker (aunt), Mary Jane Watson-Parker (wife), May Parker (daughter, allegedly deceased)",
    },
    image: {
      url: "https://www.superherodb.com/pictures2/portraits/10/100/133.jpg",
    },
  },
  {
    id: "621",
    name: "Spider-Man",
    powerstats: {
      intelligence: "null",
      strength: "57",
      speed: "null",
      durability: "null",
      power: "null",
      combat: "null",
    },
    biography: {
      "full-name": "Miguel O'Hara",
      "alter-egos": "No alter egos found.",
      aliases: ["Spider-Man 2099", "Spiderman"],
      "place-of-birth": "-",
      "first-appearance": "Amazing Spider-Man #365 (August, 1992)",
      publisher: "Marvel Comics",
      alignment: "good",
    },
    appearance: {
      gender: "-",
      race: "Human",
      height: ["5'10", "178 cm"],
      weight: ["170 lb", "77 kg"],
      "eye-color": "Red",
      "hair-color": "Brown",
    },
    work: {
      occupation: "CEO of Alchemax Corporation, Executive Assistant",
      base: "-",
    },
    connections: {
      "group-affiliation": "-",
      relatives: "-",
    },
    image: {
      url: "https://www.superherodb.com/pictures2/portraits/10/100/479.jpg",
    },
  },
  {
    id: "622",
    name: "Spider-Man",
    powerstats: {
      intelligence: "null",
      strength: "58",
      speed: "null",
      durability: "null",
      power: "null",
      combat: "null",
    },
    biography: {
      "full-name": "Miles Morales",
      "alter-egos": "No alter egos found.",
      aliases: ["Spiderman"],
      "place-of-birth": "-",
      "first-appearance": "Ultimate Comics Fallout #4 (October, 2011)",
      publisher: "Marvel Comics",
      alignment: "good",
    },
    appearance: {
      gender: "Male",
      race: "Human",
      height: ["5'2", "157 cm"],
      weight: ["125 lb", "56 kg"],
      "eye-color": "Brown",
      "hair-color": "Black",
    },
    work: {
      occupation: "Student, adventurer, vigilante",
      base: "-",
    },
    connections: {
      "group-affiliation": "-",
      relatives: "-",
    },
    image: {
      url: "https://www.superherodb.com/pictures2/portraits/10/100/10647.jpg",
    },
  },
  {
    id: "623",
    name: "Spider-Woman",
    powerstats: {
      intelligence: "56",
      strength: "42",
      speed: "42",
      durability: "60",
      power: "68",
      combat: "70",
    },
    biography: {
      "full-name": "Jessica Drew",
      "alter-egos": "No alter egos found.",
      aliases: [
        "Arachne",
        "Ariadne Hyde",
        "Dark Angel",
        "Dark Angel of San Francisco",
      ],
      "place-of-birth": "London, England",
      "first-appearance": "Marvel Spotlight #32 (February, 1977)",
      publisher: "Marvel Comics",
      alignment: "good",
    },
    appearance: {
      gender: "Female",
      race: "Human",
      height: ["5'10", "178 cm"],
      weight: ["130 lb", "59 kg"],
      "eye-color": "Green",
      "hair-color": "Black",
    },
    work: {
      occupation:
        "Form agent of HYDRA, former bounty hunter, private investigator, adventurer",
      base: "-",
    },
    connections: {
      "group-affiliation": "Former agent of HYDRA",
      relatives: "Jonathan (father, deceased), Merriem (mother, deceased)",
    },
    image: {
      url: "https://www.superherodb.com/pictures2/portraits/10/100/481.jpg",
    },
  },
  {
    id: "624",
    name: "Spider-Woman II",
    powerstats: {
      intelligence: "null",
      strength: "null",
      speed: "null",
      durability: "null",
      power: "null",
      combat: "null",
    },
    biography: {
      "full-name": "",
      "alter-egos": "No alter egos found.",
      aliases: ["-"],
      "place-of-birth": "-",
      "first-appearance": "-",
      publisher: "Marvel Comics",
      alignment: "good",
    },
    appearance: {
      gender: "Female",
      race: "null",
      height: ["-", "0 cm"],
      weight: ["- lb", "0 kg"],
      "eye-color": "-",
      "hair-color": "-",
    },
    work: {
      occupation: "-",
      base: "-",
    },
    connections: {
      "group-affiliation": "-",
      relatives: "-",
    },
    image: {
      url: "https://www.superherodb.com/pictures2/portraits/10/100/483.jpg",
    },
  },
  {
    id: "625",
    name: "Spider-Woman III",
    powerstats: {
      intelligence: "50",
      strength: "48",
      speed: "27",
      durability: "42",
      power: "60",
      combat: "28",
    },
    biography: {
      "full-name": "Martha Franklin",
      "alter-egos": "No alter egos found.",
      aliases: ["-"],
      "place-of-birth": "Rochester, New York",
      "first-appearance":
        "(cameo) Spectacular Spider-Man #263 (1998); (full) Amazing Spider-Man #441 (1998)",
      publisher: "Marvel Comics",
      alignment: "good",
    },
    appearance: {
      gender: "Female",
      race: "null",
      height: ["5'8", "173 cm"],
      weight: ["123 lb", "55 kg"],
      "eye-color": "Brown",
      "hair-color": "Brown",
    },
    work: {
      occupation: "-",
      base: "-",
    },
    connections: {
      "group-affiliation": "Formerly Gathering of the Five",
      relatives:
        "Jeremy Franklin (father, deceased), Bernice Franklin (mother, deceased), J. Jonah Jameson (foster father), Marla Madison (foster mother)",
    },
    image: {
      url: "https://www.superherodb.com/pictures2/portraits/10/100/482.jpg",
    },
  },
  {
    id: "626",
    name: "Spider-Woman IV",
    powerstats: {
      intelligence: "null",
      strength: "null",
      speed: "null",
      durability: "null",
      power: "null",
      combat: "null",
    },
    biography: {
      "full-name": "Charlotte Witter",
      "alter-egos": "No alter egos found.",
      aliases: ["Spider-Woman"],
      "place-of-birth": "-",
      "first-appearance": "Amazing Spider-Man Volume 2 #5 (# 446)",
      publisher: "Marvel Comics",
      alignment: "bad",
    },
    appearance: {
      gender: "Female",
      race: "null",
      height: ["5'10", "178 cm"],
      weight: ["128 lb", "58 kg"],
      "eye-color": "Red",
      "hair-color": "White",
    },
    work: {
      occupation: "Fashion designer, professional criminal",
      base: "New York City",
    },
    connections: {
      "group-affiliation": "-",
      relatives: "Cassandra Webb (aka Madame Web, grandmother)",
    },
    image: {
      url: "https://www.superherodb.com/pictures2/portraits/10/100/883.jpg",
    },
  },
];
