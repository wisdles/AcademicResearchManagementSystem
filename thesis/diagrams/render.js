const puppeteer = require('puppeteer');
const fs = require('fs');
const path = require('path');

const dir = __dirname;
const htmlPath = path.join(dir, 'all_diagrams.html');

(async () => {
  const browser = await puppeteer.launch({ headless: 'new' });
  const page = await browser.newPage();
  await page.goto('file:///' + htmlPath.replace(/\\/g, '/'), { waitUntil: 'networkidle0', timeout: 30000 });

  // Wait for mermaid to render
  await new Promise(r => setTimeout(r, 3000));

  // Check if mermaid rendered
  const svgCount = await page.evaluate(() => document.querySelectorAll('.mermaid svg').length);
  console.log('SVG diagrams found:', svgCount);

  const diagrams = await page.$$('.diagram');
  console.log('Diagram containers:', diagrams.length);

  const names = ['图4-1_ER关系图', '图4-2_成果申报类图', '图4-5_绩效考核流程图', '图4-6_异常预警流程图'];

  for (let i = 0; i < diagrams.length; i++) {
    const el = diagrams[i];
    const name = names[i] || ('diagram_' + i);
    await el.screenshot({ path: path.join(dir, name + '.png'), type: 'png' });
    const stats = fs.statSync(path.join(dir, name + '.png'));
    console.log(name + '.png: ' + stats.size + ' bytes');
  }

  await browser.close();
  console.log('Done!');
})();
