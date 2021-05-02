import { useRouter } from "next/router";

const DeckDetail = () => {
  const router = useRouter();
  const { id } = router.query;

  return (
    <div className="container">
      <main>
        <h1 className="title">Mazo con id: {id}</h1>
      </main>
    </div>
  );
};

export default DeckDetail;
