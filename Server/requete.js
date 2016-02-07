 //<b>requete.js</b> définit les functions qui font des requêtes à Redis ou à Nominatim
 //et la function renvoyant la pool de connexion servant dans les routes

//<table border="1">
//	<tr>
//		<th>module appelé dans</th> <th><a href="registration.html"><b>registration.js</b></a></th>
//		<th></th> <th><a href="login.html"><b>login.js</b></a></th>
//		<th></th> <th><a href="Faddress.html"><b>Faddress.js</b></a></th>
//	</tr>
//</table>

//Ajout des modules node.js
//<ul>
//	<li>  <a href="https://github.com/brianc/node-postgres"><b>Pg</b></a> : client pour postgreSQL</li>
//	<li>  <a href="https://github.com/assistunion/xml-stream"><b>XML-Stream</b></a> : outil pour parser du XML d'un flux</li>
//	<li>  <a href="https://github.com/coopernurse/node-pool"><b>Generic-Pool</b></a> : génération d'une pool de connection pour postgreSQL</li>
//</ul>
var async = require('async');
var pg = require('pg');
var poolModule = require('generic-pool');
var XmlStream = require('xml-stream');

//<b>isEmpty</b>: fonction vérifiant si un objet est nulle ou non
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>obj</b> : un objet quelconque</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>true</b> si l'objet est vide (ex: "", [], {})</li>
//		<li><b>false</b> si l'objet n'est pas vide</li>
//	</ul>
//</ul>
function isEmpty(obj) {
	if (obj === null)
		return true;
	if (obj.length > 0)
		return false;
	if (obj.length === 0)
		return true;
	for (var key in obj) {
		if (hasOwnProperty.call(obj, key))
			return false;
	}
	return true;
}

//Fonction <b>initPostPool</b><br />
// <small>lorsque la fonction est appelé dans <a href="app.js"><b>app.js</b></a>, elle crée la
// pool de connection et renvoit une fonction qui servira à faire les requêtes à la base de données postgreSQL</small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>config</b> : objet contenant les paramètres du serveur</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>fonction</b></li>
//	</ul>
//</ul>
exports.initPostPool = function(config) {
	var pool = poolModule.Pool({// Création de la pool
		name : 'psql',
		create : function(callback) {
			var param = "postgres://" + config.postgresql.db_user + ":" + config.postgresql.db_pass + "@" + 
				config.postgresql.db_host + ":" + config.postgresql.db_port + 
				"/" + config.postgresql.db_name;
			var client = new pg.Client(param);
			"postgres://127.0.0.1:@localhost:5433"
			client.connect();
			callback(null, client);
		},
		destroy : function(client) {
			client.end();
		},
		max : config.postgresql.pool_max,
		min : config.postgresql.pool_min,
		idleTimeoutMillis : config.postgresql.pool_idleTimeoutMillis,
		log : config.postgresql.pool_log
/*		max : 10,
		min : 2,
		idleTimeoutMillis : 30000,
		log : false*/
	});
	return (function(query, callback) {// renvoit de la fonction qui est appelé pg dans app.js
		pool.acquire(function(err, client) {// appel a la pool et récupération d'un client
			if (err)
				callback(err, null);
			else
				client.query(query, function(err, result) {// envoi de la requête avec le client obtenu précédemment
					pool.release(client);
					// libération du client dans la pool
					if (err)
						callback(err, null);
					else
						callback(null, result);
					// si tout c'est bien passé l'objet résultat est envoyé à la fonction Callback
				});
		});
	});
};

var te = ["email", "house_number", "road", "postcode", "city", "building", // sert a bien formaté la requête
"residential", "suburb", "city_district", "county", "state", "lon", "lat", "makepoint"];
// à la base de données PostgreSQL

//Fonction <b>queryNominatim</b><br />
// <small>lorsque la fonction est appelé dans <a href="app.js"><b>app.js</b></a>, elle renvoit
// une fonction qui récupére les informations liées à une position géographique
// (adresse, code postal, ville) depuis le serveur Nominatim et crée la requête qui sera envoyé à postgreSQL</small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>http</b> : objet de l'API node.js permettant d'envoyer des requêtes à des serveurs</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>String</b> : requête pour la base de données postgreSQL</li>
//		<li><b>err</b></li>
//	</ul>
//</ul>
exports.queryNominatim = function(http, config) {
	return (function(elements, Callback) {
		elements.house_number = (elements.road.search(/[0-9]+[ ,]+/g) != -1 ? elements.road.match(/[0-9]+/g)[0] : "01");
		elements.road = elements.road.replace([0-9]*[ ,], " ").trim();
		elements.town = elements.town.trim();
		elements.postcode = elements.postcode.trim();
		console.log(elements);
		var host = config.misc.nominatimURL;
		var path = '/search?q=' + elements.house_number + "+" + elements.road.replace(/ /g, '+') + "+" + elements.town.replace(/ /g, '+') + '&format=xml&polygon=1&addressdetails=1';
		console.log(path);
		var request = http.get({// envoit de la requête a Nominatim host + path
			host : host,
			path : path
		}).on('error', function(e) {// en cas d'erreur lors de la récupération de la réponse
			Callback(e);
		}).on('response', function(response) {
			response.setEncoding('utf8');
			var xml = new XmlStream(response, 'utf8');
			xml.collect('place');
			xml.on('endElement: searchresults', function(item) {// après avoir récupéré la réponse, parsage du XML
				if (item.place === undefined)
					Callback("error");
				else {
					item = item.place[0];
					var queryAddress = "INSERT INTO favorite_address VALUES(";
					for ( j = 0; j < te.length; ++j) {// création de la requête avec les informations recueillis
						if (te[j] != "makepoint" && te[j] != "lat" && te[j] != "lon") {
							queryAddress += (item[te[j]] === undefined ? (j < 5 ? "\'" + elements[te[j]] + "\'" : "null") : "\'" + item[te[j]].replaceBadCharacter() + "\'") + ",";
							elements[te[j]] = (item[te[j]] === undefined ? (j < 5 ? elements[te[j]] : "") : item[te[j]].replaceBadCharacter());
						} else if (te[j] == 'makepoint')
							queryAddress += "ST_MakePoint(" + item.$.lon + "," + item.$.lat + ")";
						else {
							queryAddress += item.$[te[j]] + ",";
							elements[te[j]] = item.$[te[j]];
						}
					}
					queryAddress += ");";
					Callback(null, queryAddress);
					// appel de la fonction Callback à la fin avec la requête
				}
			});
		});
	});
};

//Fonction <b>queryRegistration</b><br />
// <small>lorsque la fonction est appelé dans <a href="app.js"><b>app.js</b></a>, elle renvoit
// une fonction qui entre dans la base de données Redis les informations de l'utilisateur avec comme
// clé le code unique qui est celui envoyé par email sur l'adresse email de l'utilisateur
// (cf : <a href="registration.html"><b>registration.js</b>, function postRegistration</a> )
// et met une date d'expiration sur l'entré</small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>clientRedis</b> : objet du module redis permettant de faire des requêtes à la base de données</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>fonction</b></li>
//	</ul>
//</UL
exports.queryRegistration = function(clientRedis) {
	return (function(element, buf, time, Callback) {
		clientRedis.hmset("email:" + buf, element, function(errh, resp) {
			if (errh) {
				Callback(errh);
			} else {
				clientRedis.expire("email:" + buf, time, function(erre, resp) {
					if (erre) {
						Callback(erre);
					} else {
						Callback(null);
					}
				});
			}
		});
	});
};

exports.queryStatIncr = function(clientRedis) {
	return (function(nameStat, callback) {
		console.log("stats:" + nameStat);
		clientRedis.incr("stats:" + nameStat, function(err, result) {
			if (!err) {
				callback(null, result);
			} else {
				callback(err);
			}
		});
	});
};

//Fonction <b>queryLoginId</b><br />
// <small>lorsque la fonction est appelé dans <a href="app.js"><b>app.js</b></a>, elle renvoit
// une fonction qui récupère les informations contenu dans Redis pour un email
// (cf : <a href="login.html"><b>login.js</b>, function getLoginId</a> ) supprime l'entré
// dans Redis et envoit les informations à la fonction Callback</small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>clientRedis</b> : objet du module redis permettant de faire des requêtes à la base de données</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>fonction</b></li>
//	</ul>
//</ul>
exports.queryLoginId = function(clientRedis) {
	return (function(id, Callback) {
		clientRedis.hgetall("email:" + id, function(errh, r) {// récupère les informations sur l'utilisateur
			if (!errh && !isEmpty(r)) {
				clientRedis.del("email:" + id, function(errd, resp) {// supprime l'entré dans la base de données'
					if (!errd) {
						Callback(null, r);
					}// si pas d'erreurs
					else {
						Callback(errd);
					}
				});
			} else {
				Callback(errh);
			}
		});
	});
};

exports.queryProductPrice = function(config, clientRedis, pg) {
	return (function(element, Callback) {
		var name = "product:" + element.ean + ":" + element.id + ":" + element.promotion;
		var querySADD = "circle:" + name;
		var queryINCR = name + ":" + element.price; 
		var queryPG = "INSERT INTO product_shop VALUES(\'" + element.ean + "\', \'" + element.id + "\', \'" + 
			element.price + "\', " + (element.promotion === 't' ? "true" :  "false") + ", localtimestamp);";
		async.waterfall([
		function(callback) {
			clientRedis.sadd(querySADD, "user:" + element.email, function(err, resultSadd) {
				if (err || parseInt(resultSadd) != 1)
					callback("vous ne pouvez plus rentrer de prix pour ce produit");
				else
					callback(null);
			});
		},
		function(callback) {
			clientRedis.incr(queryINCR, function(err, resultIncr) {
				if (err)
					callback("error incr");
				else
					callback(null, resultIncr);
			});
		},
		function(number, callback) {
			if (number % config.misc.numberClientPostPrice == 0 && number != 0) {
				console.log(queryPG);
				pg(queryPG, function(err, result) {
					if (!err) {
						callback(null, result);
					} else {
						callback(config.stringError.ServerError + ": pg");
					}
				});
			} else {
				callback(null, number);
			}
		},
		function(result) {
			console.log(result);
			Callback(null, result);
		}], function(err) {
			console.log(err);
			Callback(err);
		});
	});
};
/*exports.queryAddProduct=function(config,clientRedis,pg){
	return(function(element,Callback){
		var name="product:"+ element.ean+":"+element.id+":"+element.pormotion;
		var querrySADD="circle:"+name;
		var queryINCR=name+":"+element.price	
*/