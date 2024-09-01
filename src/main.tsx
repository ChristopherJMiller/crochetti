import React from "react";
import ReactDOM from "react-dom/client";
import { App } from "./App";
import "./index.css";
import { createMemoryRouter, RouterProvider } from "react-router-dom";
import { AddPattern } from "./AddPattern";

const router = createMemoryRouter([
  {
    path: "/",
    element: <App />,
  },
  {
    path: "/add",
    element: <AddPattern />,
  },
]);

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>
);
