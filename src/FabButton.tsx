import { Button } from "flowbite-react";

interface FabButtonProps {
  icon: React.ReactNode;
  text: string;
}

export const FabButton: React.FC<FabButtonProps> = ({ icon, text }) => (
  <Button size="lg" pill className="flex flex-row gap-4 items-center">
    {icon}
    <span>{text}</span>
  </Button>
);
