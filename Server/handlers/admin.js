//<table border="1">
//	<tr>
//		<th>routes ajoutées dans</th> <th><a href="app.html"><b>app.js</b></a></th>
//	</tr>
//</table>

//Ajout des modules node.js
//<ul>
//	<li>  <a href="http://nodejs.org/api/os.html"><b>os</b></a> : récupérer des informations du système</li>
//	<li>  <a href="http://nodejs.org/api/fs.html"><b>fs</b></a> : travailler avec les fichiers</li>
//	<li>  <a href="http://nodejs.org/api/path.html"><b>path</b></a> : travailler avec le système de fichier Unix</li>
//	<li>  <a href="http://nodejs.org/api/child_process.html"><b>child_process</b></a> : créer des threads avec des commandes Unix</li>
//	<li>  <a href="https://github.com/arunoda/node-usage"><b>usage</b></a> : récupérer des informations sur des processus par pid</li>
//</ul> 
var os = require('os');
var fileSystem = require('fs');
var path = require('path');
var exec = require('child_process').exec;
var element = [];
var usage = require('usage');

// Récuperer les informations pour les processus : nodejs, redis-server, postgres
// pour mettre dans la variable element
(function() {
	element.push({
		name : "node",
		pid : process.pid // récupération du pid de node.js
	});
	exec("ps axf | grep redis-server | grep -v grep", function (err, stdout, stderr) {  // lancement de la requête
		if (!err) {																																				// pour récupérer le pid de redis-server
			element.push({
				name : "redis",
				pid : stdout.match(/[0-9]+/)[0]
			});
			exec("ps axf | grep postgres | grep -v grep", function (err, stdout, stderr) { // lancement de la requête
				if (!err) {																																	 // // pour récupérer le pid de postgres
					element.push({
						name : "postgresql",
						pid : stdout.match(/[0-9]+/)[0] // extraction du pid de la requête
					});
				}
			});
		}
	});
})();

//Route GET <b>/system</b><br />
// <small>route renvoyant la page d'administrateur</small>  
//<ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>Page HTML</b></li>
//	</ul>
//</ul>
exports.system = function(config, pg, clientRedis) {
	return (function(req, res) {
		var bo = false;
		for (y = 0; y < config.admin.administrators.length; ++y) {
			if (res.get('user') === config.admin.administrators[y]) {
				bo = true;
			}
		}
		if (bo == false) {
			res.redirect('/');
		} else {
			var element = [
				{name : "temp files directory", cmd : "tmpdir"}, // envoyé dans la page jade
				{name : "endian", cmd : "endianness"},					 // pour le lancement des requêtes d'os
				{name : "hostname", cmd : "hostname"},					 // et leur nom sur la page
				{name : "type", cmd : "type"},
				{name : "platform", cmd : "platform"},
				{name : "release", cmd : "release"},
				{name : "CPU arch", cmd : "arch"},
				{name : "total MEM", cmd : "totalmem"},
				{name : "free MEM", cmd : "freemem"},
			];
			res.render('admin', {
				web : config.web, 
				os : os, elem : element, 
				cpus : os.cpus(), // informations sur les CPUs de la machine sur laquelle tourne node.js
				nets : os.networkInterfaces(), // interfaces réseaux de la machine
			});
		}
	});
};

//Route GET <b>/system/activity</b><br />
// <small>permet la récupération depuis la page admin des informations sur l'activité du serveur
// (nombre de connection en une semaine, etc...) pour en faire un graphique</small>
// <small>route appelé dans <a href="jquery-admin.html"><b>jquery-admin.js</b></a></small> 
//<ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : contenant les informations</li>
//	</ul>
//</ul>
exports.systemActivity = function(config, pg, clientRedis) {
	return (function(req, res) {
		var querySystem = "SELECT * FROM stats WHERE time >= current_date - interval '2 month' ORDER BY name, time ASC;";
		pg(querySystem, function(err, result) {
			if (!err) {
				res.json({registered : "success", results : result.rows});
			} else {
				res.json({registered : "failure"});
			}
		});
	});
};

//Route GET <b>/system/process</b><br />
// <small>permet la récupération depuis la page admin des informations (utilisation cpu et memory) 
// sur les processus redis, postgresql et node.js pour en faire un graphique</small>
// <small>route appelé dans <a href="jquery-admin.html"><b>jquery-admin.js</b></a></small> 
//<ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : contenant les informations</li>
//	</ul>
//</ul>
exports.systemProcess = function(config, pg, clientRedis) {
	return (function(req, res) {
		var bo = false;
		for (y = 0; y < config.admin.administrators.length; ++y) {
			if (res.get('user') === config.admin.administrators[y]) {
				bo = true;
			}
		}
		if (bo == false) {
			res.redirect('/');
		} else {
			var asyncEach = require('async');
			var finalResult = {};
			asyncEach.each(element, function(miniElement, callback) {
				usage.lookup(miniElement.pid, function(err, result) {
					if (!err) {
						finalResult[miniElement.name] = {
							pid : miniElement.pid,
							cpu : Math.round(result.cpu),
							memory : Math.round(result.memory / os.totalmem())
						};
						callback();
					} else {
						callback(err);
					}
				});
			}, function(err) {
				if (!err) {
					res.json({registered : "success", results : finalResult, mem : os.totalmem()});
				} else {
					res.json({registered : "failure", err: [err]});
				}
			});
		}
	});
};

//Route GET <b>/system/selectfile</b><br />
// <small>renvoit les fichiers disponibles à la modification</small>
// <small>route appelé dans <a href="jquery-admin.html"><b>jquery-admin.js</b></a></small> 
//<ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : contenant les informations</li>
//	</ul>
//</ul>
exports.selectFile = function(config) {
	return (function(req, res) {
		var admins = null;
		for (y = 0; y < config.admin.administrators.length; ++y) {
			if (config.admin.administrators[y] == res.get('user')) {
				admins = config.admin.administrators[y];
			}
		}
		if (!admins) {
			res.redirect('/');
		} else {
			var elements = [];
			for (y = 0; y < config.admin.files.length; ++y) {
				elements.push(config.admin.files[y].name);
			}
			res.render('selectFile', {files : elements, web : config.web});
		}
	});
};

//Route POST <b>/system/receivefile</b><br />
// <small>récupère le fichier envoyé par la page admin pour changer le fichier sur 
// le serveur et dans le cas</small>
// <small>route appelé dans <a href="jquery-admin.html"><b>jquery-admin.js</b></a></small> 
//<ul>
//	<li>paramètres</li>
//	<ul>
//		<li><b>name</b> : nom du fichier</li>
//		<li><b>file</b> : contenu du fichier</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : confirmation de la réception</li>
//	</ul>
//</ul>
exports.receiveFile = function(config) {
	return(function(req, res) {
		var admins = null;
		for (y = 0; y < config.admin.administrators.length; ++y) {
			if (config.admin.administrators[y] == res.get('user')) {
				admins = config.admin.administrators[y];
			}
		}
		if (!admins) {
			res.redirect('/');
		} else {
			var element;
			for (y = 0; y < config.admin.files.length; ++y) {
				if (config.admin.files[y].name == req.body.name) {
					element = config.admin.files[y];
				}
			}
			if (element == undefined) {
				res.json({registered : "success"});
				return;
			}
			var filePath = path.join(__dirname, "../" + element.path + element.name); // récupération du chemin vers le fichier
			fileSystem.writeFile(filePath, req.body.file, function(err) { // écriture du fichier reçu dans celui d'origine
				if(err) {
					res.json({registered : "failure", err : [err]});
				} else if (element.name === "config.json") { // dans le cas de config.json
					var newconfig = JSON.parse(req.body.file);	// parsage du fichier reçu
					for (var key in newconfig) {								// modification de l'objet config défini dans app.js
						config[key] = newconfig[key];
					}
					res.json({registered : "success"});
				} else {
					res.json({registered : "success"});
				}
			});
		}
	});
};

//Route GET <b>/system/sendfile</b><br />
// <small>envoie du fichier à la page admin</small>
// <small>route appelé dans <a href="jquery-admin.html"><b>jquery-admin.js</b></a></small> 
//<ul>
//	<li>paramètres</li>
//	<ul>
//		<li><b>name</b> : nom du fichier</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : confirmation de la réception</li>
//	</ul>
//</ul>
exports.sendFile = function(config) {
	return (function(req, res) {
		var admins = null;
		for (y = 0; y < config.admin.administrators.length; ++y) {
			if (config.admin.administrators[y] == res.get('user')) {
				admins = config.admin.administrators[y];
			}
		}
		if (!admins) {
			res.redirect('/');
		} else {
			var element;
			for (y = 0; y < config.admin.files.length; ++y) {
				if (config.admin.files[y].name == req.query.name) {
					element = config.admin.files[y];
				}
			}
			var filePath = path.join(__dirname, "../" + element.path + element.name); // récupération du chemin vers le fichier
		    var stat = fileSystem.statSync(filePath);
		    res.writeHead(200, {
				'Content-Type' : (req.query.name.indexOf('.json') != -1) ? "application/json" : "text/plain",
				'Content-Length' : stat.size
			});
			var readStream = fileSystem.createReadStream(filePath); // création d'un readable depuis le fichier
			readStream.pipe(res);																		// création d'un pipe avec la réponse
		}
	});
};
