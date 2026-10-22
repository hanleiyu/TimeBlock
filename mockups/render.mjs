import { mkdirSync, copyFileSync } from "node:fs";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";
import { createServer } from "node:http";
import { readFile } from "node:fs/promises";
import puppeteer from "puppeteer-core";

const root = dirname(fileURLToPath(import.meta.url));
const outDir = resolve(root, "export");
const artifactDir = "/opt/cursor/artifacts";
mkdirSync(outDir, { recursive: true });
mkdirSync(artifactDir, { recursive: true });

const MIME = {
  ".html": "text/html; charset=utf-8",
  ".ttf": "font/ttf",
  ".css": "text/css",
  ".js": "text/javascript",
};

const server = createServer(async (req, res) => {
  const url = new URL(req.url, "http://127.0.0.1");
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

await new Promise((r) => server.listen(0, "127.0.0.1", r));
const port = server.address().port;
const base = `http://127.0.0.1:${port}`;

const browser = await puppeteer.launch({
  executablePath: "/usr/bin/google-chrome-stable",
  headless: "new",
  args: [
    "--no-sandbox",
    "--disable-gpu",
    "--hide-scrollbars",
    "--font-render-hinting=none",
    "--force-color-profile=srgb",
  ],
});

const shots = [
  { id: "1", name: "01-today-wake.png" },
  { id: "2", name: "02-today-grid.png" },
  { id: "2a", name: "option-A.png" },
  { id: "2b", name: "option-B.png" },
  { id: "2c", name: "option-C.png" },
  { id: "3", name: "03-type-picker.png" },
  { id: "4", name: "04-stats.png" },
  { id: "4off", name: "app-stats-elapsed.png" },
  { id: "5", name: "05-settings.png" },
  { id: "6", name: "06-type-manage.png" },
];

async function waitFonts(page) {
  await page.evaluate(() => document.fonts.ready);
  await new Promise((r) => setTimeout(r, 250));
}

for (const shot of shots) {
  const page = await browser.newPage();
  await page.setViewport({ width: 390, height: 844, deviceScaleFactor: 3 });
  await page.goto(`${base}/index.html?shot=${shot.id}`, { waitUntil: "networkidle0" });
  await waitFonts(page);
  const dest = resolve(outDir, shot.name);
  await page.screenshot({ path: dest, type: "png", omitBackground: false });
  copyFileSync(dest, resolve(artifactDir, shot.name));
  console.log("wrote", dest);
  await page.close();
}

const overview = await browser.newPage();
await overview.setViewport({ width: 2680, height: 1100, deviceScaleFactor: 2 });
await overview.goto(`${base}/index.html?overview=1`, { waitUntil: "networkidle0" });
await waitFonts(overview);
const overviewPath = resolve(outDir, "00-overview.png");
await overview.screenshot({ path: overviewPath, type: "png", fullPage: true });
copyFileSync(overviewPath, resolve(artifactDir, "00-overview.png"));
console.log("wrote", overviewPath);

const aliases = [
  ["01-today-wake.png", "app-today-asleep.png"],
  ["02-today-grid.png", "app-today-awake.png"],
  ["03-type-picker.png", "app-type-sheet.png"],
  ["04-stats.png", "app-stats.png"],
  ["04-stats.png", "app-stats-all.png"],
  ["05-settings.png", "app-settings.png"],
];
for (const [src, dest] of aliases) {
  copyFileSync(resolve(outDir, src), resolve(artifactDir, dest));
  copyFileSync(resolve(outDir, src), resolve(root, "../artifacts", dest));
}
await overview.close();

await browser.close();
server.close();
