(function () {
    let message = {
        receiver: "antiCaptchaPlugin",
        data: {'options': {'antiCaptchaApiKey': 'YOUR-ANTI-CAPTCHA-API-KEY'}},
        type: "setOptions"
    };
    window.postMessage(message, window.location.href);
})();