import { Label, Textarea } from "flowbite-react";
import { Row } from "./lib/row";
import { RowSectionGroup } from "./RowSectionGroup";

interface RowSectionProps {
  row: Row;
  index: number;
}

export const RowSection: React.FC<RowSectionProps> = ({ index, row }) => {
  const rowGroups = row.groups.map((group) => (
    <RowSectionGroup rowGroup={group} />
  ));

  return (
    <div>
      <h2 className="text-xl">Row {index + 1}</h2>
      <div>
        <div className="my-2 block">
          <Label htmlFor={`description${index}`} value="Description" />
        </div>
        <Textarea id={`description${index}`} required rows={2} />
      </div>
      {rowGroups}
    </div>
  );
};
