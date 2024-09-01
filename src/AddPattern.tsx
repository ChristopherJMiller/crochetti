import { Button, Label, Textarea, TextInput } from "flowbite-react";
import { useEffect, useState } from "react";
import { invoke } from "@tauri-apps/api/core";
import { Row } from "./lib/row";
import { RowSection } from "./RowSection";
import { FaBackspace } from "react-icons/fa";

export function AddPattern() {
  const [selectedRow, setSelectedRow] = useState(0);
  const [rows, setRows] = useState<Row[]>([
    {
      groups: [],
      description: "",
    },
  ]);
  const [stitchList, setStitchList] = useState<string[]>([]);

  useEffect(() => {
    if (stitchList.length === 0) {
      const run = async () => {
        const list: string[] = await invoke("stitch_list");
        setStitchList(list.filter((stitch) => stitch.length > 0));
      };

      run();
    }
  }, [stitchList]);

  const pushStitch = (stitch: string) => {
    const updatedRow = [...rows];

    const lastGroup = updatedRow[selectedRow].groups.length - 1;

    // TODO this does not work to push stitches
    if (updatedRow[selectedRow].groups[lastGroup] === undefined) {
      updatedRow[selectedRow].groups[lastGroup] = {
        stitches: [stitch],
        repeats: 1,
      };
    } else {
      updatedRow[selectedRow].groups[lastGroup].stitches.push(stitch);
    }

    setRows(updatedRow);
  };

  const buttonList = stitchList.map((stitch) => (
    <Button color="gray" key={stitch} onClick={() => pushStitch(stitch)}>
      <span className="text-xs self-center">{stitch}</span>
    </Button>
  ));

  const rowSections = rows.map((row, index) => (
    <RowSection row={row} index={index} />
  ));

  console.log(rows);

  return (
    <div className="flex flex-col gap-3 w-screen h-screen pt-3 px-4">
      <h1 className="text-2xl font-semibold">Add Pattern</h1>
      <form>
        <div>
          <div className="my-2 block">
            <Label htmlFor="name" value="Name" />
          </div>
          <TextInput id="name" required />
        </div>
        <div>
          <div className="my-2 block">
            <Label htmlFor="description" value="Description" />
          </div>
          <Textarea id="description" required rows={4} />
        </div>
      </form>
      <div>{rowSections}</div>
      <div>
        <Button.Group>{buttonList}</Button.Group>
        <Button.Group>
          <Button color="gray">
            <span className="text-xs self-center">(</span>
          </Button>
          <Button color="gray">
            <span className="text-xs self-center">x</span>
          </Button>
          <Button color="gray">
            <span className="text-xs self-center">
              <FaBackspace />
            </span>
          </Button>
        </Button.Group>
      </div>
    </div>
  );
}
