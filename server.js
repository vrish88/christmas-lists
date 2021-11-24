const http = require("http");
const path = require("path");
const fs = require("fs");
const jsdom = require("jsdom");

http
  .createServer((req, res) => {
    if (req.method == "GET") {
      fs.readFile(getFile(req), function (err, data) {
        if (err) {
          res.writeHead(404);
          res.end(JSON.stringify(err));
          return;
        }
        res.writeHead(200);
        res.end(data);
      });
    } else if (req.method == "POST") {
      fs.readFile(getFile(req), function (err, data) {
        if (err) {
          res.writeHead(404);
          res.end(JSON.stringify(err));
          return;
        }
        let body = "";
        req.on("data", (chunk) => (body += chunk));
        req.on("end", () => {
          const doc = new jsdom.JSDOM(data).window.document;

          doc.querySelector('[data-id="christmas-items"]').replaceChildren(
            ...new URLSearchParams(body).getAll("christmas-items").map((item) => {
              const li = doc.createElement("li");
              li.textContent = item;

              return li;
            })
          );

          res.writeHead(200);
          res.end(doc.documentElement.innerHTML);
        });
      });
    }
  })
  .listen(8000);
function getFile(req) {
  return path.normalize(__dirname + req.url);
}
