import { createServer } from "node:http";
import { readFile } from "node:fs/promises";
import { copyFileSync, mkdirSync } from "node:fs";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";
import puppeteer from "puppeteer-core";

const root = dirname(fileURLToPath(import.meta.url));
const artifactDir = "/opt/cursor/artifacts";
const localDir = resolve(root, "export");
mkdirSync(artifactDir, { recursive: true });
mkdirSync(localDir, { recursive: true });

const MIME = {
  ".html": "text/html; charset=utf-8",
  ".ttf": "font/ttf",
};

const server = createServer(async (req, res) => {
  const url = new URL(req.url, "http://127.0.0.1");
  let rel = url.pathname === "/" ? "/datepick-marked.html" : url.pathname;
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

await new Promise((r) => server.listen(0, "127.0.0.1", r));
const port = server.address().port;
const browser = await puppeteer.launch({
  executablePath: "/usr/bin/google-chrome-stable",
  headless: "new",
  args: ["--no-sandbox", "--disable-gpu", "--hide-scrollbars", "--font-render-hinting=none"],
});
const page = await browser.newPage();
await page.setViewport({ width: 390, height: 844, deviceScaleFactor: 3 });
await page.goto(`http://127.0.0.1:${port}/datepick-marked.html`, { waitUntil: "networkidle0" });
await page.evaluate(() => document.fonts.ready);
await new Promise((r) => setTimeout(r, 300));
const name = "时间块-清单日历-完成点.png";
const dest = resolve(artifactDir, name);
await page.screenshot({ path: dest, type: "png" });
copyFileSync(dest, resolve(localDir, name));
copyFileSync(dest, resolve("/workspace/artifacts", name));
await browser.close();
server.close();
console.log(dest);
