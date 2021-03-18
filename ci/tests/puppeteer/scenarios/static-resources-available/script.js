const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
	const browser = await puppeteer.launch({
		ignoreHTTPSErrors: true
	});
	const page = await browser.newPage();
	let result = await page.goto('https://localhost:8443/cas/js/cas.js');
	console.log(result.status());
	assert(result.status() === 200);

	await browser.close();
})();
