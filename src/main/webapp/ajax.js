function createRequest() {

   var xmlhttp = false;

        try {
                xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
        } catch (e) {
                try {
                        xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
                } catch (E) {
                        // do nothing...
                }
        }


        if (!xmlhttp && typeof XMLHttpRequest != 'undefined') {
                try {
                        xmlhttp = new XMLHttpRequest();
                } catch (e) {
                        // do nothing...
                }
        }

        return xmlhttp;
}

function sendRequest(uri, method, callback) {
  var req = createRequest();
  if (!method) method = 'GET';
  req.open(method.toUpperCase(), uri, true);
  req.onreadystatechange = function() { callback(req); };
  req.send(null);
}
