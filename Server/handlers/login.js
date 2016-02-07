//<table border="1">
//	<tr>
//		<th>routes ajoutées dans</th> <th><a href="app.html"><b>app.js</b></a></th>
//	</tr>
//</table>

//Ajout des modules node.js
//<ul>
// <li>  <a href="https://github.com/chriso/node-validator"><b>Validator</b></a> : ensemble de fonction de format de String</li>
// <li>  <a href="http://nodejs.org/api/crypto.html"><b>Crypto</b></a> : ensemble de fonction pour la cryptographie</li>
//</ul>
var crypto = require('crypto');
var validator = require('validator');

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
  if (obj === null) return true;
  if (obj.length > 0)    return false;
  if (obj.length === 0)  return true;
	for (var key in obj) {
    if (hasOwnProperty.call(obj, key)) return false;
  }
	return true;
}

//Route GET <b>/login</b><br />
// <small>route renvoyant une page pour se connecter</small>  
//<ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>Page HTML</b> : login</li>
//	</ul>
//</ul>
exports.getLogin = function (config) {
	return (function(req, res) {
		res.render('login', {web : config.web});
	});
};

//Route GET <b>/login/:id</b><br />
// <small>lorsque l'utilisateur clique dans le lien de l'email envoyé après l'enregistrement
// (cf. la route postRegistration dans <a href="registration.html"><b>registration.js</b></a>) les informations dans Redis
// sont enregistré dans postgreSQL. Si l'utilisateur a mis son adresse, son code postal et sa ville
// alors une adresse favorite est automatiquement ajouté</small>  
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>id</b> : numéro unique créé avec crypto</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>Page HTML</b> : login</li>
//	</ul>
//</ul>
exports.getLoginId = function (config, redisfunc, query_nominatim, queryStatIncr, pg, errorGes) {
	return(function(req, res) {
		var asyncWaterfall = require('async');
		var user = req.get('user-agent');
		asyncWaterfall.waterfall([ // création d'un waterfall servant à imbriquer des fonctions
			function(callback) {
				redisfunc(req.params.id, function(err, resultUser) {
					if (!err) {
						callback(null, resultUser);
					} else {
						callback(err);
					}
				});
			},
			function(r, callback) {
				var queryLogin = "INSERT INTO users values(\'" + r.email + "\',\'" + r.password + "\',\'" +
				r.name + "\'," + (r.forename == ' ' ? "null" : "\'" + r.forename + "\'") +
				"," + (r.birth == ' ' ? "null" : "\'" + r.birth + "\'")+ // création de la requête à postgreSQL
				"," + (r.address == ' ' ? "null" : "\'" + r.address + "\'")+
				"," + (r.postcode == ' ' ? "null" : "\'" + r.postcode + "\'")+
				"," + (r.town == ' ' ? "null" : "\'" + r.town + "\'")+
				");";
				pg(queryLogin, function(err, resultPG) {
					if (!err && r.address != ' ' && r.town != ' ' && r.postcode != ' ') {
						/* si les informations entrées par l'utilisateur lors de son inscription 
						contenaient l'adresse, le code postal et la ville alors une recherche sur 
						le serveur nominatim est effectuer pour récupéré la longitude et la latitude et 
						d'autres informations pour pouvoir créer automatiquement une adresse favorite*/
						var epic = {road : r.address, town : r.town, postcode : r.postcode, email : r.email};
						query_nominatim(epic, function(err, queryAddress) {
							if (!err) {
								pg(queryAddress, function(err, resultPut) { // l'adresse favorite est ajouté dans la base données
									if (!err) {callback(null, resultPut);}
									else {callback(err);} // redirection vers home
								});
							} else {callback(err);}
						});
					} else if (!err) {
						callback(null, null);
					} else {
						callback(err);
					}
				});
			},
			function(finish, callback) {
				queryStatIncr("registration", function(err, resultStatIncr) {
					/* incrémentation de la variable registration
					dans Redis pour les statistiques*/
					if (!err) {
						callback(null, (finish || resultStatIncr));
					} else {
						callback(err);
					}
				});
			}
		], function(err, result) {
			if (!err) {
				res.redirect('/');
			} else {
				errorGes(user, res, 'login', [err]);
			}
		});
	});
};

//Route POST <b>/login</b><br />
// <small>si la valeur des champs email et password sont dans la base de données, alors 
// un objet contenant l'email est envoyé a Redis avec une date d'expiration et un cookie avec 
// le même temps est envoyé au client afin qu'il puisse accéder aux autres services du serveur</small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>email</b> : l'adresse email</li>
//		<li><b>password</b> : hash du mot de passe</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : confirmation de la requête</li>
//		<li><b>page HTML</b> : home</li>
//		<li><b>cookie signé</b> : contenant un identifiant unique vérifié à chaque requête vers les routes 
//      contenant le middleware ensureAuthenticated (cf : les routes dans <a href="app.html"><b>app.js</b></a>)</li>
//	</ul>
//</ul>
exports.postLogin = function (config, clientRedis, queryStatIncr, pg, errorGes) {
	return (function(req,res) {
		var user = req.get("user-agent");
		var error = [];
		var wherefrom = (user == 'scanoid/1.0') ? 'app' : 'web';
		if (!req.body.email) // vérification des champs récupérés
			error.push(config.stringRL.missingEmail);
		else if (validator.isEmail(req.body.email) === false)
			error.push(config.stringRL.formEmail);
		if (!req.body.password)
			error.push(config.stringRL.missingPassword);
		if (error.length === 0) {
			var asyncWaterfall = require('async');
			asyncWaterfall.waterfall([
				function(callback) {
					crypto.randomBytes(config.misc.sizeRandomEmail, function(ex, buf) {
						callback(null, buf.toString('hex').substr(0, config.misc.sizeRandomEmail));
					});
				},
				function(buf, callback) {
					var sh = crypto.createHash('sha1').update(req.body.password).digest('hex'); // création du hash du mot de passe
					var queryLogin = 'SELECT user_email,user_password FROM users WHERE user_email=\''+ // création de la requête à postgreSQL
						req.body.email +'\' AND user_password=\''+ sh +'\';';
					pg(queryLogin, function(err, resultL) {
						if (!err && resultL.rows.length === 0) {
							callback(config.stringRL.login);
						} else if (!err) {
							callback(null, buf);
						} else {
							callback(err);
						}
					});
				},
				function(buf, callback) {
					clientRedis.set(wherefrom + ":" + buf, req.body.email, function (err, resp) { // envoit d'un objet dans Redis numéro aléatoire + mot de passe
						if (!err) {
							res.cookie('name', wherefrom + ":" + buf, {httpOnly: true, signed : true}); // création du cookie pour le client 
							if (user == 'scanoid/1.0') {// si l'utilisateur est sur l'application Android pas de date d'expiration
								callback(null, buf);
							} else {
								clientRedis.expire(wherefrom + ":" + buf, config.misc.timeConnexion, function(err2, resp2) {
									/* mise en place d'une date d'expiration pour l'entree dans Redis*/
									if (!err) {
										callback(null, resp2);
									} else {
										callback(err2); // redirection vers home si tout c'est bien passé
									}
								});
							}
						}
						if (err) {
							callback(err);
						}
					});
				},
				function(finish, callback) {
					queryStatIncr("connection", function(err, resultStatIncr) { 
						/* incrémentation de la variable connection
						dans Redis pour les statistiques*/
						if (!err) {
							callback(null, finish);
						} else {
							callback(err);
						}
					});
				}
			], function(err, finish) {
				if (!err && user == 'scanoid/1.0') {
					res.json({registered : "success", code : wherefrom + ":" + finish}); // envoit de la confirmation
				} else if (!err) {
					res.redirect('/');
				} else {
					errorGes(user, res, 'login', [err]);
				}
			});
		} else {
			errorGes(user, res, 'login', error);
		}
	});	
};

//Route POST <b>/loginFacebook</b><br />
// <small>reçoit les champs du formulaire de connexion facebook envoyé par l'application Android,
// si l'utilisateur est déjà inscrit sur le site alors un cookie est envoyé comme postLogin. si
// l'utilisateur n'est pas inscrit sur l'application scanOID alors avant d'envoyé un cookie signé,
// une entrée est créée dans la base postgreSQL avant d'envoyer le cookie signé </small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>name</b> : nom de l'utilisateur</li>
//		<li><b>forename</b> : prénom de l'utilisateur</li>
//		<li><b>email</b> : l'adresse email</li>
//		<li><b>id</b> : hash de l'id donné par facebook à l'utilisateur</li>
//		<li><b>birth</b> : date de naissance</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : confirmation de la requête</li>
//		<li><b>cookie signé</b> : contenant un identifiant unique vérifié à chaque requête vers les routes 
//			contenant le middleware ensureAuthenticated (cf : les routes dans <a href="app.html"><b>app.js</b></a>)</li>
//	</ul>
//</ul>
exports.postLoginFacebook = function(config, clientRedis, pg, errorGes) {
	return (function(req, res) {
		var user = req.get("user-agent");
		var wherefrom = (user == 'scanoid/1.0') ? 'app' : 'web';
		
		console.log(req.body.email + " " + req.body.id + " " + req.body.forename + " " + req.body.name + " " + req.body.birth);
		if (req.body.birth != null) {
			var coucou = req.body.birth.match(/[0-9]+/g);
			req.body.birth = coucou[2] + "-" + coucou[0] + "-" + coucou[1];
		}
		var loggFacebook = function() {
			/* définition de la fonction pour créé une entrée avec une date 
			d'expiration dans Redis et un cookie signé à envoyé au client*/
			var reqi = req,resi = res;
			crypto.randomBytes(config.misc.sizeRandomEmail, function(ex, buf) {
				buf = buf.toString('hex').substr(0, config.misc.sizeRandomEmail);
				clientRedis.set(wherefrom + ":" + buf, reqi.body.email, function (err, resp) {
					console.log("error [" + err + "] je suis dans" + "clientRedis set");
					if (err) {errorGes(user, resi, 'login');}
					else {
						resi.cookie('name', wherefrom + ":" + buf, {httpOnly: true, signed : true}); // création du cookie
						if (user == 'scanoid/1.0')  // si l'utilisateur est sur l'application Android pas de date d'expiration
							resi.json({registered : "success", code : wherefrom + ":" + buf});
						else {
							clientRedis.expire(wherefrom + ":" + buf, config.misc.timeConnexion, function(err, resp) { // date d'expiration
								if (err) {errorGes(user, resi, 'login');}
								else
									resi.redirect('/'); // redirection vers home
							});
						}
					}
				});			
			});		
		};
		
		var queryEmail = "SELECT * FROM users where user_email = \'" + req.body.email + "\';";
		pg(queryEmail , function(err, re) { // vérification de la présence de l'email dans la base
			console.log("error [" + err + "] je suis dans" + "pg queryEmail");
			if (err) {errorGes(user, res, 'login');}
			else if (re.rows.length !== 0) { // l'email existe dans la base
				queryEmail = "SELECT * FROM users where user_email = \'" + req.body.email +
				"\' AND user_password = \'" + req.body.id + "\';";
				pg(queryEmail, function(err, resultat) {  // vérification de l'association adresse email password dans postgreSQL
					if (err || resultat.rows.length === 0) {errorGes(user, res, 'login');}
					else
						loggFacebook(); // si pas d'erreur
				});
			} else {
				var queryRegistration = "INSERT INTO users values(\'" + req.body.email  + // création de la requête postgreSQL
				"\',\'" + req.body.id + "\',\'"+
				req.body.name + "\',\'" + req.body.forename + "\'" +
				"," + (req.body.birth == ' ' ? "null" : "\'" + req.body.birth + "\'")+
				");";
				pg(queryRegistration, function(err, resultat) { // requête pour entrer le nouvelle utilisateur facebook
				console.log("error [" + err + "] je suis dans" + "pg queryRegistration");
					if (!err) {loggFacebook();} // si pas d'erreur
					else
						errorGes(user, res, 'login');
				});				
			}
		});
	});
};
