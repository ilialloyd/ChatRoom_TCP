This is Local Chat Room App
You can connect as many client as you can.
Everyone nned to download progtram and run the server first.
If only one person run server it will be enought

you can talk with command line , 
but you need to do new steps before

1. As a client you need to go src directory first
2. javac Client.java   run this
3. java Client.java  run this
4. Set the nickname and you are ready

/nick - after this keyword you can change your nickname
/quit - you leave the chatroom

Main point!
In the Server side, You should add your Home Wifi or Local ip
it means you should change this - "127.0.0.1 "

client = new Socket("127.0.0.1 ", 9999); 