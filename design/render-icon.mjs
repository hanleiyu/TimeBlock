import { mkdirSync, copyFileSync, writeFileSync } from "node:fs";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";
import { createServer } from "node:http";
import { readFile } from "node:fs/promises";
import puppeteer from "../mockups/node_modules/puppeteer-core/lib/esm/puppeteer/puppeteer-core.js";

const root = dirname(fileURLToPath(import.meta.url));
const repo = resolve(root, "..");
mkdirSync(resolve(repo, "app/src/main"), { recursive: true });
mkdirSync(resolve(repo, "artifacts"), { recursive: true });
mkdirSync("/opt/cursor/artifacts", { recursive: true });

const MIME = { ".html": "text/html; charset=utf-8" };
const server = createServer(async (req, res) => {
  const file = resolve(root, "." + new URL(req.url, "http://127.0.0.1").pathname);
  if (!file.startsWith(root)) { res.writeHead(403); res.end(); return; }
  try {
    const data = await readFile(file);
    res.writeHead(200, { "content-type": MIME[".html"] });
    res.end(data);
  } catch {
    res.writeHead(404); res.end();
  }
});
await new Promise((r) => server.listen(0, "127.0.0.1", r));
const port = server.address().port;

const browser = await puppeteer.launch({
  executablePath: "/usr/bin/google-chrome-stable",
  headless: "new",
  args: ["--no-sandbox", "--disable-gpu", "--hide-scrollbars", "--force-color-profile=srgb"],
});

const page = await browser.newPage();
await page.goto(`http://127.0.0.1:${port}/icon.html`, { waitUntil: "networkidle0" });

const png512 = resolve(repo, "app/src/main/ic_launcher-playstore.png");
const el = await page.$("#png512");
await el.screenshot({ path: png512, type: "png", omitBackground: false });

await page.setViewport({ width: 1400, height: 720, deviceScaleFactor: 2 });
const sheet = await page.$("#sheet");
const preview = resolve(repo, "artifacts/app-icon.png");
await sheet.screenshot({ path: preview, type: "png" });

copyFileSync(png512, resolve(repo, "artifacts/ic_launcher-512.png"));
copyFileSync(png512, "/opt/cursor/artifacts/ic_launcher-512.png");
copyFileSync(preview, "/opt/cursor/artifacts/app-icon.png");

console.log("wrote", png512);
console.log("wrote", preview);
await browser.close();
server.close();
