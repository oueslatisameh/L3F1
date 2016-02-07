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
var validator = require('validator');
var crypto = require('crypto');

//Route POST <b>/recovery</b><br />
// <small>route appelée lorsque sur le site internet ou dans l'application, l'utilisateur 
// entre son adresse email pour la récupération d'un nouveau mot de passe. un email est envoyé
// avec un mot de passe généré aléatoirement et un lien vers le serveur avec un identifiant unique</small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>email</b> : email de l'utilisateur</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>Page HTML</b> : login</li>
//	</ul>
//</ul>
exports.postRecovery = function(config, redisfunc, pg, email, errorGes) {
	return (function(req, res) {
		user = req.get('user-agent');
		if (validator.isEmail(req.body.email)) {
			var asyncWaterfall = require('async');
			asyncWaterfall.waterfall([ // création d'un waterfall servant à imbriquer des fonctions
				function(callback) {
					var queryPG = "SELECT * FROM users WHERE user_email = \'" + req.body.email + "\'";
					pg(queryPG, function(err, result) { // requête pour savoir l'email existe dans la base postgresql
						if (!err && result.rows.length !== 0) {
							callback(null);
						} else if (!err && result.rows.length === 0) {
							callback(config.stringRL.notExistEmail);
						} else {
							callback(config.stringError.postgresqlError);
						}
					});
				},
				function(callback) {
					crypto.randomBytes(config.misc.sizeRandomPassword, function(ex, ebuf) { // création du nouveau mot de passe
						callback(null, ebuf.toString('hex').substr(0, config.misc.sizeRandomPassword));
					});
				},
				function(ebuf, callback) {
					crypto.randomBytes(config.misc.sizeRandomEmail, function(ex, buf) { // création d'un identifiant pour le lien dans l'email
						callback(null, buf.toString('hex').substr(0, config.misc.sizeRandomEmail), ebuf);
					});
				},
				function(buf, ebuf, callback) {
					/* a ce moment, un objet est envoyé dans la base Redis afin de garder en mémoire l'email envoyé avec
					le nouveau mot de passe et l'adresse email associé. l'objet a une date d'expiration (cf : requete.js)*/
					redisfunc({password : ebuf, email : req.body.email}, buf, config.misc.timeEmail, function(err, result) {
						if (!err) {
							callback(null, buf, ebuf);
						} else {
							callback(config.stringError.redisError);
						}
					});
				},
				function(buf, ebuf, callback) {
					/*création d'un objet "contenu d'email" avec les informations de l'utilisateur*/
					var newEmail = email.Email(req.body.email, config.config.urldev, buf);
					newEmail.html = "<h1>changement de mot de passe</h1><br />" +
						"<p> votre nouveau mot de passe : <b>" + ebuf + "</b><br />"+
						"veuillez cliquer sur le lien en dessous : "+
						"<a href=\"http://" + config.config.urldev + "/recovery/" + buf +
						"\">Lien vers le site ScanOID</a> pour le rendre actif</p>";
					email.smtpTransport.sendMail(newEmail, function (err, resp) { // envoi de l'email
						callback((err != null) ? config.stringError.nodemailerError : null);
					});
				}
			], function(err) {
				if (!err) {
					res.json({registered : "success"});
				} else {
					res.json({registered : "failure", err : [err]});
				}
			});
		} else {
			errorGes(user, res, 'login', [config.stringRL.formEmail]);			
		}
	});
};

//Route POST <b>/recovery</b><br />
// <small>lorsque l'utilisateur a appuyé sur le lien dans l'email pour un mot de passe perdu,
// le mot de passe est rentré dans la base de données postgresql</small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>id</b> : identifiant unique pour l'email</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>Page HTML</b> : login</li>
//	</ul>
//</ul>
exports.getRecovery = function(config, redisfunc, pg, errorGes) {
	return (function(req, res) {
		user = req.get('user-agent');
		var asyncWaterfall = require('async');
		asyncWaterfall.waterfall([
			function(callback) {
				redisfunc(req.params.id, function(err, resp) {
					if (!err && resp == null) {
						callback(config.stringError.noAuthorization);
					} else if (!err) {
						callback(null, resp);
					} else {
						callback(config.stringError.redisError);
					}
				});
			},
			function(r, callback) {
				var sh = crypto.createHash('sha1').update(r.password).digest('hex'); // création du sha1 du mot de passe
				sh = crypto.createHash('sha1').update(sh).digest('hex');
				var queryPG = "UPDATE users SET user_password = \'" + sh + "\' WHERE user_email = \'" + r.email + "\'";
				pg(queryPG, function(err, result) {
					if (!err) {
						callback(null);
					} else {
						callback(config.stringError.postgresqlError);
					}
				});
			}
		], function(err) {
			if (!err) {
				res.redirect('/');
			} else {
				res.errorGes(user, res, 'login', [err]);
			}
		});
	});	
}

