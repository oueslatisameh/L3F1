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

// Déclaration de tableau
var nuance = { // tableau des champs d'un champ avec sa fonction de format et si le champ est clé primaire dans postgreSQL ou non
	email : {val :validator.isEmail, ne : true},
	password : {val : function() {return(true);}, ne : true},
	cpassword : {val : function() {return(true);}, ne : true},
	name : {val : validator.isAlpha, ne : true},
	forename : {val : validator.isAlpha, ne : true},
	birth : {val : validator.isDate, ne : false},
	address : {val : function (input) {
		input = input.replace(/[, áàâäãåçéèêëíìîïñóòôöõúùûüýÿæœÁÀÂÄÃÅÇÉÈÊËÍÌÎÏÑÓÒÔÖÕÚÙÛÜÝŸÆŒ-]/g, "");
		return (validator.isAlphanumeric(input));
	}, ne : false},
	postcode : {val : validator.isNumeric, ne : false},
	town : {val : validator.isAlpha, ne : false}
};

//Route POST <b>/registration</b><br />
// <small>route qui sert à vérifier les champ d'enregistrement rempli par l'utilisateur.
// dans le cas où les champs ont été rempli convenablement, les informations sont stockées temporairement dans Redis
// jusqu'au moment où l'utilisateur cliquera dans le lien de l'email (cf: la route /login/:id dans <a href="login.html"><b>login.js</b></a>)</small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>name</b> : nom de l'utilisateur</li>
//		<li><b>forename</b> : prénom de l'utilisateur</li>
//		<li><b>birth</b> : date de naissance</li>
//		<li><b>address</b> : nom de la rue avec le numéro</li>
//		<li><b>town</b> : nom de la ville</li>
//		<li><b>postcode</b> : code postal de l'addresse</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : confirmation de la requête</li>
//		<li><b>page HTML</b> : home</li>
//		<li><b>cookie signé</b> : contenant un identifiant unique vérifié à chaque requête vers les routes contenant le middleware ensureAuthenticated (cf : les routes dans <a href="app.html"><b>app.js</b></a>)</li>
//	</ul>
//</ul>
exports.postRegistration = function(config, redisfunc, pg, email, errorGes) {
	return (function(req, res){
		var user = req.get("user-agent");
		var error = [];

		for (var i in nuance) { // tests sur les champs recuperes
			if (nuance[i].ne && !req.body[i]) {
				error.push(config.stringRL['missing' + i.capitalize()]);
			} else if (req.body[i] && !nuance[i].val(req.body[i])) {
				error.push(config.stringRL['form' + i.capitalize()]);
			}
		}
		if (req.body.password != req.body.cpassword) { // testes si le mot de passe et la confirmation sont les mêmes
			error.push(config.stringRL.notSamePassword);
		}
		if (error.length === 0) { // Si les tests sont concluants
			crypto.randomBytes(config.misc.sizeRandomEmail, function(ex, buf) {
				buf = buf.toString('hex').substr(0, config.misc.sizeRandomEmail);
				var queryRegistration = 'SELECT user_email FROM users WHERE user_email=\''+
					req.body.email+'\';';
				pg(queryRegistration, function(err, result) { // vérifier si l'email existe déjà
					if (err) {errorGes(user, res, 'registration');}
					else if (result.rowCount !== 0) {errorGes(user, res, 'registration', [config.stringRL.existEmail]);}
					else {
						var sh = crypto.createHash('sha1').update(req.body.password).digest('hex'); // création du sha1 du mot de passe
						var element = {email : req.body.email, password : sh, forename : req.body.forename || " ",
							name : req.body.name || " ", birth : req.body.birth || " ", address : req.body.address || " ",
							postcode : req.body.postcode || " ", town : req.body.town || " "};
						/* envoi des informations dans Redis (cf : queryRegistration 
							dans <a href="requete.js"><b>requete.js</b></a>)*/
						redisfunc(element, buf, config.misc.timeEmail, function(err) {
							if (err) {errorGes(user, res, 'registration');}
							else {
								var newemail = email.Email(req.body.email, config.config.urldev, buf); // création du corps de l'email
								email.smtpTransport.sendMail(newemail, function (err, resp) { // envoi de l'email
									if (err) {errorGes(user, res, 'registration');}
									else if (user == 'scanoid/1.0') // confirmation selon l'user-agent
										res.json({registered : "success"});
									else
										res.redirect('/');
								});
							}
						});
					}
				});
			});
		} else {errorGes(user, res, 'registration', error);}
	});
};

//Route GET <b>/login</b><br />
// <small>route renvoyant une page pour s'enregistrer</small>  
//<ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>Page HTML</b> : registration</li>
//	</ul>
//</ul>
exports.getRegistration = function(config) {
	return(function(req,res) {
		res.render('registration', {web : config.web});
	});
};

//Route GET <b>/email</b><br />
// <small>route permettant de vérifier si une adresse email existe dans la base de données postgreSQL</small><br />
// <small>route appelé dans <a href="jquery-registration.html"><b>jquery-registration.js</b></a></small>  
//<ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : confirmation</li>
//	</ul>
//</ul>
exports.getEmail = function(config, pg, errorGes) {
	return(function(req, res) {
		req.query.email = req.query.email.replaceBadCharacter().trim();
		var queryEmail = "SELECT * FROM users WHERE user_email = \'" + req.query.email + "\';";
		pg(queryEmail, function(err, result) {
			if (err || result.rows.length !== 0) {
				res.json({registered : "failure"});
			} else {
				res.json({registered : "success"});
			}
		});
	});
};