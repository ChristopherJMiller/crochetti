export interface RowGroup {
  stitches: string[];
  repeats: number;
}

export interface Row {
  groups: RowGroup[];
  description: string;
}
