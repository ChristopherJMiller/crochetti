import { FaPlus } from "react-icons/fa";
import { FabButton } from "./FabButton";
import { useNavigate } from "react-router-dom";

export function App() {
  const navigate = useNavigate();

  return (
    <div className="static">
      <div className="flex flex-col gap-1 justify-between w-screen h-screen pt-5">
        <h1 className="text-3xl font-semibold pl-4">Patterns</h1>
      </div>
      <div className="absolute bottom-5 right-5">
        <FabButton
          icon={<FaPlus className="w-4 h-4 mr-2 self-center" />}
          text="Add Pattern"
          onClick={() => navigate("/add")}
        />
      </div>
    </div>
  );
}
