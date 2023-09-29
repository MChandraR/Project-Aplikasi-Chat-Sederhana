const socket = require("socket.io-client")("http://192.168.1.8:3000/");

socket.on("connect_error", (err) => {
  console.log(`connect_error due to ${err.message}`);
});

socket.on("new message", (data) => {
  console.log(data);
});

socket.on("typing", (data) => {
  console.log(data);
});

socket.on("user joined", (data) => {
  console.log("User join " ,data);
});

socket.on("user left", (data) => {
  console.log("User disconnect " ,data);
});

socket.on("personal chat", (message) => {
  console.log(message);
});

socket.emit("add user","Syawal");
socket.emit("broadcast","Hallo semuanya !");


function greet(){
  socket.emit("new message","Nice !");
}

greet();
