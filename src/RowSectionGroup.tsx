import { Kbd, TextInput } from "flowbite-react";
import { RowGroup } from "./lib/row";

interface RowSectionGroupProps {
  rowGroup: RowGroup;
}

export const RowSectionGroup: React.FC<RowSectionGroupProps> = ({
  rowGroup,
}) => {
  const sections = rowGroup.stitches.map((stitch) => (
    <Kbd>
      <span>{stitch}</span>
      <TextInput id="small" type="text" sizing="sm" />
    </Kbd>
  ));

  return <div>{sections}</div>;
};
