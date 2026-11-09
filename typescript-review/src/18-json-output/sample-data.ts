/**
 * Sample data shared by the files of lesson 18. NO demo code here, only exports, so importing it prints nothing.
 */
export interface Address {
  city: string;
  zip: string | null;
}

export interface User {
  id: number;
  name: string;
  active: boolean;
  score: number;
  tags: string[];
  address: Address | null;
}

export function sampleUser(): User {
  return { id: 1, name: "Ann", active: true, score: 9.5, tags: ["ts", "node"], address: { city: "Bangkok", zip: null } };
}
