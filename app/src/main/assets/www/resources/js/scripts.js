var urlParams = new URLSearchParams(window.location.search);

var errorCode = urlParams.get('errorCode');
var host = urlParams.get('host');

document.getElementById('url').innerHTML = "https://" + host;
document.getElementById('errorCode').innerHTML = errorCode;