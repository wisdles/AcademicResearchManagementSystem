const puppeteer = require('puppeteer');
const fs = require('fs');
const path = require('path');
const dir = __dirname;

(async () => {
  const browser = await puppeteer.launch({ headless: 'new' });
  const page = await browser.newPage();

  const htmlFile = 'file:///' + path.join(dir, 'all6.html').replace(/\\/g, '/');
  await page.goto(htmlFile, { waitUntil: 'networkidle0', timeout: 30000 });
  await new Promise(r => setTimeout(r, 3000));

  const svgCount = await page.evaluate(() => document.querySelectorAll('.mermaid svg').length);
  console.log('SVGs found:', svgCount);

  const diagrams = await page.$$('.d');
  const names = ['图4-1_ER关系图', '图4-2_成果申报类图', '图4-3_审核时序图', '图4-4_催报类图', '图4-5_绩效考核流程图', '图4-6_异常预警流程图'];

  for (let i = 0; i < Math.min(diagrams.length, names.length); i++) {
    const outPath = path.join(dir, names[i] + '.png');
    await diagrams[i].screenshot({ path: outPath, type: 'png' });
    const sz = fs.statSync(outPath).size;
    console.log(names[i] + '.png: ' + sz + ' bytes');
  }

  await browser.close();
  console.log('All 6 diagrams done!');
})();
