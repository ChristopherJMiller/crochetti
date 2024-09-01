import { Button } from "flowbite-react";

interface FabButtonProps {
  icon: React.ReactNode;
  text: string;
  onClick: () => void;
}

export const FabButton: React.FC<FabButtonProps> = ({
  icon,
  text,
  onClick,
}) => (
  <Button
    size="lg"
    pill
    className="flex flex-row gap-4 items-center"
    onClick={onClick}
  >
    {icon}
    <span>{text}</span>
  </Button>
);
