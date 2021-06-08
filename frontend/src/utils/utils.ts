import { MatchState } from "../../types";

export const parseMatchState = (matchState: MatchState) => {
  if (matchState["BattleResult"]) {
    return { state: "battleResult", ...matchState["BattleResult"] };
  } else if (matchState["PreBattle"]) {
    return { state: "preBattle", ...matchState["PreBattle"] };
  } else if (matchState["Draw"]) {
    return { state: "draw", ...matchState["Draw"] };
  } else if (matchState["Finished"]) {
    return { state: "finished", ...matchState["Finished"] };
  }
};
