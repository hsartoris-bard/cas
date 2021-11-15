const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');
const fs = require('fs');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.setRequestInterception(true);

    let args = process.argv.slice(2);
    let config = JSON.parse(fs.readFileSync(args[0]));
    assert(config != null)

    console.log(`Certificate file: ${config.trustStoreCertificateFile}`);

    const certBuffer = fs.readFileSync(config.trustStoreCertificateFile);
    const certHeader = certBuffer.toString().replace(/\n/g, " ").replace(/\r/g,"");

    console.log(`ssl-client-cert-from-proxy: ${certHeader}`);

    page.on('request', request => {
        let data = {
            'method': 'GET',
            'headers': {
                ...request.headers(),
                'ssl-client-cert-from-proxy': certHeader
            },
        };
        request.continue(data);
    });

    await page.goto("https://localhost:8443/cas/login");
    await page.waitForTimeout(5000)

    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    await cas.assertInnerTextContains(page, "#content div p", "CN=mmoayyed, OU=dev, O=bft, L=mt, C=world");

    await page.goto("https://localhost:8443/cas/login?service=https://github.com");
    await page.waitForTimeout(5000)
    await assertFailure(page);
    await browser.close();
})();

async function assertFailure(page) {
    await cas.assertInnerText(page, "#loginErrorsPanel p", "Service access denied due to missing privileges.")
    await page.waitForTimeout(1000)
}


