/*=====================
			Boilerplate
=====================*/
var express = require('express');
var bodyParser = require('body-parser');
var crypto = require('crypto');
var uuid = require('node-uuid');
var app = express();
app.use(bodyParser());
app.set('port', (process.env.PORT || portNum));
var Sequelize = require('sequelize');
var sequelize = new Sequelize('databaseName','username','password', {
	host: "localhost",
	port: 3306
});


/*============================
			Routing Functions
============================*/

// given a username and password
// on successful login returns a unique session id
app.post('/user/login',function(req, res) {
	var username = req.body.username;
	var password = req.body.password;
	var test;
	if(username.length < 1 || password.length < 1) {
		res.send({type:"error", message: "Invalid Credentials"});
		return;
	}
	sequelize.query('SELECT * FROM Users WHERE username = "' + username + '" LIMIT 1').on('success', function(data) {
		if(data != '[]') {
			var dbHash = data[0].hash;
			var salt = data[0].salt;
			var userHash = stringifyHex(crypto.pbkdf2Sync(password, salt, 200000, 20));
			if(dbHash === userHash) {
				var sessionId = stringifyHex(crypto.randomBytes(10));
				sequelize.query('UPDATE Users SET SessionID = "' + sessionId + '" WHERE Username = "' + username + '"')
				.on('success',function(sessionData) {
					data[0].SessionID = sessionId;
					delete data[0].UserID
					delete data[0].hash;
					delete data[0].salt;
					res.send({type:"success", message: data[0]});
				});
			} else {
				res.send({type: "error", message: "Incorrect Credentials."});
			}
		} else {
			res.send({type: "error", message: "Incorrect Credentials."});
		}
	});
});

// Takes username and sessionid
// Returns Success
app.post('/user/logout',function(req,res) {
	var username = req.body.username;
	var sessionId = req.body.sessionid;
	sequelize.query('UPDATE Users SET SessionId = NULL WHERE UserName = "' + username + '" AND SessionID = "' + sessionId + '"')
	.on('success',function(data) {
		res.send({type:"success", message: "Logged Out"});
	});
});

//	Takes username & password
//	Returns success
app.post('/user/register',function(req,res) {
	var username = req.body.username;
	var password = req.body.password;
	var email = req.body.email;
	if(username.length < 1 || password.length < 1) {
		res.send({type:"error", message: "UserName or Password not given"});
		return;
	}
	var salt = generateSalt();
	var hash = stringifyHex(crypto.pbkdf2Sync(password, salt, 200000, 20));
	sequelize.query('INSERT INTO Users (UserId, UserName, hash, salt) VALUES ("' + uuid.v4() + '","' + username + '", "' + hash + '", "' + salt + '")')
	.on('success', function(data) {
		res.send({type: "success", message: "User Created"});
	}).on('error', function(data) {
		if(data.name === "SequelizeUniqueConstraintError") {
			if(data.index === "PRIMARY'") {
				res.send({type:"error", message: "That UserName is already taken"});
			} else {
				res.send({type:"error", message: data});
			}
		} else {
			res.send({type: "error", message: data});
		}
	});
});

//	Takes username & sessionid
//	Returns list of event objects
app.post('/events/user/created',function(req,res) {
	var username = req.body.username;
	var sessionid = req.body.sessionid;
	sequelize.query('SELECT Events.* FROM Users JOIN Events ON UserID=Creator WHERE UserName= "' + username + '" AND SessionID= "' + sessionid + '"')
	.on('success',function(data) {
		res.send(data);
	});
});

//	Takes username & sessionid
//	Returns list of event objects
app.post('/events/user/attending',function(req,res) {
	var username = req.body.username;
	var sessionid = req.body.sessionid;
	sequelize.query('SELECT Events.* FROM Users JOIN AttendedEvents ON Users.UserID=AttendedEvents.UserID JOIN Events ON Events.EventID=AttendedEvents.EventID WHERE UserName= "' + username + '" AND SessionID= "' + sessionid + '"')
	.on('success',function(data) {
		res.send({type: "success", message: data});
	}).on('error',function(data) {
		res.send({type: "error", message: data});
	});
});

/*========================
			Helper Functions
========================*/
//	Returns newly generated salt
function generateSalt() {
	var buf = crypto.randomBytes(10);
	var salt = stringifyHex(buf);
	return salt;
}
//	Takes hex array
//	Returns concatenated string
function stringifyHex(hex) {
	var result = "";
	for(i = 0; i< hex.length; i++) {
		result += hex[i].toString(16);
	}
	return result;
}


/*===========================================
			Administrative/Debugging Functions
===========================================*/
app.get('/',function(req,res) {
	res.send("Node is up & running");
});

app.listen(app.get('port'), function() {
	console.log("Node app is running at localhost:" + app.get('port'))
});
