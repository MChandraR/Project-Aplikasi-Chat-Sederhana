//Kode program untuk memulai membuat server 
const express = require('express');
const app = express();
const server = require('http').createServer(app);
const port = 3000;

//Deklarasi socket.io di dalam server
const io = require('socket.io')(server,{
    cors: {
        origin: "*",
        methods: ["GET", "POST"],
        },
        
    });

//Membuat agar server me-listen ke port 3000 di komputer
server.listen(port, () => {
  console.log('Server listening at port %d', port);
});

//Routing untuk resouce yang ada di folder public
app.use(express.static('public'));

//Variabel global untuk socket server
let numUsers = 0;
let UserOnline = [];

//Custom function untuk edit data user pada server
function editUser(user,token){
  for(let i=0;i<UserOnline.length;i++){
    if(user==UserOnline[i][0]) return;
    if(UserOnline[i][0]=="") {
      UserOnline[i][0] = user;
      UserOnline[i][1] = token;
      return;
    }
  }UserOnline.push([user,token]);
}

//Kondisi saat suatu event dari client
io.on('connection', (socket) => {
  let addedUser = false;

   //Ketika user memulai sesi pada client
   socket.on('add user', (username) => {
    if (addedUser) return;
    editUser(username,socket.id);
    socket.username = username;
    console.log("User ",username," added !" + socket.id);
    ++numUsers;
    addedUser = true;
    socket.emit('login', {
      numUsers: numUsers
    });
    socket.broadcast.emit('user joined', {
      username: socket.username,
      numUsers: numUsers,
      onlineUser : UserOnline
    });
    console.log(UserOnline);
  });

  //Ketika menerima pesan baru secara global
  socket.on('new message', (data) => {
    socket.broadcast.emit('new message', {
      username : socket.username,
      message: data
    });
    console.log({
      user : socket.username,
      message: data
    });
  });

  //Ketika client melakukan broadcast atau siaran
  socket.on('broadcast',(message)=>{
    console.log("User : ",socket.username, " melakukan broadcast !");
      socket.broadcast.emit('broadcast',{
        username : socket.username,
        message : message
      });
  });

  //Ketika client mengirim pesan yg bersifat private
  socket.on('personal chat',(username,token,message) =>{
      io.to(token).emit('personal new chat',{
        username: socket.username,
        message : message
      });
    });

  //Event ketika client memberikan status typing sebuah pesan secara private
  socket.on('typing private', (token) => {
    io.to(token).emit('typing private', {
      username: socket.username
    }); console.log("private typing");
  });
  
  //Ketika client meminta data client yang online
  socket.on('stop typing private', (token) => {
    io.to(token).emit('stop typing private', {
      username: socket.username
    });
  });

  
  socket.on('fetch_user', () => {
    socket.emit('user joined', {
      username: socket.username,
      numUsers: numUsers,
      onlineUser : UserOnline
    });

    console.log("Fetching");
  });

  //Event ketika client memberikan status typing sebuah pesan ke global
  socket.on('typing', () => {
    socket.broadcast.emit('typing', {
      username: socket.username
    });
  });



  socket.on('stop typing', () => {
    socket.broadcast.emit('stop typing', {
      username: socket.username
    });
  });


  //Ketika client meminta untuk keluar dari sesi/ disconnect dari socket
  socket.on('disconnect', () => {
    if (addedUser) {
      for(let i=0;i<UserOnline.length;i++){
        if(UserOnline[i][0]==socket.username) UserOnline[i][0] = "";
      }
      console.log(UserOnline);
      --numUsers;
      socket.broadcast.emit('user left', {
        username: socket.username,
        numUsers: numUsers
      });
    }
  });
});