import { createServer } from "node:http";
import { readFile } from "node:fs/promises";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";

const root = dirname(fileURLToPath(import.meta.url));
const MIME = {
  ".html": "text/html; charset=utf-8",
  ".ttf": "font/ttf",
  ".png": "image/png",
  ".js": "text/javascript",
  ".css": "text/css",
};

const port = Number(process.env.PORT || 4721);
const server = createServer(async (req, res) => {
  const url = new URL(req.url, `http://127.0.0.1:${port}`);
  let rel = url.pathname === "/" ? "/index.html" : url.pathname;
  const file = resolve(root, "." + rel);
  if (!file.startsWith(root)) {
    res.writeHead(403);
    res.end();
    return;
  }
  try {
    const data = await readFile(file);
    const ext = file.slice(file.lastIndexOf("."));
    res.writeHead(200, { "content-type": MIME[ext] || "application/octet-stream" });
    res.end(data);
  } catch {
    res.writeHead(404);
    res.end("not found");
  }
});

server.listen(port, "0.0.0.0", () => {
  console.log(`mockups http://127.0.0.1:${port}`);
});
